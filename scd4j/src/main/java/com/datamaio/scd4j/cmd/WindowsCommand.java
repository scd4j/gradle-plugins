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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.datamaio.scd4j.util.io.ZipUtils;

/**
 * 
 * @author Fernando Rubbo
 */
public class WindowsCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	@Override
	public void serviceStart(String name){
		throw new RuntimeException("Function 'service start' not implemented for windows");
	}
	
	@Override
	public void serviceStop(String name){
		throw new RuntimeException("Function 'service stop' not implemented for windows");
	}
	
	@Override
	public void serviceRestart(String name){
		throw new RuntimeException("Function 'service restart' not implemented for windows");
	}
	
	@Override
	public String serviceStatus(String name){
		throw new RuntimeException("Function 'service status' not implemented for windows");
	}
	
	@Override
	public void activeAtBoot(String serviceName) {
		throw new RuntimeException("Function 'activeAtBoot' not implemented for windows");
	}
	
	@Override
	public void deactiveAtBoot(String serviceName) {
		throw new RuntimeException("Function 'deactiveAtBoot' not implemented for windows");
	}
	
	@Override
	public void execute(String file) {
		run(file);
	}
	
	@Override
	public String distribution() {
		String OS_NAME = "OS Name:";
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("SYSTEMINFO");
			BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			String line = "";
			while ((line = in.readLine()) != null) {
				if (line.contains(OS_NAME)) {
					return line.substring(line.lastIndexOf(OS_NAME) + OS_NAME.length(), line.length() - 1);
				}
			}

			return "N/A";
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	@Override
	public void installRemotePack(String pack) {
		run(pack);
	}

	@Override
	public void installRemotePack(String pack, String version) {
		throw new RuntimeException("Function 'install' with version not implemented for windows");
	}
	
	@Override
	public boolean isInstalled(String pack) {
		throw new RuntimeException("Function 'isLocalPackInstalled' not implemented for windows");
	}
	
	@Override
	public void uninstallRemotePack(String pack) {
		throw new RuntimeException("Function 'uninstall' not implemented for windows");
	}
	
	@Override
	public void uninstallLocalPack(String pack) {
		throw new RuntimeException("Function 'uninstallLocalPack' not implemented for CentOS");
	}

	@Override
	public void chmod(String mode, String file) {
		// do nothing
	}

	@Override
	public void chmod(String mode, String file, boolean recursive) {
		// do nothing
	}

	@Override
	public void normalizeTextContent(String file) {
		throw new RuntimeException("Function 'normalizeTextContent' not implemented for windows");
	}

	@Override
	public void chown(String user, String file) {
		// do nothing
	}

	@Override
	public void chown(String user, String file, boolean recursive) {
		// do nothing
	}

	@Override
	public void chown(String user, String group, String file, boolean recursive) {
		// do nothing
	}
	
	@Override
	public void ln(String file, String link) {
		throw new RuntimeException("Function 'ln' is not implemented for windows");
	}

	@Override
	public void groupadd(String group) {
		// do nothing
	}

	@Override
	public void groupadd(String group, String options) {
		// do nothing
	}

	@Override
	public void useradd(String user) {
		// do nothing
	}

	@Override
	public void useradd(String user, String options) {
		// do nothing
	}

	@Override
	public void passwd(String user, String passwd) {
		// do nothing
	}

	@Override
	public void unzip(String from, String toDir) {
		LOGGER.info("Descompactando " + from + " para " + toDir + " ... ");
		ZipUtils.unzip(Paths.get(from), Paths.get(toDir));
		LOGGER.info("Descompatacao concluida!");
	}

	@Override
	public void installLocalPack(String path) {
		run(path);
	}
	
	@Override
	public void addRepository(String repository) {
		throw new RuntimeException("Not Implemented!");
	}
}
