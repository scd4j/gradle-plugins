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
package com.datamaio.scd4j.hooks;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.datamaio.junit.IsCentos;
import com.datamaio.junit.IsLinux;
import com.datamaio.junit.IsUbuntu;
import com.datamaio.junit.IsWindows;
import com.datamaio.junit.RunIfRule;
import com.datamaio.junit.RunIfRule.RunIf;
import com.datamaio.scd4j.cmd.CentosCommand;
import com.datamaio.scd4j.cmd.UbuntuCommand;
import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.hooks.module.ModuleHook;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;

@RunWith(MockitoJUnitRunner.class)
public class HookBasicsTest {

	@Rule
	public RunIfRule rule = new RunIfRule();
	
	@Mock
	private Configuration conf;
	@Spy
	private Hook hook = new ModuleHook() {		
		@Override
		public Object run() {
			return null;
		}
	};

	@Before
	public void setUp() throws Exception {		
		hook.setConf(conf);
	}
	
	@Test @RunIf(IsLinux.class)
	public void osnameOnLinux(){
		assertThat(hook.osname(), is("Linux"));
	}
	
	@Test @RunIf(IsWindows.class)
	public void osnameOnWindows(){
		assertThat(hook.osname(), is("Windows"));
	}
	
	@Test @RunIf(IsLinux.class)
	public void isLinuxOnLinux(){
		assertThat(hook.isLinux(), is(true));
	}
	
	@Test @RunIf(IsWindows.class)
	public void isLinuxOnWindows(){
		assertThat(hook.isLinux(), is(false));
	}

	@Test @RunIf(IsLinux.class)
	public void isWindowsOnLinux(){
		assertThat(hook.isWindows(), is(false));
	}
	
	@Test @RunIf(IsWindows.class)
	public void isWindowsOnWindows(){
		assertThat(hook.isWindows(), is(true));
	}
	
	@Test @RunIf(IsCentos.class)
	public void distributionOnCentos(){
		assertThat(hook.distribution(), is(CentosCommand.DIST_NAME));
	}
	
	@Test @RunIf(IsUbuntu.class)
	public void distributionOnUbuntu(){
		assertThat(hook.distribution(), is(UbuntuCommand.DIST_NAME));
	}
	
	@Test @RunIf(IsWindows.class)
	public void distributionOnWindows(){
		assertThat(hook.distribution(), is(System.getProperty("os.name")));
	}
	
	/**
	 * If you do not have dos2unix installed, this method will try to install, so.. you need to run with root
	 */
	@Test @RunIf(IsLinux.class)
	public void fixTextOnLinux() throws Exception {
		Path tmp = Files.createDirectory(Paths.get("tmp"));
		try {
			Path file = PathUtils.get(tmp, "file.txt");
			Files.write(file, "first line\r\nsecond line\r\n".getBytes());
		    hook.fixText(file.toString()); 
			String result = FileUtils.read(file);
			assertThat(result, is("first line\nsecond line\n"));
		} finally {
			FileUtils.delete(tmp);
		}
	}
	
	@Test @RunIf(IsWindows.class)
	public void fixTextWindows() throws Exception {
		Path tmp = Files.createDirectory(Paths.get("tmp"));
		try {
			Path file = PathUtils.get(tmp, "file.txt");
			Files.write(file, "first line\nsecond line\n".getBytes());
		    hook.fixText(file.toString()); 
			String result = FileUtils.read(file);
			assertThat(result, is("first line\r\nsecond line\r\n"));
		} finally {
			FileUtils.delete(tmp);
		}
	}
	
	@Test
	public void whoami() {
		assertThat(hook.whoami(), is(System.getProperty("user.name")));
	}
	
	@Test
	public void fileDirExists() throws Exception{
		Path dir = Files.createDirectory(Paths.get("tmp"));
		try {
			Path file = PathUtils.get(dir, "file.txt");
			Files.write(file, "aa".getBytes());
			assertThat(hook.exists(dir.toString()), is(true));
			assertThat(hook.exists(file.toString()), is(true));
		} finally {
			FileUtils.delete(dir);
		}
	}

