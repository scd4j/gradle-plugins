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

import static org.mockito.Mockito.reset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.datamaio.junit.IsLinux;
import com.datamaio.junit.RunIfRule;
import com.datamaio.junit.RunIfRule.RunIf;
import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.hooks.module.ModuleHook;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;

/**
 * 
 * @author Fernando Rubbo
 */
@RunWith(MockitoJUnitRunner.class)
public class HookActiveAtBootTest {

	@Rule
	public RunIfRule rule = new RunIfRule();
	
	private static Path root;
	
	@Mock
	private Configuration conf;
	@Spy
	private Hook hook = new ModuleHook() {		
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
		hook.setConf(conf);
	}

	@After
	public void teardown() throws Exception {
		reset(hook);
	}
	
	@Test @RunIf(IsLinux.class)
	public void testActiveDeactiveAtBoot() throws Exception {
		checkRoot();
		
		Path dir = PathUtils.get(root, "tmp");
		String service = "date-arq";
		Path srcPath = PathUtils.get("/etc/init.d/", service);
		Path targetPath = PathUtils.get(dir, service); 

		// check
		if( hook.ls("/etc/rc3.d").stream().filter(s -> s.contains(service)).count()>0 ) {
			throw new RuntimeException("Instalations has failed! Service already installed on boot");
		}

		// isntall
		hook.ln(srcPath.toString(), targetPath.toString()); 
		hook.registryToBoot(service);
		
		// check
		if( hook.ls("/etc/rc3.d").stream().filter(s -> s.contains(service)).count()==0 ) {
			throw new RuntimeException("Instalations has failed! Service not installed on boot");
		} 
		
		// clean up		
		hook.unregistryFromBoot(service);
		hook.rm(targetPath.toString());
		hook.rm(dir.toString());
		
		// check
		if( hook.ls("/etc/rc3.d").stream().filter(s -> s.contains(service)).count()>0 ) {
			throw new RuntimeException("Instalations has failed! Service was not unistalled from boot");
		}	
		if( hook.exists(targetPath.toString()) ) {
			throw new RuntimeException("Instalations has failed! Service link is still on");
		}
		if( hook.exists(dir.toString()) ) {
			throw new RuntimeException("Instalations has failed! Service files was not deleted");
		}
	}
	
	private void checkRoot() {
		String whoami = System.getProperty("user.name");
		if (!"root".equals(whoami)) {
			throw new RuntimeException("This Automated Test must run with root. You are " + whoami);
		}
	}
	
	private static Path createEnv() throws Exception{
		Path tmp = createTempDir();
		FileUtils.copy(Paths.get(HookActiveAtBootTest.class.getResource("/com/datamaio/scd4j/hooks/activeatboot").toURI()), tmp);		
		return tmp;
	}

	private static Path createTempDir() throws IOException {
		return Files.createTempDirectory("root");
	}
}
