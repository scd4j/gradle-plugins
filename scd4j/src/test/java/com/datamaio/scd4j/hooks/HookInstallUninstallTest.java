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
import static com.datamaio.scd4j.cmd.Command.whoami;
import static java.nio.file.Files.exists;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.datamaio.scd4j.cmd.CentosCommand;
import com.datamaio.scd4j.cmd.Command;
import com.datamaio.scd4j.cmd.UbuntuCommand;
import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;

/**
 * 
 * @author Fernando Rubbo
 */
@RunWith(MockitoJUnitRunner.class)
public class HookInstallUninstallTest {

	private static final Path FOO_PATH = Paths.get("/opt/foo/tst/foo.txt");
	private static final String RPM_PACK = "foo-0.1-1.i386.rpm";
	private static final String DEB_PACK = "foo_0.1-1_all.deb";
		
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
		Path deb = PathUtils.get(root, "linux/ubuntu/" + DEB_PACK);
		when(conf.getDependency(DEB_PACK)).thenReturn(deb);
		
		Path rpm = PathUtils.get(root, "linux/centos/" + RPM_PACK);
		when(conf.getDependency(RPM_PACK)).thenReturn(rpm);
		
		hook.setConf(conf);
	}

	@After
	public void teardown() throws Exception {
		reset(hook);
	}
		
	@Test
	public void testInstallAndUninstallLocalPack() throws Exception {
		checkRoot();
		
		String pack = null;
		String dist = Command.get().distribution();
		if(UbuntuCommand.DIST_NAME.equals(dist)) {
			pack = DEB_PACK;
		} else if(CentosCommand.DIST_NAME.equals(dist)) {
			pack = RPM_PACK;
		} else {
			throw new RuntimeException("Not Implemented for windows! Precisamos discutir");
		}			
		
		try {
			assertThat(exists(FOO_PATH), is(false));
			hook.install(pack);
			assertThat(exists(FOO_PATH), is(true));
		} finally {
			hook.uninstall("foo");
			assertThat(exists(FOO_PATH), is(false));
		}
		
	    verify(hook, times(1)).installLocalPack(anyString());
	    verify(hook, times(0)).installRemotePack(anyString());
	    verify(hook, times(1)).resolve(pack);
	    verify(hook, times(1)).uninstallLocalPack(anyString());
	    verify(hook, times(0)).uninstallRemotePack(anyString());
	}
	
	@Test
	public void testInstallAndUninstallRemotePack() throws Exception {
		checkRoot();
		
		String pack = null;
		String dist = Command.get().distribution();
		if(UbuntuCommand.DIST_NAME.equals(dist)) {
			pack = "lxde=0.5.0-4ubuntu4";
		} else if(CentosCommand.DIST_NAME.equals(dist)) {
			pack = "lxde-?????????????";
		} else {
			throw new RuntimeException("Not Implemented for windows! Precisamos discutir");
		}			
		
		Path path = Paths.get("/usr/share/doc/lxde/copyright");
		try {
			assertThat(exists(path), is(false));
			hook.install(pack);
			assertThat(exists(path), is(true));
		} finally {
			hook.uninstall("lxde");
			assertThat(exists(path), is(false));
		}
		
	    verify(hook, times(0)).installLocalPack(anyString());
	    verify(hook, times(1)).installRemotePack(anyString());
	    verify(hook, times(1)).resolve(pack);
	    verify(hook, times(1)).uninstallLocalPack(anyString());
	    verify(hook, times(0)).uninstallRemotePack(anyString()); 
		// TODO: It would not be better to uninstall using the same strategy than installation?
		// Asking this, because it is unistalling with uninstallLocalPack even though it has been installed using apt-get install
	}
	
	/**
	 * TODO: Tests for 
	 * 1) install and uninstall from Linux repository
	 * 		a) providing the version
	 * 		b) withot provide the version
	 * 2) isntall and unistall from Artifactory/Maven repository
	 * 		- how to test this? need to have artifactory/nexus up and running
	 * 		a) providing extension. ex: com.datamaio:foo:1@deb
	 *      b) not providing extension. ex. com.datamaio:foo:1
	 *          this case the OS operational should be infered by install method
	 */
	
	
	private void checkRoot() {
		String whoami = whoami();
		if (!"root".equals(whoami)) {
			throw new RuntimeException("This Automated Test must run with root. You are " + whoami);
		}
	}

	private static Path createEnv() throws Exception{
		Path r = null;				
		if(isWindows()) {
			FileUtils.createDirectories(Paths.get("/tmpUnit"));
			r = Files.createTempDirectory(Paths.get("/tmpUnit"), "root");
		} else {
			r =  Files.createTempDirectory("root");
		}
						
		FileUtils.copy(Paths.get(HookInstallUninstallTest.class.getResource("/_packages").toURI()), r);		
		return r;
	}

	
}
