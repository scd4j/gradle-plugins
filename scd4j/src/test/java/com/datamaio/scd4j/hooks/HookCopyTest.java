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
		
		hook.setConf(conf);
	}

	@After
	public void teardown() throws Exception {
		reset(hook);
	}
	
	@Test
	public void testInstallAndUninstallLocalPack() throws Exception {
//		String pack = null;
//		String dist = Command.get().distribution();
//		if(UbuntuCommand.DIST_NAME.equals(dist)) {
//			pack = DEB_PACK;
//		} else if(CentOSCommand.DIST_NAME.equals(dist)) {
//			pack = RPM_PACK;
//		} else {
//			throw new RuntimeException("Not Implemented for windows! Precisamos discutir");
//		}			
//		
//		try {
//			assertThat(exists(FOO_PATH), is(false));
//			hook.install(pack);
//			assertThat(exists(FOO_PATH), is(true));
//		} finally {
//			hook.uninstall("foo");
//			assertThat(exists(FOO_PATH), is(false));
//		}
//		
//	    verify(hook, times(1)).installLocalPack(anyString());
//	    verify(hook, times(0)).installRemotePack(anyString());
//	    verify(hook, times(1)).resolve(pack);
//	    verify(hook, times(1)).uninstallLocalPack(anyString());
//	    verify(hook, times(0)).uninstallRemotePack(anyString());
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
