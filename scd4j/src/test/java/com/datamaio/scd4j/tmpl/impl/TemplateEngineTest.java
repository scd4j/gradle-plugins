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
package com.datamaio.scd4j.tmpl.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datamaio.scd4j.exception.MissingPropertyException;
import com.datamaio.scd4j.tmpl.TemplateEngine;
import com.datamaio.scd4j.tmpl.TemplateEngineConfig;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;

public abstract class TemplateEngineTest {
	private TemplateEngine engine;
	private static Path root;
	private String dir;	

	@BeforeClass
	public static void beforeClass() throws Exception {
		root = createEnv();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		// FileUtils.delete(root);
	}

	
	public void setUp(String engineName) {
		this.dir = engineName;
		TemplateEngineConfig conf = new TemplateEngineConfig(engineName);
		engine = TemplateEngine.get(conf);
	}
	
	@Test
	public void testNoScript(){
		Path path = PathUtils.get(root, dir, "NoScript.tmpl");
		StringWriter writer = new StringWriter();
		engine.createTemplate(path)
			.make()
			.writeTo(writer);
		String result = writer.toString();
		assertThat(result, is(equalTo("no script")));
	}
	
	@Test(expected=MissingPropertyException.class)
	public void testMissingProperty(){
		Path path = PathUtils.get(root, dir, "MissingProperty.tmpl");
		StringWriter writer = new StringWriter();
		engine.createTemplate(path)
			.make()
			.writeTo(writer);
		Assert.fail("should give an error. Because 'key' is not set");
	}
	
	@Test
	public void testWithProperty(){
		Path path = PathUtils.get(root, dir, "WithProperty.tmpl");
		StringWriter writer = new StringWriter();
		Map<String, String> map = new HashMap<>();
		map.put("key", "value");
		engine.createTemplate(path)
			.make(map)
			.writeTo(writer);
		String result = writer.toString();
		assertThat(result, is(equalTo("ok key is set to value")));
	}
	
	@Test
	public void testWithComplexProperty(){
		Path path = PathUtils.get(root, dir, "WithComplexProperty.tmpl");
		StringWriter writer = new StringWriter();
		Map<String, Person> map = new HashMap<>();
		map.put("person", new Person("fernando"));
		engine.createTemplate(path)
			.make(map)
			.writeTo(writer);
		String result = writer.toString();
		assertThat(result, is(equalTo("ok person.name is set to fernando")));
	}
	
	@Test
	public void testWithListProperty(){
		Path path = PathUtils.get(root, dir, "WithListProperty.tmpl");
		StringWriter writer = new StringWriter();
		Map<String, Person[]> map = new HashMap<>();
		map.put("persons", new Person[]{new Person("fernando"), new Person("mateus")});
		engine.createTemplate(path)
			.make(map)
			.writeTo(writer);
		String result = writer.toString();
		assertThat(result, is(equalTo("Person = fernando\nPerson = mateus\n")));
	}
	
	public static final class Person {
		private String name;
		public Person(String name) {
			super();
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
		
	private static Path createEnv() {
		try {
			Path to = Files.createTempDirectory("root");
			copyTemplateFiles("groovy", to);
			copyTemplateFiles("handlebars", to);
			copyTemplateFiles("mustache", to);
			copyTemplateFiles("velocity", to);
			return to;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Locate and copy the template's files used in unit tests.  
	 * 
	 * @param templateName The template name. Must be a directory (as from) with same name containing the files.
	 * @param to Directory to copy.
	 */
	private static void copyTemplateFiles(final String templateName, final Path to) {
		try {
			FileUtils.copy(
				Paths.get(TemplateEngineTest.class.getClassLoader().getResource("com/datamaio/scd4j/tmpl/impl/" + templateName).toURI()), 
				Paths.get(to.toString(), templateName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
