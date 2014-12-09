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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.datamaio.junit.IsLinux;
import com.datamaio.junit.IsWindows;
import com.datamaio.junit.RunIfRule;
import com.datamaio.junit.RunIfRule.RunIf;
import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.hooks.module.ModuleHook;

@RunWith(MockitoJUnitRunner.class)
public class HookExecuteTest {

	@Rule
	public RunIfRule rule = new RunIfRule();
	
	@Mock
	private Configuration conf;
	@Spy
	private Hook hook = new ModuleHook() {		
		@Override
		public Object run() {
			return null;
		}
	};

	@Before
	public void setUp() throws Exception {		
		hook.setConf(conf);
	}
	
	@Test @RunIf(IsLinux.class)
	public void executableFileOnLinux(){
		Assert.fail("Test Must Be Implemented");
	}
	
	@Test @RunIf(IsLinux.class)
	public void withoutPermitionExecutableFileOnLinux(){
		Assert.fail("Test Must Be Implemented");
	}
	
	@Test @RunIf(IsLinux.class)
	public void notExecutableFileOnLinux(){
		// put (expected=RuntimeException.class)
		Assert.fail("Test Must Be Implemented");
	}

	@Test @RunIf(IsWindows.class)
	public void executeOnWindows(){
		Assert.fail("Test Must Be Implemented");
	}
	
	@Test @RunIf(IsWindows.class)
	public void notExecutableFileOnWindows(){
		Assert.fail("Test Must Be Implemented");
	}	
}
