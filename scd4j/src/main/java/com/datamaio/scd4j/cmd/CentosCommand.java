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

/**
 * 
 * @author Fernando Rubbo
 */
public class CentosCommand extends LinuxCommand {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static final String DIST_NAME = "CentOS";
	public static final String INST_EXTENSION = "rpm";

	@Override
	public String getPackExtension(){
		return INST_EXTENSION;
	}
	
	@Override
	public String distribution() {
		return DIST_NAME;
	}
	
	@Override
	public void startServiceAtSystemBoot(String serviceName) {
		run("chkconfig --add " + serviceName +
				" && chkconfig " + serviceName + " on " + 
				" && chkconfig --list " + serviceName);
	}
	
	@Override
	public void doNotStartServiceAtSystemBoot(String serviceName) {
		run("chkconfig --del " + serviceName );
	}
	
	@Override
	public void installRemotePack(String pack, String version) {
		LOGGER.info("\tInstalling package " + pack + (version!=null? " ("+version+")" : ""));
		String fullpack = pack + (version!=null? "-" + version : "");
		List<String> cmd = Arrays.asList(new String[] { "yum", "-y", "install", fullpack });
		run(cmd);
	}	
	
	@Override
	public void installLocalPack(String path) {
		LOGGER.info("\tInstalling RPM from " + path + " ... ");
		run("rpm -i " + path);
	}
	
	@Override
	public boolean isInstalled(String pack) {
		throw new RuntimeException("Function 'isLocalPackInstalled' not implemented for CentOS");
	}
	
	@Override
	public void uninstallRemotePack(String pack) {
		LOGGER.info("\tRemoving package " + pack);
		run("yum -y erase " +pack );
	}
	
	@Override
	public void uninstallLocalPack(String pack) {
		throw new RuntimeException("Function 'uninstallLocalPack' not implemented for CentOS");
	}
	
	@Override
	public void addRepository(String repository) {
		throw new RuntimeException("Not Implemented!");
	}
}
