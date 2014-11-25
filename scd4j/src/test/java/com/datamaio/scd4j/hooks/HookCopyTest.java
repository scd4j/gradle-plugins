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

import static com.datamaio.scd4j.cmd.Command.isWindows;
import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;

/**
 * 
 * @author Fernando Rubbo
 */
@RunWith(MockitoJUnitRunner.class)
public class HookCopyTest {

	private static Path root;
	
	@Mock
	Configuration conf;
	@Spy
	private Hook hook = new Hook() {		
		@Override
		public Object run() {
			return null;
		}
	};

	@BeforeClass
	public static void beforeClass() throws Exception {
		root = createEnv();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		FileUtils.delete(root);
	}
	
	@Before
	public void setUp() throws Exception {
		Path zip = PathUtils.get(root, "linux/foo.zip");
		when(conf.getDependency("foo.zip")).thenReturn(zip);
		when(conf.getDependency("foo@zip")).thenReturn(zip);
		
		hook.setConf(conf);
	}

	@After
	public void teardown() throws Exception {
		reset(hook);
	}
	
	@Test
	public void testCopyInvalidFile() throws Exception {
		try {
			hook.cp("/file/do/not/exist.txt", "/dummy/folder");
			fail("Should not be able to copy a non exisnting file");
		} catch (RuntimeException e) {
			assertThat(e.getMessage(), is(equalTo("It was not possible to find file/dir '/file/do/not/exist.txt' to copy!")));
		}
	}
	
	@Test
	public void testCopyFile() throws Exception {
		Path zip = PathUtils.get(root, "linux/foo.zip");
		Path targetDir = createTempDir();
		Path targetFile = PathUtils.get(targetDir,"foo.zip");
		
		try {
			assertThat(exists(targetFile), is(false));
			hook.cp(zip.toString(), targetDir.toString());
			assertThat(exists(targetFile), is(true));
		} finally {
			FileUtils.delete(targetDir);
			assertThat(exists(targetFile), is(false));
		}
	}
	
	@Test
	public void testCopyDir() throws Exception {
		Path targetDir = createTempDir();
		Path targetFile = PathUtils.get(targetDir, "linux", "foo.zip");
		
		try {
			assertThat(exists(targetFile), is(false));
			hook.cp(root.toString(), targetDir.toString());
			assertThat(exists(targetFile), is(true));
		} finally {
			FileUtils.delete(targetDir);
			assertThat(exists(targetFile), is(false));
		}
	}
	
	@Test
	public void testCopyLocalDependency() throws Exception {
		String depName = "foo.zip";
		Path targetDir = createTempDir();
		Path targetFile = PathUtils.get(targetDir,"foo.zip");

		try {
			assertThat(exists(targetFile), is(false));
			hook.cp(depName, targetDir.toString());
			assertThat(exists(targetFile), is(true));
		} finally {
			FileUtils.delete(targetDir);
			assertThat(exists(targetFile), is(false));
		}
		
	    verify(hook, times(1)).resolve(depName);
	}
		
	@Test
	public void testCopyRemoteDependency() throws Exception {
		String depName = "foo@zip";
		Path targetDir = createTempDir();
		Path targetFile = PathUtils.get(targetDir,"foo.zip");

		try {
			assertThat(exists(targetFile), is(false));
			hook.cp(depName, targetDir.toString());
			assertThat(exists(targetFile), is(true));
		} finally {
			FileUtils.delete(targetDir);
			assertThat(exists(targetFile), is(false));
		}
		
	    verify(hook, times(1)).resolve(depName);
	}
	
	@Test
	public void testCopyDynamicRemoteDependency() throws Exception {
		String depName = "foo";
		Path targetDir = createTempDir();
		Path targetFile = PathUtils.get(targetDir,"foo.zip");

		try {
			assertThat(exists(targetFile), is(false));
			hook.cp(depName, targetDir.toString());
			assertThat(exists(targetFile), is(true));
		} finally {
			FileUtils.delete(targetDir);
			assertThat(exists(targetFile), is(false));
		}
		
	    verify(hook, times(1)).resolve(depName);
	}
	
	private static Path createEnv() throws Exception{
		Path tmp = createTempDir();
		FileUtils.copy(Paths.get(HookInstallUninstallTest.class.getResource("/_packages").toURI()), tmp);		
		return tmp;
	}

	private static Path createTempDir() throws IOException {
		Path r = null;				
		if (isWindows()) {
			// FileUtils.createDirectories(Paths.get("/tmpUnit"));
			// Path tmpUnit = Files.createTempDirectory("tmpUnit");
			r = Files.createTempDirectory(/*Paths.get("/tmpUnit"),*/ "root");
		} else {
			r =  Files.createTempDirectory("root");
		}
		return r;
	}
}
