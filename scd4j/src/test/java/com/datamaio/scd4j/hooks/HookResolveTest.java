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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.datamaio.scd4j.cmd.UbuntuCommand;
import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.exception.DependencyNotFoundException;
import com.datamaio.scd4j.hooks.module.ModuleHook;

/**
 * 
 * @author Fernando Rubbo
 */
@RunWith(MockitoJUnitRunner.class)
public class HookResolveTest {


	private static final String LOCAL_ZIP_PATH = File.separator + "foo" + File.separator + "foo.zip";
	private static final String LOCAL_ZIP_DEP = "foo_zip.zip";
	
	private static final String LOCAL_DEB_PATH = File.separator + "foo" + File.separator + "foo.deb";
	private static final String LOCAL_DEB_DEP = "foo.deb";

	private static final String LOCAL_RPM_PATH = File.separator + "foo" + File.separator + "foo.rpm";
	private static final String LOCAL_RPM_DEP = "foo.rpm";
	
	private static final String REMOTE_FILE_DEB_PATH = File.separator + "other" + File.separator + "foo.deb";
	private static final String REMOTE_DEB_DEP = "foo@deb";

	private static final String REMOTE_FILE_RPM_PATH = File.separator + "other"+ File.separator + "foo.rpm";
	private static final String REMOTE_RPM_DEP = "foo@rpm";
	

	private static final String REMOTE_FILE_ZIP_PATH = File.separator + "other"+ File.separator + "foo.zip";
	private static final String REMOTE_ZIP_DEP = "foo@zip";

	private static final String REMOTE_FILE_EAR_PATH = File.separator + "other" + File.separator + "foo.ear";
	private static final String REMOTE_EAR_DEP = "foo@ear";

	private static final String REMOTE_FILE_WAR_PATH = File.separator + "other"+ File.separator +"foo.war";
	private static final String REMOTE_WAR_DEP = "foo@war";

