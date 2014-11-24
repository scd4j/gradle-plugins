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

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

/**
 * 
 * @author Fernando Rubbo
 */
public class FileUtilsTest {
	
	@Test
	public void deleteFile() throws IOException{
		Path file = createTempFile("FILE", ".tmp");
		assertThat(exists(file), is(true));
		FileUtils.delete(file);
		assertThat(exists(file), is(false)); 
	}
	
	@Test
	public void deleteEmptyDir() throws IOException{
		Path dir = Files.createTempDirectory("DIR");
		assertThat(exists(dir), is(true));
		FileUtils.delete(dir);
		assertThat(exists(dir), is(false)); 
	}
	
	@Test
	public void deleteDir() throws IOException{
		Path dir = Files.createTempDirectory("DIR");		
		Path file = createTempFile(dir, "FILE", ".tmp");
		assertThat(exists(dir), is(true));
		assertThat(exists(file), is(true));
		
		FileUtils.delete(dir);
		
		assertThat(exists(dir), is(false));
		assertThat(exists(file), is(false));
	}
	
	@Test
	public void deleteTheWholeDir() throws IOException{
		Path parentdir = Files.createTempDirectory("DIR");		
		Path parentdirfile = createTempFile(parentdir, "FILE", ".tmp");
		Path subdir = Files.createTempDirectory(parentdir, "SUBDIR");		
		Path subdirfile = createTempFile(subdir, "SUBFILE", ".tmp");
		
		assertThat(exists(parentdir), is(true));
		assertThat(exists(parentdirfile), is(true));
		assertThat(exists(subdir), is(true));
		assertThat(exists(subdirfile), is(true));

		FileUtils.delete(parentdir);
		
		assertThat(exists(parentdir), is(false));
		assertThat(exists(parentdirfile), is(false));
		assertThat(exists(subdir), is(false));
		assertThat(exists(subdirfile), is(false));
	}
	
	
	@Test
	public void deleteSomeFiles() throws IOException{
		Path parentdir = Files.createTempDirectory("DIR");		
		Path parentdirfile = createTempFile(parentdir, "FILE", ".tmp");
		Path parentdirfileToDelete = createTempFile(parentdir, "FILE_TO_DELETE", ".delete");
		Path subdir = Files.createTempDirectory(parentdir, "SUBDIR");		
		Path subdirfile = createTempFile(subdir, "SUBFILE", ".tmp");
		Path subdirfileToDelete = createTempFile(subdir, "SUBFILE_TO_DELETE", ".delete");
		
		assertThat(exists(parentdir), is(true));
		assertThat(exists(parentdirfile), is(true));
		assertThat(exists(parentdirfileToDelete), is(true));
		assertThat(exists(subdir), is(true));
		assertThat(exists(subdirfile), is(true));
		assertThat(exists(subdirfileToDelete), is(true));		

		FileUtils.delete(parentdir, "*.delete");
		
		assertThat(exists(parentdir), is(true));
		assertThat(exists(parentdirfile), is(true));
		assertThat(exists(subdir), is(true));
		assertThat(exists(subdirfile), is(true));
		
		assertThat(exists(parentdirfileToDelete), is(false));
		assertThat(exists(subdirfileToDelete), is(false));
		
		// cleanup
		Files.deleteIfExists(subdirfile);
		Files.deleteIfExists(subdir);
		Files.deleteIfExists(parentdirfile);
		Files.deleteIfExists(parentdir);		
	}
	
	@Test
	public void deleteSomeDirs() throws IOException{
		Path parentdir = Files.createTempDirectory("DIR");		
		Path parentdirfile = createTempFile(parentdir, "FILE", ".tmp");
		Path subdir = Files.createTempDirectory(parentdir, "SUBDIR");		
		Path subdirfile = createTempFile(subdir, "SUBFILE", ".tmp");
		Path subdirToDelete = Files.createTempDirectory(parentdir, "delete.SUBDIR_TO_DELETE");
		Path subdirfileToDelete = createTempFile(subdirToDelete, "SUBFILE_TO_DELETE", ".tmp");
		
		assertThat(exists(parentdir), is(true));
		assertThat(exists(parentdirfile), is(true));
		assertThat(exists(subdir), is(true));
		assertThat(exists(subdirfile), is(true));
		assertThat(exists(subdirToDelete), is(true));
		assertThat(exists(subdirfileToDelete), is(true));		
		

		FileUtils.delete(parentdir, "delete.*");
		
		assertThat(exists(parentdir), is(true));
		assertThat(exists(parentdirfile), is(true));
		assertThat(exists(subdir), is(true));
		assertThat(exists(subdirfile), is(true));

		assertThat(exists(subdirToDelete), is(false));
		assertThat(exists(subdirfileToDelete), is(false));
		
		// cleanup
		Files.deleteIfExists(subdirfile);
		Files.deleteIfExists(subdir);
		Files.deleteIfExists(parentdirfile);
		Files.deleteIfExists(parentdir);
	}

