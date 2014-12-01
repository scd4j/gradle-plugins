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


import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.datamaio.scd4j.tmpl.Template;
import com.datamaio.scd4j.tmpl.TemplateEngine;
import com.datamaio.scd4j.tmpl.Writable;


/**
 * Template documentation at https://velocity.apache.org/engine/releases/velocity-1.5/user-guide.html
 * 
 * @author Fernando Rubbo
 */
public class VelocityTemplate extends TemplateEngine implements Template, Writable {
	public static final String NAME = "velocity";

    static {
        Velocity.setProperty("resource.loader", "file");
        Velocity.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        Velocity.setProperty("file.resource.loader.path", "/");
        Velocity.setProperty("file.resource.loader.cache", "true");
        Velocity.init();
    }
    
    private org.apache.velocity.Template template;
    private VelocityContext context;
	
	@Override
	public Template createTemplate(Path path) {
		// final String encoding = encodingUtil.getEncoding(vmTemplate);
        // template = Velocity.getTemplate(vmTemplate, encoding);

		this.template = Velocity.getTemplate(path.toAbsolutePath().toString());
		return this;
	}

	@Override
	public Writable make() {
		this.context = new VelocityContext();
		return this;
	}

	@Override
	public Writable make(Map<String, ? extends Object> binding) {
		this.context = new VelocityContext();
		binding.forEach((String key, Object value) -> context.put(key, value));
		return this;
	}

	@Override
	public Writer writeTo(Writer out){
		try {
			template.merge(context, out);
			out.flush();
			return out;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
