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

package com.datamaio.scd4j;

import static com.datamaio.scd4j.cmd.Command.isWindows;
import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.datamaio.scd4j.util.LogHelper;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;

/**
 * @author Fernando Rubbo
 * @author Mateus M. da Costa
 */
public class EnvConfiguratorTest {
	
	@After
	public void teardown(){
		LogHelper.closeAndRemoveFileHandler();
		Path base = new File(".").getAbsoluteFile().toPath();
		FileUtils.delete(PathUtils.get(base, "backup"));
		FileUtils.delete(PathUtils.get(base, "log"));
	}
	
	@Test
	public void testDelete() throws Exception {
		Path[] paths = createEnv(1);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		
		assertThat(exists(PathUtils.get(fs, "dir2/dir21/f21.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "dir2")), is(true));
		assertThat(exists(PathUtils.get(fs, "dir1/f1.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "f.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "ff.txt")), is(false));
		
		new EnvConfigurator(new HashMap<>(), module).deleteFiles();

		assertThat(exists(PathUtils.get(fs, "dir1/f1.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "dir2/dir21/f21.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "dir2")), is(false));
		assertThat(exists(PathUtils.get(fs, "f.txt")), is(false));
		
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "ff.txt")), is(false));
		
		FileUtils.delete(root);
	}

