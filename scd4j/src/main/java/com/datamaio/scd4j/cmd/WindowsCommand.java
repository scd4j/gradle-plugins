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

import java.nio.file.Paths;
import java.util.logging.Logger;

import com.datamaio.scd4j.util.io.ZipUtils;

/**
 * 
 * @author Fernando Rubbo
 * @author Mateus M. da Costa
 */
public class WindowsCommand extends Command {
	public static final String OS_NAME = "Windows";
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	@Override
	public  String osname() {
		return OS_NAME;
	}
	
	@Override
	public  boolean isLinux() {
		return false;
	}
	
	@Override
	public  boolean isWindows() {
		return true;
	}
	
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
		return System.getProperty("os.name");
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
		// TODO - Look at: http://technet.microsoft.com/en-us/library/bb490872.aspx
		// TODO - Look at: http://technet.microsoft.com/en-us/library/cc753525%28WS.10%29.aspx
	}

	@Override
	public void chmod(String mode, String file, boolean recursive) {
		// do nothing
	}

	@Override
	public void fixTextContent(String file) {
		throw new RuntimeException("Function 'normalizeTextContent' not implemented for windows");
	}

	@Override
	public void chown(final String user, final String file) {
		// TODO - Look at: http://technet.microsoft.com/pt-br/library/cc753525%28v=ws.10%29.aspx
		throw new RuntimeException("Function 'chown' is not implemented for windows yet!");
	}

	@Override
	public void chown(final String user, final String file, final boolean recursive) {
		// TODO - Look at: http://technet.microsoft.com/pt-br/library/cc753525%28v=ws.10%29.aspx
		throw new RuntimeException("Function 'chown' is not implemented for windows yet!");
	}

	@Override
	public void chown(final String user, final String group, final String file) {
		// TODO - Look at: http://technet.microsoft.com/pt-br/library/cc753525%28v=ws.10%29.aspx
		throw new RuntimeException("Function 'chown' is not implemented for windows yet!");
	}

	@Override
	public void chown(final String user, final String group, final String file, final boolean recursive) {
		// TODO - Look at: http://technet.microsoft.com/pt-br/library/cc753525%28v=ws.10%29.aspx
		throw new RuntimeException("Function 'chown' is not implemented for windows yet!");
	}
	
	@Override
	public void ln(String file, String link) {
		// TODO - Can use this: mklink. Look at: http://technet.microsoft.com/en-us/library/cc753194.aspx
		// This was tested in Windows 7 and worked...
		throw new RuntimeException("Function 'ln' is not implemented for windows yet!");
	}

	@Override
	public void groupadd(String group) {
		// TODO - Can use this: net localgroup GroupNameToAdd /add. Look at: http://technet.microsoft.com/en-us/library/cc725622.aspx
		// This was tested in Windows 7 and worked...
	}

	@Override
	public void groupadd(String group, String options) {
		// do nothing
	}

	@Override
	public void useradd(String user) {
		// TODO - Can use this: net user UserName password /add. Look at: http://support.microsoft.com/kb/251394
		// This was tested in Windows 7 and worked...
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