	@Test
	public void copyFileToDir() throws Exception {
		Path file = createTempFile("FILE", ".tmp");
		Path targetDir = Files.createTempDirectory("DIR");
		
		FileUtils.copy(file, targetDir);
		Path targetFile = PathUtils.get(targetDir, file.getFileName());
		assertThat(exists(targetFile), is(true));
		
		// cleanup		
		FileUtils.delete(file);
		assertThat(exists(file), is(false));
		FileUtils.delete(targetDir);
		assertThat(exists(targetDir), is(false));
	}
	
	@Test
	public void copyFile() throws Exception {
		Path file = createTempFile("FILE", ".tmp");
		Path targetDir = Files.createTempDirectory("DIR");

		Path targetFile = PathUtils.get(targetDir, file.getFileName());
		FileUtils.copy(file, targetFile);
		assertThat(exists(targetFile), is(true));
		
		// cleanup		
		FileUtils.delete(file);
		assertThat(exists(file), is(false));
		FileUtils.delete(targetDir);
		assertThat(exists(targetDir), is(false));
	}
	
	@Test
	public void copyFileToNonExistingDir() throws Exception {
		Path file = createTempFile("FILE", ".tmp");
		Path tempDir = Files.createTempDirectory("DIR");
		try {	
			Path targetDir = PathUtils.get(tempDir, "TO_BE_CREATED");	
			Path targetFile = PathUtils.get(targetDir, file.getFileName());
			FileUtils.copy(file, targetFile);
		} catch (Exception e) {
			assertThat(e.getCause(), instanceOf(NoSuchFileException.class));
		} finally {		
			// cleanup		
			FileUtils.delete(file);
			assertThat(exists(file), is(false));
			FileUtils.delete(tempDir);
			assertThat(exists(tempDir), is(false));
		}
	}
		
	@Test
	public void copyDirWhenTargetDoesNotExists() throws Exception{
		Path sourceDir = Files.createTempDirectory("SOURCE_DIR");
		Path file = FileUtils.createFile(sourceDir, "file.txt");
		
		Path tempTargetDir = Files.createTempDirectory("TARGET_DIR");		
		Path targetDir = PathUtils.get(tempTargetDir, "TO_BE_CREATED");		
		FileUtils.copy(sourceDir, targetDir); // deve também criar o diretório targetDir
		Path targetFile = PathUtils.get(targetDir, file.getFileName());
		assertThat(exists(targetFile), is(true));
		
		// cleanup		
		FileUtils.delete(sourceDir);
		assertThat(exists(sourceDir), is(false));
		FileUtils.delete(tempTargetDir);
		assertThat(exists(tempTargetDir), is(false));
	}
	
	@Test
	public void copySomeDirs() throws Exception{
		Path parentdir = Files.createTempDirectory("DIR");		
		Path parentdirfile = createTempFile(parentdir, "FILE", ".tmp");
		Path subdir = Files.createTempDirectory(parentdir, "SUBDIR");		
		Path subdirfile = createTempFile(subdir, "SUBFILE", ".tmp");
		Path subdirToCopy = Files.createTempDirectory(parentdir, "SUBDIR_TO_COPY");
		Path subdirfileToCopy = createTempFile(subdirToCopy, "SUBFILE_TO_COPY", ".tmp");
		
		Path tempTargetDir = Files.createTempDirectory("TARGET_DIR");		
		Path targetDir = PathUtils.get(tempTargetDir, "TO_BE_CREATED");		
		
		FileUtils.copy(parentdir, targetDir, "*TO_COPY*"); 
		
		Path targetParentdirfile = PathUtils.resolve(parentdirfile, parentdir, targetDir);		
		assertThat(exists(targetParentdirfile), is(false));
		Path targetSubdirfile = PathUtils.resolve(subdirfile, parentdir, targetDir);		
		assertThat(exists(targetSubdirfile), is(false));
		Path targetSubdirfileToCopy = PathUtils.resolve(subdirfileToCopy, parentdir, targetDir);
		assertThat(exists(targetSubdirfileToCopy), is(true));		
		
		// cleanup		
		FileUtils.delete(parentdir);
		assertThat(exists(parentdir), is(false));
		FileUtils.delete(tempTargetDir);
		assertThat(exists(tempTargetDir), is(false));
	}
	
