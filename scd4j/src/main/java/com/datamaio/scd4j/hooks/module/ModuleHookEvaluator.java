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
package com.datamaio.scd4j.hooks.module;

import static com.datamaio.scd4j.conf.Configuration.HOOK_SUFFIX;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.hooks.HookEvaluator;
import com.datamaio.scd4j.util.io.PathUtils;

/**
 * 
 * @author Fernando Rubbo
 */
public class ModuleHookEvaluator extends HookEvaluator {
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private final Path moduleDir;
	
	public ModuleHookEvaluator(final Configuration conf) {
		super(buildModuleHookName(conf), buildBinding(conf), conf);
		this.moduleDir = conf.getModuleDir();
	}

	@Override
	protected String getScriptBaseClass() {
		return ModuleHook.class.getName();
	}	
	
	private static Map<String, Object> buildBinding(final Configuration conf) {
		Map<String, Object> map = new HashMap<>();
		map.put("moduleDir", conf.getModuleDir().toString());
		return map;
	}

	@Override
	public boolean pre() {
		LOGGER.info("#############################################################################################################");
		LOGGER.info("######## BEGIN MODULE ############# " + relativize() + " ############ BEGIN MODULE #########");
		LOGGER.info("#############################################################################################################");
		LOGGER.info(":PRE MODULE");
		boolean pre = super.pre();
		LOGGER.info("--------------------------" );
		return pre;
	}

	private String relativize() {
		String mod = moduleDir.toString();
		return mod.substring(mod.indexOf(File.separator + "module"));
	}

	@Override
	public void post() {
		LOGGER.info(":POST MODULE");
		super.post();		
	}

	@Override
	public void finish() {
		super.finish();
		LOGGER.info("#############################################################################################################");
		LOGGER.info("########## END MODULE ############## " + relativize() + " ############## END MODULE ##########");
		LOGGER.info("#############################################################################################################");
	}
	
	private static Path buildModuleHookName(Configuration conf) {
		return PathUtils.get(conf.getModuleDir(), "Module" + HOOK_SUFFIX);
	}	
}
