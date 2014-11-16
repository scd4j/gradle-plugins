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
package com.datamaio.scd4j.cmd;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class UbuntuCommand extends LinuxCommand {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static final String DIST_NAME = "Ubuntu";
	
	public void uninstall(String pack) {
		LOGGER.info("\tRemoving package " + pack + " and dependencies");
		run("apt-get -y purge " + pack);
		run("apt-get -y autoremove");
	}
	
	public void install(String pack, String version) {
		LOGGER.info("\tInstalling package " + pack + (version!=null? " ("+version+")" : ""));
		String fullpack = pack + (version!=null? "=" + version : "");
		List<String> cmd = Arrays.asList(new String[] { "apt-get", "-y", "install", fullpack });
		run(cmd);
	}	
	
	@Override	
	public void installFromLocalPath(String path) {
		LOGGER.info("\tInstalling DEB File from " + path + " ... ");
		run("dpkg -i " + path);
	}
	
	@Override
	public String distribution() {
		return DIST_NAME;
	}

	@Override
	public void addRepository(String repository) {
		run("add-apt-repository -y " + repository);
		run("apt-get update", false);
	}
}
