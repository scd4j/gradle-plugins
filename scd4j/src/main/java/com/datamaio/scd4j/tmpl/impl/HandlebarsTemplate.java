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


import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.datamaio.scd4j.tmpl.Template;
import com.datamaio.scd4j.tmpl.TemplateEngine;
import com.datamaio.scd4j.tmpl.Writable;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.URLTemplateSource;


/**
 * Template documentation at https://github.com/jknack/handlebars.java
 * 
 * @author Fernando Rubbo
 */
public class HandlebarsTemplate extends TemplateEngine implements Template, Writable {
	public static final String NAME = "handlebars";

	private static final Handlebars HANDLEBARS = new Handlebars();
	private com.github.jknack.handlebars.Template template;
	private Map<String, ? extends Object> binding;
		
	@Override
	public Template createTemplate(Path path) {
		try {
			this.template = HANDLEBARS.compile(new URLTemplateSource(path.toString(), path.toUri().toURL()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
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
			Context context = Context.newContext(binding);
			template.apply(context, out);
			out.flush();
			return out;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
