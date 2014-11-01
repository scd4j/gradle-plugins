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

package com.datamaio.fwk.io;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Test;

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
//	
//	@Test
//	public void copyDirAndKeepExistingFileAttributes() {
//		Assert.fail("Implementar...");
//	}
//	

	
}

