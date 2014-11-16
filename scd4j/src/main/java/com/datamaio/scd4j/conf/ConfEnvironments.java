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
	private List<String> ipProd;
	private List<String> ipHom;
	private List<String> ipTest;
	
	public ConfEnvironments() {
		this((List<String>)null, null, null);
	}
	
	public ConfEnvironments(String[] ipProd, String[] ipHom, String[] ipTst) {
		this(ipProd!=null ? Arrays.asList(ipProd) : null,
			ipHom!=null ? Arrays.asList(ipHom) : null,
			ipTst!=null ? Arrays.asList(ipTst) : null);
	}
	
	public ConfEnvironments(List<String> ipProd, List<String> ipHom, List<String> ipTst) {
		super();
		this.ipProd = ipProd!=null ? ipProd : new ArrayList<String>();
		this.ipHom = ipHom!=null ? ipHom : new ArrayList<String>();
		this.ipTest = ipTst!=null ? ipTst : new ArrayList<String>();
	}

	public boolean isProd(final String address) {
        return ipProd.contains(address);
	}
	
	public boolean isHom(final String address) {
        return ipHom.contains(address);
	}
	
	public boolean isTest(final String address) {
        return ipTest.contains(address);
	}
}