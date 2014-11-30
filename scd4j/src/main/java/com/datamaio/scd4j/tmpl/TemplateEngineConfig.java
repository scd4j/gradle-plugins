package com.datamaio.scd4j.tmpl;

import java.util.HashMap;
import java.util.Map;

public class TemplateEngineConfig {
	private String name;
	private Map<String,String> options;
	
	public TemplateEngineConfig(String name) {
		this(name, new HashMap<>());
	}
	public TemplateEngineConfig(String name, Map<String, String> options) {
		super();
		this.name = name;
		this.options = options;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getOptions() {
		return options;
	}
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}
	
}
