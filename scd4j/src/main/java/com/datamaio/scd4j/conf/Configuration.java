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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.datamaio.fwk.io.PathUtils;

public class Configuration {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private static final String LOG_FOLDER = "log";
	private static final String BACKUP_FOLDER = "backup";
	
	public static final String HOOK_SUFFIX = ".hook";
	public static final String DELETE_SUFFIX = ".del";
	public static final String TEMPLATE_SUFFIX = ".tmpl";
		
	/** path for the config file */
	private Path configPath;
	/** all the properties allowed to be used during installaiton. Includes, config file and system properties */
	private final Map<String, String> properties;
	/** Module directory */
	private Path moduleDir;
	/** The environments IPs */
	private ConfEnvironments environments;
	/** Dependencies. Where the key is the gradle dependency and the value is the cache path */
	private Map<String, Path> dependencies;
	
	public Configuration(Path properties, Path module) {
		this(properties, module, new ConfEnvironments(), new HashMap<>());
	}
	
	public Configuration(Path properties, Path module, ConfEnvironments environments, Map<String, Path> dependencies) {
		this(properties, toMaps(properties), module, environments, dependencies);
	}

	private static Map<String, String> toMaps(Path properties) {
		ConfProperties props = new ConfProperties().load(properties);
		return props.entrySet().stream()
				.collect(toMap(e -> e.getKey().toString()
							  ,e -> e.getValue().toString()));
	}
	
	public Configuration(Path propertiesPath, Map<String, String> properties, Path module) {
		this(propertiesPath, properties, module, new ConfEnvironments(), new HashMap<>());
	}
	
	public Configuration(Path confiPath, Map<String, String> properties, Path module, 
			ConfEnvironments environments, Map<String, Path> dependencies) {
		this.configPath = confiPath;
		this.properties = properties;
		this.moduleDir = module;
		this.environments = environments;
		this.dependencies = dependencies;
	}

	public Path getConfigPath() {
		return configPath;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public Path getModuleDir() {
		return moduleDir;
	}

	public ConfEnvironments getEnvironments() {
		return environments;
	}

	public Path getDependency(String name) {
		return dependencies.get(name);
	}
	
	public void printProperties() {
		LOGGER.info("========================================================================================================");
		LOGGER.info("============ Properties allowed to be used during installation (i.e *.hook and *.tmpl) =================");
		LOGGER.info("========================================================================================================");
		Map<String, String> props = getProperties();
		Set<String> keys = props.keySet();
		for (String key : keys) {
			LOGGER.info(String.format("%s = %s", key, props.get(key)));
		}
		LOGGER.info("========================================================================================================");
	}
	
	public Path getLogFile() {
		Path baseDir = new File(".").getAbsoluteFile().toPath();
		String currentExecutionPath = buildCurrentExecutionPath();
		String fileName = currentExecutionPath + ".log";
		return PathUtils.get(baseDir, LOG_FOLDER, fileName).normalize();
	}

	public Path getBackupDir() {
		Path baseDir = new File(".").getAbsoluteFile().toPath();
		String currentExecutionPath = buildCurrentExecutionPath();
		return PathUtils.get(baseDir, BACKUP_FOLDER, currentExecutionPath).normalize();
	}

	private String currentExecutionPath;
	private synchronized String buildCurrentExecutionPath() {
		if(currentExecutionPath==null) {		
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			Path baseDir = new File(".").getAbsoluteFile().toPath();				
			String fileName = baseDir.relativize(getConfigPath()).toString().replace("config/", "");
			if(fileName.endsWith(".conf")) {
				fileName = fileName.replace(".conf", "");
			}
			if(fileName.endsWith(".properties")) {
				fileName = fileName.replace(".properties", "");
			}
			String module = getModuleDir().getFileName().toString();
			currentExecutionPath = Paths.get(module, fileName + "_" + df.format(new Date())).toString();
		}
		
		return currentExecutionPath;
	}
}
