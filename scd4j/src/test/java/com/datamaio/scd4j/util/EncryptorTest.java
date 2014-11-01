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

package com.datamaio.scd4j.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.datamaio.scd4j.util.Encryptor;
import com.datamaio.scd4j.util.Encryptor.DecryptionException;

public class EncryptorTest {
	
	@Test
	public void testDescrypt(){
		Encryptor enc = Encryptor.get("abc123");
		String value = enc.encrypt("encryption test");		
		assertThat(enc.decrypt(value), is(equalTo("encryption test")));		
	}
	
	@Test (expected=DecryptionException.class)
	public void testIncorrectDescrypt(){
		Encryptor enc = Encryptor.get("abc123");
		String value = enc.encrypt("encryption test");
		
		Encryptor enc2 = Encryptor.get("123abc");
		assertThat(enc2.decrypt(value), is(equalTo("encryption test")));
	}
}
