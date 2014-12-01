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


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.datamaio.scd4j.tmpl.Template;
import com.datamaio.scd4j.tmpl.TemplateEngine;
import com.datamaio.scd4j.tmpl.Writable;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;


/**
 * Template documentation at https://github.com/spullara/mustache.java
 * 
 * @author Fernando Rubbo
 */
public class MustacheTemplate extends TemplateEngine implements Template, Writable {
	public static final String NAME = "mustache";
	
	private static final MustacheFactory FACTORY = new DefaultMustacheFactory();
	private Mustache mustache; 		
	private Map<String, ? extends Object> binding;
	
	@Override
	public Template createTemplate(Path path) {
		try {
			Reader reader = new FileReader(path.toFile());
			this.mustache = FACTORY.compile(reader, path.toString());
			return this;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Writable make() {
		this.binding = new HashMap<>();
		return this;
	}

	@Override
	public Writable make(Map<String, ? extends Object> binding) {
		this.binding = binding;
		return this;
	}

	@Override
	public Writer writeTo(Writer out){
		try {
			Writer to = mustache.execute(out, binding);
			to.flush();
			return to;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