	@Test
	public void fileDirNotExists() throws Exception{
		Path dir = Files.createDirectory(Paths.get("tmp"));
		try {
			Path file = PathUtils.get(dir, "file.txt");
			Files.write(file, "aa".getBytes());
			assertThat(hook.exists(PathUtils.get(dir, "dir2").toString()), is(false));
			assertThat(hook.exists(PathUtils.get(dir, "file2.txt").toString()), is(false));
		} finally {
			FileUtils.delete(dir);
		}
	}
	
	@Test
	public void mkdir() throws Exception{
		Path tmp = Files.createDirectory(Paths.get("tmp"));
		try {
			Path dir = PathUtils.get(tmp, "dir");
			hook.mkdir(dir.toString());
			assertThat(Files.exists(dir), is(true));
			assertThat(Files.isDirectory(dir), is(true));
			assertThat(Files.isRegularFile(dir), is(false));
			
			Path dir2 = PathUtils.get(tmp, "dir2", "another2", "onemore2");
			hook.mkdir(dir2.toString());
			assertThat(Files.exists(dir2), is(true));
			assertThat(Files.isDirectory(dir2), is(true));
		} finally {
			FileUtils.delete(tmp);
		}
	}
	
	@Test
	public void rmFile()  throws Exception{
		Path dir = Files.createDirectory(Paths.get("tmp"));
		try {
			Path file = PathUtils.get(dir, "file.txt");
			Files.write(file, "aa".getBytes());
			assertThat(hook.exists(file.toString()), is(true));			
			hook.rm(file.toString());
			assertThat(hook.exists(file.toString()), is(false));
			
		} finally {
			FileUtils.delete(dir);
		}
	}
	
	@Test
	public void rmDir()  throws Exception{
		Path dir = Files.createDirectory(Paths.get("tmp"));
		try {
			Path file = PathUtils.get(dir, "file.txt");
			Files.write(file, "aa".getBytes());
			assertThat(hook.exists(file.toString()), is(true));
			
			Path subDir = PathUtils.get(dir, "dir2", "another2");
			FileUtils.createDirectories(subDir);
			assertThat(hook.exists(subDir.toString()), is(true));
			Path file2 = PathUtils.get(dir, "file2.txt");
			Files.write(file2, "aa".getBytes());
			assertThat(hook.exists(file2.toString()), is(true));
			
			hook.rm(dir.toString());
			assertThat(hook.exists(file.toString()), is(false));
			assertThat(hook.exists(file2.toString()), is(false));
			assertThat(hook.exists(dir.toString()), is(false));
			
		} finally {
			FileUtils.delete(dir);
		}
	}

	
	@Test
	public void moveFileToDir() throws Exception {
		Path file = createTempFile("FILE", ".tmp");
		Path targetDir = Files.createTempDirectory("DIR");
		
		try {
			Path targetFile = PathUtils.get(targetDir, file.getFileName());
			assertThat(exists(file), is(true));
			assertThat(exists(targetFile), is(false));
			hook.mv(file.toString(), targetDir.toString());
			assertThat(exists(targetFile), is(true));
		} finally {
			FileUtils.delete(file);
			assertThat(exists(file), is(false));			
			FileUtils.delete(targetDir);
			assertThat(exists(targetDir), is(false));
		}
	}
	
	@Test
	public void moveFileToFile() throws Exception {
		Path file = createTempFile("FILE", ".tmp");
		Path tempDir = Files.createTempDirectory("DIR");

		try {
			Path targetFile = PathUtils.get(tempDir, file.getFileName());
			hook.mv(file.toString(), targetFile.toString());			
			assertThat(exists(targetFile), is(true));			
		} finally {
			FileUtils.delete(file);
			assertThat(exists(file), is(false));
			FileUtils.delete(tempDir);
			assertThat(exists(tempDir), is(false));
		}
	}
	
