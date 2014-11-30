package com.datamaio.scd4j.tmpl;

import java.nio.file.Path;

import com.datamaio.scd4j.tmpl.impl.GroovyTemplate;

public abstract class TemplateEngine {
	
	public static TemplateEngine get(TemplateEngineConfig conf) {
		if(GroovyTemplate.NAME.equalsIgnoreCase(conf.getName())){
			return new GroovyTemplate();
		}
		throw new RuntimeException("Templete engine '" + conf.getName() + "' not found!");
	}
	
	public abstract Template createTemplate(Path path);
}
