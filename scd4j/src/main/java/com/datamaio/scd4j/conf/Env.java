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

/**
 * 
 * @author Fernando Rubbo
 */
public class Env {

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
	}

	public boolean isProduction(final String address) {
        return productionIps.contains(address);
	}
	
	public boolean isStagging(final String address) {
        return stagingIps.contains(address);
	}
	
	public boolean isTesting(final String address) {
        return testingIps.contains(address);
	}
	
	@Override
	public String toString() {
		return "{production:" + productionIps 
				+ ", staging:" + stagingIps 
				+ ", testing:" + testingIps + "}";
	}
}