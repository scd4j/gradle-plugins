package com.datamaio.scd4j.tmpl.impl;


import groovy.text.SimpleTemplateEngine;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;

import com.datamaio.scd4j.tmpl.Template;
import com.datamaio.scd4j.tmpl.TemplateEngine;
import com.datamaio.scd4j.tmpl.Writable;

/**
 * Issue: https://jira.codehaus.org/browse/GROOVY-2939
 * <p>
 * Using GroovyTemplate, we need to encode in .tmpl files:
 * <ul>
 * 	<li> all '\' as '\\'
 * 	<li> all '$' as '\$' (not because of the issue, but because it is a char to execute the EL)
 * </ul>  
 */
public class GroovyTemplate extends TemplateEngine implements Template, Writable {
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
			return writable.writeTo(out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
