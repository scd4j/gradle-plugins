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
package com.datamaio.scd4j.conf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvTest {
	private static final String PRODUCTION_IP2 = "3.3.3.2";
	private static final String PRODUCTION_IP1 = "3.3.3.1";
	private static final String STAGING_IP2 = "2.2.2.2";
	private static final String STAGING_IP1 = "2.2.2.1";
	private static final String TESTING_IP = "1.1.1.0";
	private static final String DESENV_IP = "0.0.0.0";
	
	private static String[] productionIps = new String[]{PRODUCTION_IP1, PRODUCTION_IP2};
	private static String[] stagingIps = new String[]{STAGING_IP1, STAGING_IP2};
	private static String[] testingIps = new String[]{TESTING_IP};
	
	@Spy
	private Env env = new Env(productionIps, stagingIps, testingIps);
	@Spy
	private Env env2 = new Env();
	
	@Before
	public void setup(){
			
	}
	
	@After
	public void teardown() throws Exception {
		reset(env);
		reset(env2);
	}
	
	@Test
	public void notSettingConfiguredEnvironment(){
		try {
			env.isDevelopment(DESENV_IP);
			fail("It should be failled because ConfiguredEnvironment is not set!");
		} catch (IllegalStateException e){
			// ok
		}

		try {
			env.isTesting(TESTING_IP);
			fail("It should be failled because ConfiguredEnvironment is not set!");
		} catch (IllegalStateException e){
			// ok
		}
		
		try {
			env.isStaging(STAGING_IP1);
			fail("It should be failled because ConfiguredEnvironment is not set!");
		} catch (IllegalStateException e){
			// ok
		}

		try {
			env.isProduction(PRODUCTION_IP1);
			fail("It should be failled because ConfiguredEnvironment is not set!");
		} catch (IllegalStateException e){
			// ok
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void tryToRunAProductionConfigAgainstANonProductionEnvironment(){
		when(env.whatIsMyIp()).thenReturn(STAGING_IP1);
		
		env.setConfiguredEnvironment("production");	
	}
	
	@Test(expected=IllegalStateException.class)
	public void tryToRunAStagingConfigAgainstANonStatingEnvironment(){
		when(env.whatIsMyIp()).thenReturn(DESENV_IP);
		
		env.setConfiguredEnvironment("staging");	
	}
	
	@Test(expected=IllegalStateException.class)
	public void tryToRunATestingConfigAgainstANonTestingEnvironment(){
		when(env.whatIsMyIp()).thenReturn(DESENV_IP);
		
		env.setConfiguredEnvironment("testing");	
	}
	
	@Test
	public void noEnvValidationBecauseListOfIpsIsEmpty(){
		when(env2.whatIsMyIp()).thenReturn(DESENV_IP);
		
		env2.setConfiguredEnvironment("development");	
		env2.setConfiguredEnvironment("testing");
		env2.setConfiguredEnvironment("staging");
		env2.setConfiguredEnvironment("production");
	}
	
	// --------------------------------------------
	
	@Test(expected=IllegalArgumentException.class)
	public void newEnvWithProductionIpIntersection(){
		new Env(productionIps, new String[]{STAGING_IP1, PRODUCTION_IP1}, new String[]{});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void newEnvWithProductionIpIntersection2(){
		new Env(productionIps, new String[]{}, new String[]{PRODUCTION_IP1});
	}

	@Test
	public void newEnvOk(){
		new Env(productionIps, new String[]{}, new String[]{});
	}
	
	// --------------------------------------------
	
	@Test
	public void isTestingYes(){
		when(env.whatIsMyIp()).thenReturn(TESTING_IP);		
		env.setConfiguredEnvironment("testing");
		assertThat(env.isTesting(TESTING_IP), is(equalTo(true)));
		
		when(env2.whatIsMyIp()).thenReturn(DESENV_IP);		
		env2.setConfiguredEnvironment("testing");
		assertThat(env2.isTesting(TESTING_IP), is(equalTo(true)));		
	}
	
	@Test
	public void isTestingNo(){
		when(env.whatIsMyIp()).thenReturn(PRODUCTION_IP1);		
		env.setConfiguredEnvironment("production");
		assertThat(env.isTesting(PRODUCTION_IP1), is(equalTo(false)));
		
		when(env2.whatIsMyIp()).thenReturn(DESENV_IP);		
		env2.setConfiguredEnvironment("production");
		assertThat(env2.isTesting(PRODUCTION_IP1), is(equalTo(false)));
	}

	// ------------------------------------------------
	
	@Test
	public void isStagingYes(){
		when(env.whatIsMyIp()).thenReturn(STAGING_IP1);		
		env.setConfiguredEnvironment("staging");
		assertThat(env.isStaging(STAGING_IP1), is(equalTo(true)));
		
		when(env2.whatIsMyIp()).thenReturn(DESENV_IP);		
		env2.setConfiguredEnvironment("staging");
		assertThat(env2.isStaging(STAGING_IP1), is(equalTo(true)));		
	}
	
	@Test
	public void isStagingNo(){
		when(env.whatIsMyIp()).thenReturn(PRODUCTION_IP1);
		env.setConfiguredEnvironment("production");
		assertThat(env.isStaging(PRODUCTION_IP1), is(equalTo(false)));
		
		when(env2.whatIsMyIp()).thenReturn(DESENV_IP);
		env2.setConfiguredEnvironment("production");
		assertThat(env2.isStaging(PRODUCTION_IP1), is(equalTo(false)));		
	}
	
	// ------------------------------------------------
	
	@Test
	public void isProductionYes(){
		when(env.whatIsMyIp()).thenReturn(PRODUCTION_IP1);
		env.setConfiguredEnvironment("production");
		assertThat(env.isProduction(PRODUCTION_IP1), is(equalTo(true)));
		
		when(env2.whatIsMyIp()).thenReturn(DESENV_IP);
		env2.setConfiguredEnvironment("production");
		assertThat(env2.isProduction(PRODUCTION_IP1), is(equalTo(true)));		
	}
	
	@Test
	public void isProductionNo(){
		when(env.whatIsMyIp()).thenReturn(STAGING_IP1);
		env.setConfiguredEnvironment("staging");
		assertThat(env.isProduction(STAGING_IP1), is(equalTo(false)));
		
		when(env2.whatIsMyIp()).thenReturn(DESENV_IP);
		env2.setConfiguredEnvironment("staging");
		assertThat(env2.isProduction(STAGING_IP1), is(equalTo(false)));		
	}

	// ------------------------------------------------

	@Test
	public void isDevelopmentYes(){
		when(env.whatIsMyIp()).thenReturn(DESENV_IP);
		env.setConfiguredEnvironment("development");
		assertThat(env.isDevelopment(DESENV_IP), is(equalTo(true)));
		
		when(env2.whatIsMyIp()).thenReturn(DESENV_IP);
		env2.setConfiguredEnvironment("development");
		assertThat(env2.isDevelopment(DESENV_IP), is(equalTo(true)));				
	}
	
	@Test
	public void isDevelopmentNo(){
		when(env.whatIsMyIp()).thenReturn(STAGING_IP1);
		env.setConfiguredEnvironment("staging");
		assertThat(env.isDevelopment(STAGING_IP1), is(equalTo(false)));
		
		when(env2.whatIsMyIp()).thenReturn(STAGING_IP1);
		env2.setConfiguredEnvironment("staging");
		assertThat(env2.isDevelopment(STAGING_IP1), is(equalTo(false)));				
	}
	
	// ------------------------------------------------

	@Test
	public void settingNullToConfiguredEnvironmentMustBeDevelopment(){
		when(env.whatIsMyIp()).thenReturn(DESENV_IP);
		env.setConfiguredEnvironment(null);
		assertThat(env.isDevelopment(DESENV_IP), is(equalTo(true)));		
	}
	
	@Test
	public void settingInvalidArgumentToConfiguredEnvironment(){
		try {
			env2.setConfiguredEnvironment("dev");
			fail("Should be thrown an Illegal argument exception!");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), CoreMatchers.containsString("Check your config file!"));
		}
	}

}
