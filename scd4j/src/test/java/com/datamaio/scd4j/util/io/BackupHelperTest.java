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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.util.BackupHelper;

/**
 * This class is responsible for Unit tests on {@link BackupHelper}
 * @author Mateus M. da Costa
 */
@RunWith(MockitoJUnitRunner.class)
public class BackupHelperTest {
	
	@Mock
	Configuration conf;
	
	@Before
	public void setUp() throws Exception {
		Path bkpDir = Files.createTempDirectory("bkpDir");	
		when(conf.getBackupDir()).thenReturn(bkpDir);
	}
	
	@Test
	public void existingBackupDir() throws Exception {
		Path dirToBkp = Files.createTempDirectory("dirToBkp");
		
		FileUtils.createDirectories(PathUtils.get(conf.getBackupDir(), dirToBkp));
		Path parentdirfile1 = createTempFile(dirToBkp, "FILE_1", ".tmp");
		Path parentdirfile2 = createTempFile(dirToBkp, "FILE_2", ".tmp");
		
		new BackupHelper(conf).backupFileOrDir(dirToBkp);
		
		
		Path bkp = PathUtils.get(conf.getBackupDir(), dirToBkp);
		assertThat(exists(bkp), is(true));
		assertThat(exists(PathUtils.get(bkp, parentdirfile1.getFileName())), is(true));
		assertThat(exists(PathUtils.get(bkp, parentdirfile2.getFileName())), is(true));
	}
	
	@Test
	public void notExistingBackupDir() throws Exception {
		Path dirToBkp = Files.createTempDirectory("dirToBkp2");
				
		Path parentdirfile1 = createTempFile(dirToBkp, "FILE_1", ".tmp");
		Path parentdirfile2 = createTempFile(dirToBkp, "FILE_2", ".tmp");
		
		new BackupHelper(conf).backupFileOrDir(dirToBkp);
		
		Path bkp = PathUtils.get(conf.getBackupDir(), dirToBkp);
		assertThat(exists(bkp), is(true));
		assertThat(exists(PathUtils.get(bkp, parentdirfile1.getFileName())), is(true));
		assertThat(exists(PathUtils.get(bkp, parentdirfile2.getFileName())), is(true));
	}
	
	@Test
	public void backupFile() throws IOException {
		Path dirToBkp = Files.createTempDirectory("dirToBkp3");		
	
		Path parentdirfile1 = createTempFile(dirToBkp, "FILE_1", ".tmp");
		
		new BackupHelper(conf).backupFile(parentdirfile1);
		Path bkp = PathUtils.get(conf.getBackupDir(), parentdirfile1);
		assertThat(exists(bkp), is(true));
	}
	
	@Test
	public void backupComplexDir() throws IOException {
		Path dirToBkp = Files.createTempDirectory("dirToBkp4");	
			
		Path subDir1 = Files.createTempDirectory(dirToBkp, "subDir1");
		Path subDir2 = Files.createTempDirectory(dirToBkp, "subDir2");
		Path subDir3 = Files.createTempDirectory(subDir1, "subDir3");
		
		Path parentdirfile1 = createTempFile(dirToBkp, "FILE_1", ".tmp");
		Path parentdirfile2 = createTempFile(subDir1, "FILE_2", ".tmp");
		Path parentdirfile3 = createTempFile(subDir3, "FILE_3", ".tmp");
		
		new BackupHelper(conf).backupFileOrDir(dirToBkp);
		
		Path bkp = PathUtils.get(conf.getBackupDir(), dirToBkp);
		assertThat(exists(bkp), is(true));
		assertThat(exists(PathUtils.get(bkp, parentdirfile1.getFileName())), is(true));
		assertThat(exists(PathUtils.get(bkp, subDir1.getFileName())), is(true));
		assertThat(exists(PathUtils.get(bkp, subDir2.getFileName())), is(true));
		
		assertThat(exists(PathUtils.get(
				PathUtils.get(bkp, subDir1.getFileName()),
				parentdirfile2.getFileName())), is(true));
		assertThat(exists(PathUtils.get(
				PathUtils.get(bkp, subDir1.getFileName()),
				subDir3.getFileName())), is(true));
		assertThat(
				exists(PathUtils.get(PathUtils.get(
						PathUtils.get(bkp, subDir1.getFileName()),
						subDir3.getFileName()), parentdirfile3.getFileName())),
				is(true));	
	}
}
