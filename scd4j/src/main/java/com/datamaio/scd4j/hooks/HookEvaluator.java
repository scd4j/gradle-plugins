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
package com.datamaio.scd4j.hooks;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.util.io.FileUtils;

/**
 * 
 * @author Fernando Rubbo
 */
public abstract class HookEvaluator {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private Path groovyPath;
	private GroovyShell shell;
	protected String script;
	
	public HookEvaluator(Path groovyPath, Map<String, Object> binds, Configuration conf) {
		this.groovyPath = groovyPath;
		
		binds = buildBinding(binds, conf);
		this.shell = createShell(binds, conf);
		this.script = readScript(binds);
	}

	private Map<String, Object> buildBinding(Map<String, Object> binds, Configuration conf) {
		binds.put("conf", conf);
		return binds;
	}

	private String readScript(Map<String, Object> binds) {
		if( Files.exists(groovyPath) ) {
			String script = "";
			for (String b : binds.keySet()) {
				script += "set" + b.substring(0,1).toUpperCase() + b.substring(1) + "(" + b + ");\n";
			}
			return script + FileUtils.read(groovyPath);
		}
		
		return null;
	}

	public boolean pre(){
		if(exists()) {
			return (boolean) evaluate("pre");
		}
		
		return true;
	}
	
	public void post(){
		if(exists()) {
			evaluate("post");
		}
	}
	
	public void finish(){
		if(exists()) {
			evaluate("finish");
		}
		LOGGER.info("--------------------------" );
	}
	
	private boolean exists() {
		return script!=null;
	}

	private Object evaluate(String action) {
		String fullScript = script + "\n " + action + "();";
		return shell.evaluate(fullScript);
	}
	
	private GroovyShell createShell(Map<String, Object> binds, Configuration conf) {
		CompilerConfiguration configuration = new CompilerConfiguration();
		configuration.setScriptBaseClass(getScriptBaseClass());
		Binding binding = new Binding();
		for (String b : binds.keySet()) {
			binding.setProperty(b, binds.get(b));
		}
		ClassLoader loader = this.getClass().getClassLoader();
		return new GroovyShell(loader, binding, configuration);
	}

	protected abstract String getScriptBaseClass();
	
	@Override
	public String toString() {
		return groovyPath.toString();
	}
}
