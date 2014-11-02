/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2014 scd4j scd4j.tools@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.datamaio.scd4j.util.io;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class ZipUtils {

	private static final Logger LOGGER = Logger.getLogger(ZipUtils.class);
	
	/**
	 * Unzips the specified zip file to the specified destination directory.
	 * Replaces any files in the destination, if they already exist.
	 * 
	 * @param zipFilename
	 *            the name of the zip file to extract
	 * @param destFilename
	 *            the directory to unzip to
	 * @throws IOException
	 */
	public static void unzip(String zipFilename, String destDirname) {
		final Path zipFile = Paths.get(zipFilename);
		final Path destDir = Paths.get(destDirname);
		unzip(zipFile, destDir);
	}

	public static void unzip(final Path zipFile, final Path destDir) {
		LOGGER.trace("Unziping Archive: " + zipFile);
		FileUtils.createDirectories(destDir);
	
		try (FileSystem zipFileSystem = createZipFileSystem(zipFile, false)) {
			final Path root = zipFileSystem.getPath("/");

			// walk the zip file tree and copy files to the destination
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					final Path dirToCreate = PathUtils.get(destDir, dir);
					if (Files.notExists(dirToCreate)) {
						LOGGER.trace("Creating directory " + dirToCreate);
						Files.createDirectory(dirToCreate);
					}
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					final Path destFile = PathUtils.get(destDir, file);
					LOGGER.trace("Extracting file " + file + " to " + destFile);
					Files.copy(file, destFile, REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates/updates a zip file.
	 * 
	 * @param zipFilename
	 *            the name of the zip to create
	 * @param filenames
	 *            list of filename to add to the zip
	 * @throws IOException
	 */
	public static void create(String zipFilename, final String relativeDir, String... filenames) {
		Path zipFile  = Paths.get(zipFilename);
		Path relative  = Paths.get(relativeDir);
		Path[] files = new Path[filenames.length];
		for (int i=0; i<filenames.length; i++) {
			files[i] = Paths.get(filenames[i]);
		}
		create(zipFile, relative, files);
	}

	public static void create(final Path zipFile, final Path relativeDir, Path... files) {
		LOGGER.trace("Creating Archive: " + zipFile);
		
		if(files.length > 0 ) {
			for (Path path : files) {
				if(!path.startsWith(relativeDir)){
					throw new IllegalArgumentException("Path " + path + " is not related to " + relativeDir);
				}
			}
		} else {
			files = new Path[]{relativeDir}; 
		}
		
		try (FileSystem zipFileSystem = createZipFileSystem(zipFile, true)) {
			final Path root = zipFileSystem.getPath("/");

			// iterate over the files we need to add
			for (Path file : files) {				
				// add a file to the zip file system
				if (!Files.isDirectory(file)) {
					final Path relativized = relativeDir.relativize(file);
					final Path dest = zipFileSystem.getPath(root.toString(), relativized.toString());
					final Path parent = dest.getParent();
					if (Files.notExists(parent)) {
						LOGGER.trace("Creating directory " + parent);
						Files.createDirectories(parent);
					}
					Files.copy(file, dest, REPLACE_EXISTING);
				} else {
					// for directories, walk the file tree
					Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							final Path relativized = relativeDir.relativize(file);
							final Path dest = zipFileSystem.getPath(root.toString(), relativized.toString());
							Files.copy(file, dest, REPLACE_EXISTING);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
							final Path relativized = relativeDir.relativize(dir);
							final Path dirToCreate = zipFileSystem.getPath(root.toString(), relativized.toString());
							if (Files.notExists(dirToCreate)) {
								LOGGER.trace("Creating directory " + dirToCreate);
								Files.createDirectories(dirToCreate);
							}
							return FileVisitResult.CONTINUE;
						}
					});
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	

	/**
	 * List the contents of the specified zip file
	 * 
	 * @param filename
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static List<String> list(String zipFilename) throws IOException {
		Path zipFile = Paths.get(zipFilename);
		return list(zipFile);
	}

	public static List<String> list(Path zipFile) throws IOException {
		LOGGER.trace("Listing Archive: " + zipFile);
		final List<String> list = new ArrayList<>();

		// create the file system
		try (FileSystem zipFileSystem = createZipFileSystem(zipFile, false)) {

			final Path root = zipFileSystem.getPath("/");

			// walk the file tree and print out the directory and filenames
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				final DateFormat DF = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
				
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					// o diretório raiz não deve entrar na listagem
					if(!dir.toString().equals("/")) {
						addToListAndPrint(dir);
					}
					return FileVisitResult.CONTINUE;
				}

				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					addToListAndPrint(file);
					return FileVisitResult.CONTINUE;
				}

				private void addToListAndPrint(Path file) throws IOException {
					list.add(file.toString());
					final String modTime = DF.format(new Date(Files.getLastModifiedTime(file).toMillis()));
					LOGGER.debug(Files.size(file) + " " + modTime + " " + file);
				}
			});
		}
		
		return list;
	}
	
	private static FileSystem createZipFileSystem(final Path path, boolean create) throws IOException {
		// convert the filename to a URI
		final URI uri = URI.create("jar:file:" + path.toUri().getPath());

		final Map<String, String> env = new HashMap<>();
		if (create) {
			env.put("create", "true");
		}
		return FileSystems.newFileSystem(uri, env);
	}
}