	@Test
	public void testSimpleCopy() throws Exception {
		Path[] paths = createEnv(2);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		
		assertThat(exists(PathUtils.get(fs, "dir2/dir21/f21.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "dir2/f2.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "dir1/f1.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "f.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "ff.txt")), is(true));
		
		new EnvConfigurator(new HashMap<>(), module).copyFiles();		

		assertThat(exists(PathUtils.get(fs, "dir1/f1.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "dir2/dir21/f21.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "f.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "ff.txt")), is(true));
		
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt")), is(true));

		FileUtils.delete(root);
	}
	
	@Test
	public void testComplexCopy() throws Exception {
		Path[] paths = createEnv(3);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		Path result = paths[3];
		
		assertThat(exists(PathUtils.get(fs, "dir2/dir21/f21.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "dir2/f2.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "dir1/f1.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "f.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "ff.txt")), is(true));
		
		Map<String, String> ext = new HashMap<>();
		ext.put("favlang", "aaaaaa");
		ext.put("favlang2", "bbbbbb");
		new EnvConfigurator(ext, module).copyFiles();		

		checkResult(fs, result, "dir1/f1.txt");
		checkResult(fs, result, "dir2/dir21/f21.txt");
		checkResult(fs, result, "f.txt");
		checkResult(fs, result, "ff.txt");
		checkResult(fs, result, "dir3/f3.txt");

		FileUtils.delete(root);
	}
	

	@Test
	public void testExec() throws Exception {
		Path[] paths = createEnv(4);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		Path result = paths[3];
		
		assertThat(exists(PathUtils.get(fs, "dir2/dir21/f21.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "dir2/f2.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "dir1/f1.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "f.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "ff.txt")), is(true));
		
		Map<String, String> ext = new HashMap<>();
		ext.put("favlang", "aaaaaa");
		ext.put("favlang2", "bbbbbb");
		new EnvConfigurator(ext, module).exec();		

		checkResult(fs, result, "dir1/f1.txt");
		checkResult(fs, result, "dir2/dir21/f21.txt");
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt")), is(false));
		checkResult(fs, result, "f.txt");
		checkResult(fs, result, "ff.txt");
		
		FileUtils.delete(root);
	}
	
	@Test
	public void testExecWithPreCondition() throws Exception {
		Path[] paths = createEnv(5);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		
		assertThat(exists(PathUtils.get(fs, "dir2/dir21/f21.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "f.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt")), is(true));
		
		Map<String, String> ext = new HashMap<>();
		ext.put("favlang", "aaaaaa");
		new EnvConfigurator(ext, module).exec();		

		assertThat(exists(PathUtils.get(fs, "dir2/dir21/f21.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "f.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt")), is(true));
		
		FileUtils.delete(root);
	}
	
	@Test
	public void testExecWithPostCondition() throws Exception {
		Path[] paths = createEnv(6);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		
		assertThat(exists(PathUtils.get(fs, "f.txt")), is(true));	
		Set<PosixFilePermission> before = null;
		
		if (!isWindows()) {
			before = Files.getPosixFilePermissions(PathUtils.get(fs, "f.txt"));
		}
		
		assertThat(exists(PathUtils.get(fs, "ff.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "ff.txt.postexecuted")), is(false));
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt")), is(true));
		
		Map<String, String> ext = new HashMap<>();
		ext.put("favlang", "aaaaaa");
		ext.put("favlang2", "bbbbbb");
		new EnvConfigurator(ext, module).exec();		

		assertThat(exists(PathUtils.get(fs, "f.txt")), is(true));
		
		if (!isWindows()) {
			Set<PosixFilePermission> after = Files.getPosixFilePermissions(PathUtils.get(fs, "f.txt"));
			assertThat(after, is(not(equalTo(before))));
		}
		assertThat(exists(PathUtils.get(fs, "ff.txt.postexecuted")), is(true));
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt.postexecuted")), is(true));
		
		FileUtils.delete(root);
	}
	
	@Test
	public void testExecWithExecPreCondition() throws Exception {
		Path[] paths = createEnv(7);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		Files.write(PathUtils.get(module, "Module.hook"), buildModuleHookPre()); 
				
		Map<String, String> ext = new HashMap<>();
		ext.put("var", "var errada");
		new EnvConfigurator(ext, module).exec();		

		assertThat(exists(PathUtils.get(fs, "f.txt")), is(false));
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt")), is(false));
		
		FileUtils.delete(root);
	}
	
	@Test
	public void testExecWithExecPostCondition() throws Exception {
		Path[] paths = createEnv(8);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		Files.write(PathUtils.get(module, "Module.hook"), buildModuleHookPost()); 
				
		Map<String, String> ext = new HashMap<>();
		new EnvConfigurator(ext, module).exec();		

		assertThat(exists(PathUtils.get(fs, "f.txt")), is(true));
		assertThat(exists(PathUtils.get(fs, "dir3/f3.txt")), is(true));
		assertThat(exists(PathUtils.get(module, "Module.postexecuted")), is(true));		
		
		FileUtils.delete(root);
	}
	
	/**
	 * Make sure the behavior will not be changed in the future. 
	 *    Issue: https://jira.codehaus.org/browse/GROOVY-2939
	 *    Because of this issue the creators do not want to fix, we need to encode in .tmpl files:
	 *      - all '\' as '\\' 
	 *      - all '$' as '\$' (not because of the issue, but because it is a char to execute the EL)  
	 * 
	 * If we change this, it should be configurable
	 */
	@Test
	public void testComplexTmplFiles() throws Exception {
		Path[] paths = createEnv(9);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		Path result = paths[3];
		
		assertThat(exists(PathUtils.get(fs, "f1.txt")), is(false));
		
		Map<String, String> ext = new HashMap<>();
		new EnvConfigurator(ext, module).exec();		

		assertThat(exists(PathUtils.get(fs, "f1.txt")), is(true));
		checkResult(fs, result, "f1.txt");
		
		FileUtils.delete(root);
	}
	
	
	@Test
	public void testSubstitutionProperty() throws IOException, URISyntaxException {
		Path[] paths = createEnv(10);
		Path root = paths[0];
		Path fs = paths[1];
		Path module = paths[2];
		Path result = paths[3];
		
		Map<String, String> ext = new HashMap<>();
		ext.put("test", "TESTADO!");
		new EnvConfigurator(ext, module).exec();
		assertThat(exists(PathUtils.get(fs, "dir1/f10.txt")), is(true));
		checkResult(fs, result, "dir1/f10.txt");
		FileUtils.delete(root);
	}
	
	private byte[] buildModuleHookPre() {
		return ("pre {"
				+ "\n	if (\"xyz\".equals(get(\"var\")))"
				+ "\n		CONTINUE_INSTALLATION;"
				+ "\n	else"
				+ "\n		SKIP_INSTALLATION;"
				+ "\n}").getBytes();
	}
	
	private byte[] buildModuleHookPost() {
		return ("\npost {"
				+ "\n	Files.createFile(Paths.get(moduleDir + \"/Module.postexecuted\"));"
				+ "\n}").getBytes();
	}

	private Path[] createEnv(int index) throws IOException, URISyntaxException {
		Path root = null;				
		if (isWindows()) {
			root = buildRootPathForWindows();
		} else {
			root =  Files.createTempDirectory("root");
		}
				
		Path targetFileSystemDir = FileUtils.createDirectories(PathUtils.get(root, "fs"));
		Path tempModuleDir = FileUtils.createDirectories(PathUtils.get(root, "modules"));
		Path targetResultDir = FileUtils.createDirectories(PathUtils.get(root, "result"));
		Path targetModuleDir = FileUtils.createDirectories(PathUtils.get(tempModuleDir, targetFileSystemDir));
		
		FileUtils.copy(getFileSystemResource(index), targetFileSystemDir);
		FileUtils.copy(getModuleResource(index), targetModuleDir);
		FileUtils.copy(getResultResource(index), targetResultDir);
		
		return new Path[]{root, targetFileSystemDir, tempModuleDir, targetResultDir};
	}
	
	private Path buildRootPathForWindows() throws IOException {
		Path root = null;
		Path newRootDir = Files.createTempDirectory("root");
		for (Path rootPath : newRootDir.getFileSystem().getRootDirectories()) {
			if (newRootDir.startsWith(rootPath)) {
				root = Paths.get("/" + rootPath.relativize(newRootDir).toString());
			}
		}
		return root;
	}

	private Path getFileSystemResource(int index) throws URISyntaxException {		
		String resource = "/com/datamaio/scd4j/EnvConfiguratorResources/filesystem/fs" + index;
		return getResource(resource);
	}
	
	private Path getModuleResource(int index) throws URISyntaxException {
		String resource = "/com/datamaio/scd4j/EnvConfiguratorResources/modules/m" + index;
		return getResource(resource);
	}

	private Path getResultResource(int index) throws URISyntaxException {
		String resource = "/com/datamaio/scd4j/EnvConfiguratorResources/result/r" + index;
		return getResource(resource);
	}
	
	private Path getResource(String resource) throws URISyntaxException {
		return Paths.get(getClass().getResource(resource).toURI());
	}
	
	private void checkResult(Path fs, Path result, String file) {
		assertThat(exists(PathUtils.get(fs, file)), is(true));
		try {
			byte[] actual = Files.readAllBytes(PathUtils.get(fs, file));
			byte[] expected = Files.readAllBytes(PathUtils.get(result, file));
			Assert.assertThat(actual, is(equalTo(expected)));
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
