package com.datamaio.scd4j.cmd;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.datamaio.junit.IsWindows;
import com.datamaio.junit.RunIfRule.RunIf;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;
import com.datamaio.scd4j.util.io.ZipUtilsTest;

/**
 * <p>This class is responsable for Unit Tests on Windows of the classes {@link Command} and {@link WindowsCommand}</p>
 * 
 * @author Mateus M. da Costa
 */
public class WindowsCommandTest {
	private Path root;
	
	@Before
	public void setup() throws IOException {
		root = Files.createTempDirectory("ROOT_DIR");	
	}
	
	@After
	public void teardown() throws IOException{
		 FileUtils.delete(root);
	}
	
	@Test
	@RunIf(IsWindows.class)
	public void get() throws Exception {
		Command command = Command.get();
		assertThat(command instanceof WindowsCommand, is(true));
	}
	
	@Test
	@RunIf(IsWindows.class)
	public void unzip() throws Exception {
		URL url = ZipUtilsTest.class.getResource("/com/datamaio/fwk/io/zip2test.zip");
		Path zipFile = Paths.get(url.toURI());
		Path targetDir = Files.createTempDirectory(root, "DIR");
		Command.get().unzip(zipFile.toString(), targetDir.toString());
		Path img = PathUtils.get(targetDir, "/dir/subdir/img.jpg");
		assertThat(exists(img), is(true));
	}
}
