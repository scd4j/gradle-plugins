package com.datamaio.scd4j.util.io;

import static com.datamaio.scd4j.cmd.Command.isWindows;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
		Path dirToBkp = null;
		
		if(isWindows()) {
			FileUtils.createDirectories(Paths.get("/tmpUnit"));
			dirToBkp = Files.createTempDirectory(Paths.get("/tmpUnit"), "dirToBkp");
		} else {
			dirToBkp =  Files.createTempDirectory("dirToBkp");
		}
		
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
		Path dirToBkp = null;
		
		if(isWindows()) {
			FileUtils.createDirectories(Paths.get("/tmpUnit"));
			dirToBkp = Files.createTempDirectory(Paths.get("/tmpUnit"), "dirToBkp2");
		} else {
			dirToBkp =  Files.createTempDirectory("dirToBkp2");
		}
		
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
		Path dirToBkp = null;
		
		if(isWindows()) {
			FileUtils.createDirectories(Paths.get("/tmpUnit"));
			dirToBkp = Files.createTempDirectory(Paths.get("/tmpUnit"), "dirToBkp3");
		} else {
			dirToBkp =  Files.createTempDirectory("dirToBkp2");
		}
		
		Path parentdirfile1 = createTempFile(dirToBkp, "FILE_1", ".tmp");
		
		new BackupHelper(conf).backupFile(parentdirfile1);
		Path bkp = PathUtils.get(conf.getBackupDir(), parentdirfile1);
		assertThat(exists(bkp), is(true));
	}
}
