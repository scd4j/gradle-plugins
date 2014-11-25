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

import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;
import com.datamaio.scd4j.util.io.ZipUtils;

/**
 * 
 * @author Fernando Rubbo
 */
public class ZipUtilsTest {
	@Test
	public void unzip() throws Exception {		
		URL url = ZipUtilsTest.class.getResource("/com/datamaio/fwk/io/zip2test.zip");
		Path zipFile = Paths.get(url.toURI());
		Path targetDir = Files.createTempDirectory("DIR");
		
		ZipUtils.unzip(zipFile, targetDir);	
		Path img = PathUtils.get(targetDir, "/dir/subdir/img.jpg");
		assertThat(exists(img), is(true));
		
		// cleanup		
		FileUtils.delete(targetDir);
	}
	
	@Test
	public void zip() throws Exception {		
		Path dir = Files.createTempDirectory("DIR");
		Path file = Files.createTempFile(dir, "TEMP", ".txt");
		Path targetDir = Files.createTempDirectory("TARGET");
		Path zipFile = PathUtils.get(targetDir, "my.zip");
		
		ZipUtils.create(zipFile, dir, file);	
		assertThat(exists(zipFile), is(true));
		List<String> list = ZipUtils.list(zipFile);
		assertThat(list.size(), is(1));
		assertThat(list, hasItem(endsWith(file.getFileName().toString())));
		
		// cleanup
		FileUtils.delete(dir);
		FileUtils.delete(targetDir);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void zipWithIllegalParameters() throws IOException {
		Path dir = Files.createTempDirectory("DIR");
		Path dir2 = Files.createTempDirectory("DIR2");
		Path file2 = Files.createTempFile(dir2, "TEMP", ".txt");

		try {	
			Path zipFile = Paths.get("my.zip");
			ZipUtils.create(zipFile, dir, file2);	
		} finally {
			// cleanup
			FileUtils.delete(dir);
			FileUtils.delete(dir2);
		}
	}
	
	@Test
	public void zipDir() throws IOException {
		Path dir = Files.createTempDirectory("DIR");
		Path file = Files.createTempFile(dir, "TEMP", ".txt");
		Path subDir = Files.createTempDirectory(dir, "SUB_DIR");
		Path subFile = Files.createTempFile(subDir, "TEMP", ".txt");
		Path targetDir = Files.createTempDirectory("TARGET");
		Path zipFile = PathUtils.get(targetDir, "my.zip");
		
		ZipUtils.create(zipFile, dir);	
		assertThat(exists(zipFile), is(true));
		List<String> list = ZipUtils.list(zipFile);
		assertThat(list.size(), is(3));
		assertThat(list, hasItem(endsWith(file.getFileName().toString())));
		assertThat(list, hasItem(endsWith(subFile.getFileName().toString())));
		
		// cleanup
		FileUtils.delete(dir);
		FileUtils.delete(targetDir);
	}
	
	@Test
	public void createComplexZip() throws IOException {
		Path dir = Files.createTempDirectory("DIR");
		Path toZip = Files.createTempDirectory(dir, "toZip");
		Path file1 = Files.createTempFile(toZip, "TEMP1", ".txt");
		Files.createTempFile(toZip, "TEMP2", ".txt");
		Files.createTempFile(toZip, "TEMP3", ".txt");
		ZipUtils.create(toZip.getParent().toString()+"/my.zip", toZip.getParent().toString()+"/"+toZip.getFileName(), file1.toString());
		
		Path zipFile = PathUtils.get(toZip.getParent(), "my.zip");
		List<String> list = ZipUtils.list(zipFile);
		assertThat(list.size(), is(1));
		assertThat(list, hasItem(endsWith(file1.getFileName().toString())));
	}
}