	@Test
	public void moveFileToNonExistingDir() throws Exception {
		Path file = createTempFile("FILE", ".tmp");
		Path tempDir = Files.createTempDirectory("DIR");
		try {	
			Path targetDir = PathUtils.get(tempDir, "TO_BE_CREATED");	
			Path targetFile = PathUtils.get(targetDir, file.getFileName());
			assertThat(exists(file), is(true));
			assertThat(exists(targetDir), is(false));
			assertThat(exists(targetFile), is(false));
			hook.mv(file.toString(), targetFile.toString());
			assertThat(exists(targetFile), is(true));	
		} finally {		
			FileUtils.delete(file);
			assertThat(exists(file), is(false));			
			FileUtils.delete(tempDir);
			assertThat(exists(tempDir), is(false));
		}
	}

	@Test
	public void moveDirToNonExistingDir() throws Exception {
		Path parentdir = Files.createTempDirectory("DIR_ROOT");
		Path file = FileUtils.createFile(parentdir, "FILE_1.tmp");
		Path tempDir = Files.createTempDirectory("DIR");
		try {	
			Path targetDir = PathUtils.get(tempDir, "TO_BE_CREATED");	
			Path targetFile = PathUtils.get(targetDir, file.getFileName());
			assertThat(exists(file), is(true));
			assertThat(exists(targetDir), is(false));
			assertThat(exists(targetFile), is(false));
			hook.mv(file.getParent().toString(), targetDir.toString());
			assertThat(exists(targetFile), is(true));	
		} finally {		
			FileUtils.delete(file);
			assertThat(exists(file), is(false));			
			FileUtils.delete(tempDir);
			assertThat(exists(tempDir), is(false));
		}
	}

	@Test
	public void copyFileToDir() throws Exception {
		Path file = createTempFile("FILE", ".tmp");
		Path targetDir = Files.createTempDirectory("DIR");
		
		try {
			Path targetFile = PathUtils.get(targetDir, file.getFileName());
			assertThat(exists(targetFile), is(false));
			hook.cp(file.toString(), targetDir.toString());
			assertThat(exists(targetFile), is(true));
		} finally {		
			FileUtils.delete(file);
			assertThat(exists(file), is(false));
			FileUtils.delete(targetDir);
			assertThat(exists(targetDir), is(false));
		}
	}
	
	@Test
	public void copyFile() throws Exception {
		Path file = createTempFile("FILE", ".tmp");
		Path targetDir = Files.createTempDirectory("DIR");

		try {
			Path targetFile = PathUtils.get(targetDir, file.getFileName());
			hook.cp(file.toString(), targetFile.toString());
			assertThat(exists(targetFile), is(true));
		} finally {
			FileUtils.delete(file);
			assertThat(exists(file), is(false));
			FileUtils.delete(targetDir);
			assertThat(exists(targetDir), is(false));
		}
	}
	
	@Test
	public void copyFileToNonExistingDir() throws Exception {
		Path file = createTempFile("FILE", ".tmp");
		Path tempDir = Files.createTempDirectory("DIR");
		try {	
			Path targetDir = PathUtils.get(tempDir, "TO_BE_CREATED");	
			hook.cp(file.toString(), targetDir.toString());
			
			Path targetFile = PathUtils.get(targetDir, file.getFileName());
			assertThat(exists(targetFile), is(true));
		} finally {		
			// cleanup		
			FileUtils.delete(file);
			assertThat(exists(file), is(false));
			FileUtils.delete(tempDir);
			assertThat(exists(tempDir), is(false));
		}
	}
		
	@Test
	public void ls() throws Exception {
		Path parentdir = Files.createTempDirectory("DIR");	
		try {
			Path parentdirfile1 = createTempFile(parentdir, "FILE_1", ".tmp");
			Path parentdirfile2 = createTempFile(parentdir, "FILE_2", ".tmp");
			
			List<String> files = hook.ls(parentdir.toString());
			assertThat(files.size(), is(2));
			assertThat(files, hasItem(parentdirfile1.toString()));		
			assertThat(files, hasItem(parentdirfile2.toString()));
		} finally {
			FileUtils.delete(parentdir);
			assertThat(exists(parentdir), is(false));			
		}
	}

}
