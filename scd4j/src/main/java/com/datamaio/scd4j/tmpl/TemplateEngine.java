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
package com.datamaio.scd4j.tmpl;

import java.nio.file.Path;

import com.datamaio.scd4j.tmpl.impl.GroovyTemplateEngine;
import com.datamaio.scd4j.tmpl.impl.HandlebarsTemplateEngine;
import com.datamaio.scd4j.tmpl.impl.MustacheTemplateEngine;
import com.datamaio.scd4j.tmpl.impl.VelocityTemplateEngine;

/**
 * @author Fernando Rubbo
 */
public abstract class TemplateEngine {
	
	public static TemplateEngine get(TemplateEngineConfig conf) {
		if(GroovyTemplateEngine.NAME.equalsIgnoreCase(conf.getName())){
			return new GroovyTemplateEngine();
		} else if(HandlebarsTemplateEngine.NAME.equalsIgnoreCase(conf.getName())){
			return new HandlebarsTemplateEngine();
		} else if(MustacheTemplateEngine.NAME.equalsIgnoreCase(conf.getName())){
			return new MustacheTemplateEngine();
		} else if(VelocityTemplateEngine.NAME.equalsIgnoreCase(conf.getName())){
			return new VelocityTemplateEngine();
		}
		throw new RuntimeException("Templete engine '" + conf.getName() 
				+ "' not found! Check options in package 'com.datamaio.scd4j.tmpl.impl'");
	}
	
	public abstract Template createTemplate(Path path);
}
