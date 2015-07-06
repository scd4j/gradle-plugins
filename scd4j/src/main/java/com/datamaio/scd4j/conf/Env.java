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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.datamaio.scd4j.cmd.Command;

/**
 * 
 * @author Fernando Rubbo
 */
public class Env {

	private static enum Environment {
		development, testing, staging, production;
	}
	
	private Environment environment;
	private List<String> productionIps;
	private List<String> stagingIps;
	private List<String> testingIps;
	
	public Env() {
		this((List<String>)null, null, null);
	}
	
	public Env(String[] productionIps, String[] stagingIps, String[] testingIps) {
		this(productionIps!=null ? Arrays.asList(productionIps) : null,
			stagingIps!=null ? Arrays.asList(stagingIps) : null,
			testingIps!=null ? Arrays.asList(testingIps) : null);
	}
	
	public Env(List<String> ipProduction, List<String> ipStaging, List<String> ipTesting) {
		super();
		this.productionIps = ipProduction!=null ? ipProduction : new ArrayList<String>();
		this.stagingIps = ipStaging!=null ? ipStaging : new ArrayList<String>();
		this.testingIps = ipTesting!=null ? ipTesting : new ArrayList<String>();
		if(thereExistIntersectionsBetweenIpLists() ) {
			throw new IllegalArgumentException("There exist intersections between env ip lists. "
					+ "Fix it in 'build.gradle' before running SCD4J again.");
		}
	}
	
	void setConfiguredEnvironment(String configuredEnvironment){
		try {
			this.environment = configuredEnvironment!=null ? Environment.valueOf(configuredEnvironment) : Environment.development;
		} catch (IllegalArgumentException e){
			throw new IllegalArgumentException(configuredEnvironment + " is not a valid env. Check your config file!", e);
		}
		validateEnvironmentAgainstIps();
	}
	
	public boolean isProduction(final String address) {
		checkCorrectState();
        boolean isInIpList = productionIps.isEmpty() || productionIps.contains(address);
		boolean isInConfiguration = environment.equals(Environment.production);
		return isInIpList && isInConfiguration;
	}

	public boolean isStaging(final String address) {
		checkCorrectState();
        boolean isInIpList = stagingIps.isEmpty() || stagingIps.contains(address);
		boolean isInConfiguration = environment.equals(Environment.staging);
		return isInIpList && isInConfiguration;
	}
	
	public boolean isTesting(final String address) {
		checkCorrectState();
        boolean isInIpList = testingIps.isEmpty() || testingIps.contains(address);
		boolean isInConfiguration = environment.equals(Environment.testing);
		return isInIpList && isInConfiguration;
	}
	
	public boolean isDevelopment(final String address) {
		checkCorrectState();
        boolean isInIpList = !isTesting(address) && !isStaging(address) && !isProduction(address);
		boolean isInConfiguration = environment.equals(Environment.development);
		return isInIpList && isInConfiguration;
	}
	
	void checkCorrectState() {
		if(environment==null){
			throw new IllegalStateException("Configured Environment was not set!");
		}
	}
	
	void validateEnvironmentAgainstIps() {
		String address = whatIsMyIp();
		switch (environment) {
			case production: 
				checkAddressAgaisntIps(address, productionIps);
				break;
			case staging: 
				checkAddressAgaisntIps(address, stagingIps);
				break;
			case testing: 
				checkAddressAgaisntIps(address, testingIps);
				break;
			default: // ok is development
		}
	}

	String whatIsMyIp() {
		return Command.get().whatIsMyIp();
	}

	boolean thereExistIntersectionsBetweenIpLists() {
		return thereExistIntersection(productionIps, stagingIps) 
				|| thereExistIntersection(productionIps, testingIps);				
	}

	boolean thereExistIntersection(List<String> ips1, List<String> ips2) {
		return ips1.stream().filter(c -> ips2.contains(c)).count()>0;
	}
	
	void checkAddressAgaisntIps(final String address, List<String> ips) {
		if(ips.isEmpty()) {
			// it is ok because it is only the environment that accounts
		} else if( !ips.contains(address) ) {
	    	// makes sure that if we have the address in the ips, we must be in the correct environment 
	    	throw new IllegalStateException("You are trying to run a " + environment 
	    			+ " config file against a NON " + environment + " environment!");
	    }
	}
	
	
	public List<String> getProductionIps() {
		return productionIps;
	}
	
	public List<String> getStagingIps() {
		return stagingIps;
	}
	
	public List<String> getTestingIps() {
		return testingIps;
	}
	
	@Override
	public String toString() {
		return "{production:" + productionIps 
				+ ", staging:" + stagingIps 
				+ ", testing:" + testingIps
				+ ", cunfiguredEnvironment: " + environment
				+ "}";
	}
}