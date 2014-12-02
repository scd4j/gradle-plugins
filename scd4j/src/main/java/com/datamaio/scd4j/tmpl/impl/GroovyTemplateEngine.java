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


import groovy.text.SimpleTemplateEngine;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;

import com.datamaio.scd4j.exception.MissingPropertyException;
import com.datamaio.scd4j.tmpl.Template;
import com.datamaio.scd4j.tmpl.TemplateEngine;
import com.datamaio.scd4j.tmpl.Writable;

/**
 * Template documentation at http://groovy.codehaus.org/Groovy+Templates
 * <p>
 * Issue: https://jira.codehaus.org/browse/GROOVY-2939
 * <br>
 * Using GroovyTemplate, we need to encode in .tmpl files:
 * <ul>
 * 	<li> all '\' as '\\'
 * 	<li> all '$' as '\$' (not because of the issue, but because it is a char to execute the EL)
 * </ul>  
 */
public class GroovyTemplateEngine extends TemplateEngine implements Template, Writable {
	public static final String NAME = "groovy";
	
	private final SimpleTemplateEngine engine = new SimpleTemplateEngine();
	private groovy.text.Template template;
	private groovy.lang.Writable writable; 
	
	@Override
	public Template createTemplate(Path path) {
		try {
			this.template = engine.createTemplate(path.toFile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	@Override
	public Writable make() {
		this.writable = template.make();
		return this;
	}

	@Override
	public Writable make(Map<String, ? extends Object> binding) {
		this.writable = template.make(binding);
		return this;
	}

	@Override
	public Writer writeTo(Writer out){
		try {
			Writer to = writable.writeTo(out);
			to.flush();
			return to;
		} catch (groovy.lang.MissingPropertyException e) {
			throw new MissingPropertyException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
