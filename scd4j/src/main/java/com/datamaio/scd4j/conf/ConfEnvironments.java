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
public class ConfEnvironments {
	private List<String> ipProduction;
	private List<String> ipStaging;
	private List<String> ipTesting;
	
	public ConfEnvironments() {
		this((List<String>)null, null, null);
	}
	
	public ConfEnvironments(String[] ipProduction, String[] ipStaging, String[] ipTesting) {
		this(ipProduction!=null ? Arrays.asList(ipProduction) : null,
			ipStaging!=null ? Arrays.asList(ipStaging) : null,
			ipTesting!=null ? Arrays.asList(ipTesting) : null);
	}
	
	public ConfEnvironments(List<String> ipProduction, List<String> ipStaging, List<String> ipTesting) {
		super();
		this.ipProduction = ipProduction!=null ? ipProduction : new ArrayList<String>();
		this.ipStaging = ipStaging!=null ? ipStaging : new ArrayList<String>();
		this.ipTesting = ipTesting!=null ? ipTesting : new ArrayList<String>();
	}

	public boolean isProduction(final String address) {
        return ipProduction.contains(address);
	}
	
	public boolean isStagging(final String address) {
        return ipStaging.contains(address);
	}
	
	public boolean isTesting(final String address) {
        return ipTesting.contains(address);
	}
}