	private static final String REMOTE_FILE_JAR_PATH = File.separator + "other"+ File.separator +"foo.jar";
	private static final String REMOTE_JAR_DEP = "foo@jar";

	
	@Mock
	Configuration conf;
	private Hook hook = new ModuleHook() {		
		@Override
		public Object run() {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	@Before
	public void setUp() throws Exception {		
		hook.setConf(conf);
	}
	
	@After
	public void teardown() throws Exception {
		reset(conf);
	}

	@Test(expected=DependencyNotFoundException.class)
	public void testNonExistingDependency() {
		String path = hook.resolve(LOCAL_ZIP_DEP);
		assertThat(path, is(nullValue()));
	}
	
	@Test
	public void testLocalZipDependency() {
		when(conf.getDependency(LOCAL_ZIP_DEP)).thenReturn(Paths.get(LOCAL_ZIP_PATH));
		
		String path = hook.resolve(LOCAL_ZIP_DEP);
		assertThat(path, is(LOCAL_ZIP_PATH));
	}

	@Test
	public void testLocalDebRpmDependency() {
		when(conf.getDependency(LOCAL_DEB_DEP)).thenReturn(Paths.get(LOCAL_DEB_PATH));
		when(conf.getDependency(LOCAL_RPM_DEP)).thenReturn(Paths.get(LOCAL_RPM_PATH));
		
		String path = hook.resolve(LOCAL_DEB_DEP);
		assertThat(path, is(LOCAL_DEB_PATH));
		
		path = hook.resolve(LOCAL_RPM_DEP);
		assertThat(path, is(LOCAL_RPM_PATH));		
	}
	
	@Test
	public void testExactRemoteDebRpmDependency() {
		when(conf.getDependency(LOCAL_DEB_DEP)).thenReturn(Paths.get(LOCAL_DEB_PATH));
		when(conf.getDependency(LOCAL_RPM_DEP)).thenReturn(Paths.get(LOCAL_RPM_PATH));
		when(conf.getDependency(REMOTE_DEB_DEP)).thenReturn(Paths.get(REMOTE_FILE_DEB_PATH));
		when(conf.getDependency(REMOTE_RPM_DEP)).thenReturn(Paths.get(REMOTE_FILE_RPM_PATH));
		
		String path = hook.resolve(REMOTE_DEB_DEP);
		assertThat(path, is(REMOTE_FILE_DEB_PATH));
		
		path = hook.resolve(REMOTE_RPM_DEP);
		assertThat(path, is(REMOTE_FILE_RPM_PATH));		
	}
	
	@Test
	public void testDynamicRemoteDebRpmDependency() {
		when(conf.getDependency(LOCAL_DEB_DEP)).thenReturn(Paths.get(LOCAL_DEB_PATH));
		when(conf.getDependency(LOCAL_RPM_DEP)).thenReturn(Paths.get(LOCAL_RPM_PATH));
		when(conf.getDependency(REMOTE_DEB_DEP)).thenReturn(Paths.get(REMOTE_FILE_DEB_PATH));
		when(conf.getDependency(REMOTE_RPM_DEP)).thenReturn(Paths.get(REMOTE_FILE_RPM_PATH));
		
		try {
			String path = hook.resolve("foo");
			
			if(hook.isWindows())
				fail("Running on windows this dependency must NOT be found");
			
			if(UbuntuCommand.DIST_NAME.equals(hook.distribution())) {
				assertThat(path, is(REMOTE_FILE_DEB_PATH));
			} else {
				assertThat(path, is(REMOTE_FILE_RPM_PATH));
			}
		} catch (DependencyNotFoundException e) {
			if(hook.isLinux())
				fail("Running on linux this dependency must be found");
		}		
	}

	@Test
	public void testDynamicZipDependency() {
		when(conf.getDependency(REMOTE_ZIP_DEP)).thenReturn(Paths.get(REMOTE_FILE_ZIP_PATH));
		when(conf.getDependency(REMOTE_EAR_DEP)).thenReturn(Paths.get(REMOTE_FILE_EAR_PATH));
		when(conf.getDependency(REMOTE_WAR_DEP)).thenReturn(Paths.get(REMOTE_FILE_WAR_PATH));
		when(conf.getDependency(REMOTE_JAR_DEP)).thenReturn(Paths.get(REMOTE_FILE_JAR_PATH));
		
		String path = hook.resolve("foo");
		assertThat(path, is(REMOTE_FILE_ZIP_PATH));
	}
	
	@Test
	public void testDynamicEarDependency() {
		when(conf.getDependency(REMOTE_EAR_DEP)).thenReturn(Paths.get(REMOTE_FILE_EAR_PATH));
		when(conf.getDependency(REMOTE_WAR_DEP)).thenReturn(Paths.get(REMOTE_FILE_WAR_PATH));
		when(conf.getDependency(REMOTE_JAR_DEP)).thenReturn(Paths.get(REMOTE_FILE_JAR_PATH));
		
		String path = hook.resolve("foo");
		assertThat(path, is(REMOTE_FILE_EAR_PATH));
	}

	@Test
	public void testDynamicWarDependency() {
		when(conf.getDependency(REMOTE_WAR_DEP)).thenReturn(Paths.get(REMOTE_FILE_WAR_PATH));
		when(conf.getDependency(REMOTE_JAR_DEP)).thenReturn(Paths.get(REMOTE_FILE_JAR_PATH));
		
		String path = hook.resolve("foo");
		assertThat(path, is(REMOTE_FILE_WAR_PATH));
	}
	
	@Test
	public void testDynamicJarDependency() {
		when(conf.getDependency(REMOTE_JAR_DEP)).thenReturn(Paths.get(REMOTE_FILE_JAR_PATH));
		
		String path = hook.resolve("foo");
		assertThat(path, is(REMOTE_FILE_JAR_PATH));
	}

}
