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
package com.datamaio.scd4j.hooks.file;

import static com.datamaio.scd4j.conf.Configuration.HOOK_SUFFIX;
import static com.datamaio.scd4j.conf.Configuration.MODULES_FOLDER;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.datamaio.scd4j.cmd.Command;
import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.hooks.HookEvaluator;
import com.datamaio.scd4j.util.PathHelper;

/**
 * 
 * @author Fernando Rubbo
 */
public class FileHookEvaluator extends HookEvaluator {
	private static final Logger LOGGER = Logger
			.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private Path src;
	private PathHelper pathHelper;

	public FileHookEvaluator(final Path src, final Path target,
			final Configuration conf) {
		super(Paths.get(src + HOOK_SUFFIX), buildBinding(src, target), conf);
		this.src = src;
		this.pathHelper = new PathHelper(conf);
	}

	@Override
	protected String getScriptBaseClass() {
		return FileHook.class.getName();
	}

	public boolean pre() {
		LOGGER.info("INSTALLING: " + relativize());
		LOGGER.info(" :PRE");
		boolean pre = super.pre();
		if (!pre) {
			LOGGER.info(" :POST (Skipped)");
		}
		return pre;
	}

	public void post() {
		LOGGER.info(" :POST");
		super.post();
	}

	private static Map<String, Object> buildBinding(final Path src,
			final Path target) {
		Map<String, Object> map = new HashMap<>();
		map.put("src", src.toString());
		map.put("target", target.toString());
		return map;
	}

	private String relativize() {
		String relative = src.toString();
		Path p = Paths.get(relative.substring(relative.indexOf(MODULES_FOLDER
				+ File.separator)));

		String path = p.subpath(2, p.getNameCount()).toString();

		return pathHelper.replaceVars(path);
	}
}
