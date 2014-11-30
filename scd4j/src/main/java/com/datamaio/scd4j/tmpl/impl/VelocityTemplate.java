package com.datamaio.scd4j.tmpl.impl;


import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;

import com.datamaio.scd4j.tmpl.Template;
import com.datamaio.scd4j.tmpl.TemplateEngine;
import com.datamaio.scd4j.tmpl.Writable;


public class VelocityTemplate extends TemplateEngine implements Template, Writable {
	public static final String NAME = "velocity";
		
	@Override
	public Template createTemplate(Path path) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Writable make() {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Writable make(Map<String, ? extends Object> binding) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Writer writeTo(Writer out){
		throw new RuntimeException("Not implemented yet");
	}
}
