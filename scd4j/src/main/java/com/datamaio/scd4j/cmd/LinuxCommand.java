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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class LinuxCommand extends Command {

	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public String service(String name, ServiceAction action){
    	return run("service " + name + " " + action);
	}
	
	@Override
	public void execute(String file) {
		Path path = Paths.get(file);
		try {
			String oldPosix = PosixFilePermissions.toString(Files.getPosixFilePermissions(path));		
			Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwxrwxrwx"));
			try {
				run(file);
			} finally {			
				Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(oldPosix));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}			
	}
	
	public void chmod(String mode, String file) {
		chmod(mode, file, false);
	}

	public void chmod(String mode, String file, boolean recursive) {
		List<String> cmd = new ArrayList<String>();
		cmd.add("chmod");
		if (recursive) {
			cmd.add("-R");
		}
		cmd.add(mode);
		cmd.add(file);
		run(cmd);

		if (file.endsWith(".sh")) {
			// executa este cara apenas para garantir que se alguem salvou no windows
			// este arquivo possa ser executado no Linux
			normalizeTextContent(file);
		}
	}

	public void normalizeTextContent(String file) {
		if(!Files.exists(Paths.get("/usr/bin/dos2unix"))) {
			install("dos2unix");
		}
		
		List<String> cmd = new ArrayList<String>();
		cmd.add("dos2unix");
		cmd.add(file);
		run(cmd, false);
	}

	public void chown(String user, String file) {
		chown(user, file, false);
	}

	public void chown(String user, String file, boolean recursive) {
		chown(user, user, file, recursive);
	}

	public void chown(String user, String group, String file, boolean recursive) {
		List<String> cmd = new ArrayList<String>();
		cmd.add("chown");
		if (recursive) {
			cmd.add("-R");
		}
		cmd.add(user + ":" + group);
		cmd.add(file);
		run(cmd);
	}

	public void groupadd(final String group) {
		groupadd(group, null);
	}

	public void groupadd(final String group, final String options) {
		run("groupadd " + (options != null ? options : "-f") + " " + group);
	}

	public void useradd(final String user) {
		useradd(user, null);
	}

	public void useradd(final String user, final String options) {
		try {
			run("id " + user, false);
			LOGGER.info("\tUser already exists. It will not be created again.");
		} catch (Exception e) {
			run("useradd " + (options != null ? options : "") + " " + user);
		}
	}

	/**
	 * OBS IMPORTANTE: Se o selinux estiver ligado este método não funciona.
	 */
	public void passwd(final String user, final String passwd) {
		List<String> cmd = Arrays.asList("passwd", user);
		run(cmd, new Interaction() {
			@Override
			void execute(OutputStream out) throws Exception {
				byte[] bytes = (passwd + "\n").getBytes();
				out.write(bytes); // entra a senha
				out.write(bytes); // confirma a senha
			}
		});
	}
	
	public void unzip(String from, String toDir) {
		LOGGER.info("\tUnziping " + from + " para " + toDir + " ... ");
		run("unzip -o " + from + " -d " + toDir);
	}
	

	// ----------- begin bash run --------

	/**
	 * Commando bash foi criado pois alguns comandos não são compreendidos
	 * corretamente pelo linux quando executados pelo java.
	 *
	 * Por exemplo o comando: echo '777777' > ~/test Quando executado
	 * diretamente no linux ele coloca o texto '777777' dentro do arquivo test
	 * Quando executado pelo java ele imprime no output '777777' > ~/test
	 *
	 * Forma de usar: bash("echo '777777' > ~/test")
	 */
	public String bash(String cmd) {
		return bash(cmd, true);
	}

	public String bash(String cmd, final boolean printOutput) {
		return run("bash -c \"" + cmd + "\"", printOutput);
	}

	public String bash(String cmd, final int... successfulExec) {
		return run("bash -c \"" + cmd + "\"", successfulExec);
	}

	public String bashWithNoInteraction(String cmd) {
		return run("bash -c \"" + cmd + "\"", (Interaction) null);
	}

	// ---------- end bash run -------

	
	public void install(String pack) {
		install(pack, null);
	}	
	
}
