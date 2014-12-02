package com.datamaio.scd4j.cmd;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.datamaio.junit.IsWindows;
import com.datamaio.junit.RunIfRule.RunIf;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;


/**
 * <p>This class is responsable for Unit Tests for the class {@link Command}</p>
 * 
 * @author Mateus M. da Costa
 */
public class CommandTest {
	private Path root;

	@Before
	public void setup() throws IOException {
		root = Files.createTempDirectory("ROOT_DIR");
	}

	@After
	public void teardown() throws IOException {
		FileUtils.delete(root);
	}

	@Test
	public void mkdir() throws IOException {
		Path createDir = PathUtils.get(root, "/test");
		Command.get().mkdir(createDir.toString());
		assertThat(exists(createDir), is(true));
	}

	@Test
	public void mkdirComplexDir() throws IOException {
		Path createDir = PathUtils.get(root, "/test/test/test");
		Command.get().mkdir(createDir.toString());
		assertThat(exists(createDir), is(true));
	}

	@Test
	public void ls() throws Exception {
		Path parentdirfile1 = createTempFile(root, "FILE_1", ".tmp");
		Path parentdirfile2 = createTempFile(root, "FILE_2", ".tmp");

		List<String> files = Command.get().ls(root.toString());
		assertThat(files.size(), is(2));
		assertThat(files, hasItem(parentdirfile1.toString()));
		assertThat(files, hasItem(parentdirfile2.toString()));
	}

	@Test
	public void rmDir() throws Exception {
		Path rmDir = Files.createTempDirectory(root, "rmDir");
		Path parentdirfile1 = createTempFile(rmDir, "FILE_1", ".tmp");
		Path parentdirfile2 = createTempFile(rmDir, "FILE_2", ".tmp");
		Command.get().rm(rmDir.toString());

		assertThat(exists(rmDir), is(false));
		assertThat(exists(parentdirfile1), is(false));
		assertThat(exists(parentdirfile2), is(false));
	}

	@Test
	public void rmFile() throws Exception {
		Path rmDir = Files.createTempDirectory(root, "rmDir");
		Path parentdirfile1 = createTempFile(rmDir, "FILE_1", ".tmp");
		Path parentdirfile2 = createTempFile(rmDir, "FILE_2", ".tmp");
		Command.get().rm(parentdirfile1.toString());

		assertThat(exists(rmDir), is(true));
		assertThat(exists(parentdirfile1), is(false));
		assertThat(exists(parentdirfile2), is(true));
	}

	@Test
	public void rmInvalidDir() throws Exception {
		Path rmDir = Files.createTempDirectory(root, "rmDir");
		Command.get().rm(root.toString() + File.separator + "rmDir1");
		assertThat(exists(rmDir), is(true));
		assertThat(exists(root), is(true));
	}

	@Test
	public void cpFile() throws Exception {
		Path fromDir = Files.createTempDirectory(root, "fromDir");
		Path cpfile = createTempFile(fromDir, "FILE_1", ".tmp");
		Path toDir = Files.createTempDirectory(root, "fromDir");

		Command.get().cp(cpfile.toString(), toDir.toString());
		assertThat(exists(fromDir), is(true));
		assertThat(exists(cpfile), is(true));
		assertThat(exists(toDir), is(true));
		assertThat(exists(PathUtils.get(toDir, cpfile.getFileName())), is(true));
	}

	@Test
	public void cpDir() throws Exception {
		Path fromDir = Files.createTempDirectory(root, "fromDir");
		Path file1 = createTempFile(fromDir, "FILE_1", ".tmp");
		Path file2 = createTempFile(fromDir, "FILE_2", ".tmp");
		Path toDir = Files.createTempDirectory(root, "fromDir");

		Command.get().cp(fromDir.toString(), toDir.toString());
		assertThat(exists(fromDir), is(true));
		assertThat(exists(file1), is(true));
		assertThat(exists(file2), is(true));
		assertThat(exists(toDir), is(true));
		assertThat(exists(PathUtils.get(toDir, file1.getFileName())), is(true));
		assertThat(exists(PathUtils.get(toDir, file2.getFileName())), is(true));
	}
	
	@Test
	public void exist() throws Exception {
		Path fromDir = Files.createTempDirectory(root, "fromDir");
		assertThat(Command.get().exists(fromDir.toString()), is(true));
	}
	
	@Test
	public void noExist() throws Exception {
		Path fromDir = Files.createTempDirectory(root, "fromDir");
		assertThat(Command.get().exists(fromDir.toString()+File.separator+"invalid"), is(false));
	}

	@Test
	public void mvFile() throws Exception {
		// Path fromDir = Files.createTempDirectory(root, "fromDir");
		// Path cpfile = createTempFile(fromDir, "FILE_1", ".tmp");
		// Path toDir = Files.createTempDirectory(root, "fromDir");
		//
		// new WindowsCommand().mv(cpfile.toString(), toDir.toString());
		// assertThat(exists(fromDir), is(true));
		// assertThat(exists(cpfile), is(false));
		// assertThat(exists(toDir), is(true));
		// assertThat(exists(PathUtils.get(toDir, cpfile.getFileName())),
		// is(true));
	}

}
