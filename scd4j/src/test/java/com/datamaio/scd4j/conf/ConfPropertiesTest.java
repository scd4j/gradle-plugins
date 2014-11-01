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

import java.io.StringReader;

import org.junit.Test;

import com.datamaio.scd4j.conf.ConfProperties;

public class ConfPropertiesTest {
	private StringReader reader = new StringReader("key1=value1\n" 
			+ "key2=value2\n" 
			+ "key3=value3\n" 
			+ "key4=value4/${key3}\n"
			+ "key5=value5/${key4}");

	@Test
	public void testSipleProp() throws Exception {
		ConfProperties props = new ConfProperties();
		props.load(reader);

		assertThat(props.resolve("${key1}"), is(equalTo("value1")));
	}

	@Test
	public void testTwoOrMoreProps() throws Exception {
		ConfProperties props = new ConfProperties();
		props.load(reader);

		assertThat(props.resolve("${key1}--basdf--${key1}"), is(equalTo("value1--basdf--value1")));
		assertThat(props.resolve("${key1}--basdf--${key2}"), is(equalTo("value1--basdf--value2")));
		assertThat(props.resolve("${key1}--basdf--${key2}--basdf--${key3}"), is(equalTo("value1--basdf--value2--basdf--value3")));
	}

	@Test
	public void testRecursiveProps() throws Exception {
		ConfProperties props = new ConfProperties();
		props.load(reader);

		assertThat(props.resolve("${key1}--basdf--${key3}"), is(equalTo("value1--basdf--value3")));
		assertThat(props.resolve("${key1}--basdf--${key4}"), is(equalTo("value1--basdf--value4/value3")));
		assertThat(props.resolve("${key1}--basdf--${key5}"), is(equalTo("value1--basdf--value5/value4/value3")));
	}

	@Test
	public void testRecursiveSystemPriorityProps() throws Exception {
		try {
			System.setProperty("key4", "systemkey4");
			ConfProperties props = new ConfProperties();
			props.load(reader);

			assertThat(props.resolve("${key1}--basdf--${key3}"), is(equalTo("value1--basdf--value3")));
			assertThat(props.resolve("${key1}--basdf--${key4}"), is(equalTo("value1--basdf--systemkey4")));
			assertThat(props.resolve("${key1}--basdf--${key5}"), is(equalTo("value1--basdf--value5/systemkey4")));
		} finally {
			System.setProperty("key4", "value4/${key3}");
		}
	}

	@Test
	public void testSystemGetsPrefferenceProps() throws Exception {

		try {
			System.setProperty("key1", "systemkey1");
			ConfProperties props = new ConfProperties();
			props.load(reader);

			assertThat(props.resolve("${key1}--basdf--${key2}--basdf--${key3}"), is(equalTo("systemkey1--basdf--value2--basdf--value3")));
		} finally {
			System.setProperty("key1", "value1");
		}
	}
}
