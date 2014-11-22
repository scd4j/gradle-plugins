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

import com.datamaio.scd4j.cmd.CentOSCommand;
import com.datamaio.scd4j.cmd.Command;
import com.datamaio.scd4j.cmd.UbuntuCommand;
import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;

@RunWith(MockitoJUnitRunner.class)
public class HookTest {

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
		} else if(CentOSCommand.DIST_NAME.equals(dist)) {
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
		} else if(CentOSCommand.DIST_NAME.equals(dist)) {
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
	
	private void checkRoot() {
		String whoami = whoami();
		if (!"root".equals(whoami)) {
			throw new RuntimeException("This Automated Test must run with root. I'm " + whoami);
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
						
		FileUtils.copy(Paths.get(HookTest.class.getResource("/_packages").toURI()), r);		
		return r;
	}

	
}