	@Test
	public void ls() throws Exception {
		Path parentdir = Files.createTempDirectory("DIR");	
		Path parentdirfile1 = createTempFile(parentdir, "FILE_1", ".tmp");
		Path parentdirfile2 = createTempFile(parentdir, "FILE_2", ".tmp");
		
		List<Path> files = FileUtils.ls(parentdir);
		assertThat(files.size(), is(2));
		assertThat(files, hasItem(parentdirfile1));		
		assertThat(files, hasItem(parentdirfile2));
	}
	
	@Test
	public void deletDir() throws IOException {
		Path parentdir = Files.createTempDirectory("DIR");	
		Path parentdirfile1 = createTempFile(parentdir, "FILE_1", ".tmp");
		Path parentdirfile2 = createTempFile(parentdir, "FILE_2", ".tmp");
		
		FileUtils.deleteDir(parentdir, new DeleteVisitor("*.tmp"));
		
		assertThat(exists(parentdir), is(true));
		assertThat(exists(parentdirfile1), is(false));
		assertThat(exists(parentdirfile2), is(false));	
	}
	

	@Test
	public void deletComplexDir() throws IOException {
		Path parentdir = Files.createTempDirectory("DIR");	
		Path subdir = Files.createTempDirectory(parentdir, "SUBDIR");	
		Path parentdirfile1 = createTempFile(parentdir, "FILE_1", ".tmp");
		Path parentdirfile2 = createTempFile(parentdir, "FILE_2", ".tmp");
		Path parentdirfile3 = createTempFile(subdir, "FILE_3", ".tmp");
		Path parentdirfile4 = createTempFile(subdir, "FILE_4", ".tmp");
		
		FileUtils.deleteDir(parentdir, new DeleteVisitor("*.tmp"));
		
		assertThat(exists(parentdir), is(true));
		assertThat(exists(subdir), is(true));
		assertThat(exists(parentdirfile1), is(false));
		assertThat(exists(parentdirfile2), is(false));
		assertThat(exists(parentdirfile3), is(false));
		assertThat(exists(parentdirfile4), is(false));	
	}
	
	@Test
	public void createDirectories() throws IOException {
		Path parentdir = Files.createTempDirectory("DIR");	
		Path createDir = PathUtils.get(parentdir, "/test");
		
		FileUtils.createDirectories(createDir);
		
		assertThat(exists(createDir), is(true));
	}
	
	@Test
	public void createComplexDirectories() throws IOException {
		Path parentdir = Files.createTempDirectory("DIR");	
		Path createDir = PathUtils.get(parentdir, "/test/test/test");
		
		FileUtils.createDirectories(createDir);
		
		assertThat(exists(createDir), is(true));
	}
	
	@Test
	public void read() throws IOException {
		final String writeHello = "Hello Test!!";
		Path parentdir = Files.createTempDirectory("DIR");	
		Path parentdirfile1 = createTempFile(parentdir, "FILE_1", ".tmp");
		Files.write(parentdirfile1, writeHello.getBytes());
		String readReturn = FileUtils.read(parentdirfile1);
		
		assertThat(readReturn.equals(writeHello), is(true));
	}
	
	@Test
	public void copyReplaceExisting() throws IOException {
		final String fileOne = "File one!!";
		Path parentdir = Files.createTempDirectory("DIR");
		Path from = Files.createTempDirectory("from");
		Path parentdirfile1 = FileUtils.createFile(from, "FILE_1.tmp");
		Files.write(parentdirfile1, fileOne.getBytes());
		Path target = Files.createTempDirectory(parentdir, "target");
		Path parentdirfile2 = FileUtils.createFile(target, "FILE_1.tmp");
		Files.write(parentdirfile2, "File two!!".getBytes());
		
		FileUtils.copy(from, target);
		String fileTwo = FileUtils.read(parentdirfile2);
		
		assertThat(exists(parentdirfile1), is(true));
		assertThat(fileOne.endsWith(fileTwo), is(true));
	}
	
	@Test
	public void copyFileReplaceExisting() throws IOException {
		final String fileOne = "File one!!";
		Path parentdir = Files.createTempDirectory("DIR");
		Path parentdirfile1 = FileUtils.createFile(parentdir, "FILE_1.tmp");
		Files.write(parentdirfile1, fileOne.getBytes());
		Path parentdirfile2 = FileUtils.createFile(parentdir, "FILE_2.tmp");
		Files.write(parentdirfile2, "File two!!".getBytes());
		
		FileUtils.copyFile(parentdirfile1, parentdirfile2);
		String fileTwo = FileUtils.read(parentdirfile2);
		
		assertThat(exists(parentdirfile1), is(true));
		assertThat(fileOne.endsWith(fileTwo), is(true));
	}
	
	
//	
//	@Test
//	public void copyDirAndKeepExistingFileAttributes() {
//		Assert.fail("Implementar...");
//	}
//		
}

