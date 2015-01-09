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
package com.datamaio.scd4j.conf;

import static java.util.stream.Collectors.toMap;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fernando Rubbo
 */
public class Install {	
	private final Path[] modules;
	private final Path config;
	private final Env env;
	
	/** All the properties allowed to be used during installaiton. Includes, config file and system properties */
	private Map<String, Object> props;
	/** Temporary properties set in the hook.pre */
	private Map<String, Object> tempProps;

	public Install(Path module) {
		this(module, (Path)null);
	}
	
	public Install(Path module, Map<String, Object> props) {
		this(module, (Path)null);
		this.props = props;
	}

	public Install(Path[] modules) {
		this(modules, (Path)null);
	}
	
	public Install(Path[] modules, Map<String, Object> props) {
		this(modules, (Path)null);
		this.props = props;
	}
	
	public Install(Path module, Path config) {
		this(module, config, new Env());
	}
	
	public Install(Path[] modules, Path config) {
		this(modules, config, new Env());
	}
	
	public Install(Path module, Path config, Env env) {
		this(new Path[]{module}, config, env);
	}
	
	public Install(Path[] modules, Path config, Env env) {
		super();
		this.modules = modules;
		this.config = config;
		this.env = env;
		this.props = loadProps();
		this.tempProps = new HashMap<>();
	}
	
	private Map<String, Object> loadProps() {
		if(this.config!=null) {
			Config props = new Config().load(this.config);
			return props.entrySet().stream()
					.collect(toMap(e -> e.getKey().toString(), 
								   e -> e.getValue()));
		} else {
			return new HashMap<>();
		}
	}

	protected Path[] getModules() {
		return modules;
	}

	protected Path getConfig() {
		return config;
	}

	protected Env getEnv() {
		return env;
	}

	protected Map<String, Object> getProps() {
		return props;
	}

	protected Map<String, Object> getTempProps() {
		return tempProps;
	}

	@Override
	public String toString() {
		return "Install [modules=" + Arrays.toString(modules) + ", config=" + config + ", env=" + env + "]";
	}
}
