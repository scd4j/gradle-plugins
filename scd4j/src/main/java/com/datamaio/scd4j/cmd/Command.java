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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.datamaio.scd4j.util.io.FileUtils;

/**
 * 
 * @author Fernando Rubbo
 */
public abstract class Command {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);	
	private static final Interaction NO_PRINTING = new Interaction() {
		@Override
		boolean shouldPrintCommand() {
			return false;
		}
		@Override
		boolean shouldPrintOutput() {
			return false;
		}		
	};
	
	public static Command INSTANCE;
	public static synchronized final Command get() {
		if(INSTANCE==null) {
			String os = System.getProperty("os.name");
			if(os.toUpperCase().contains("LINUX")) {
				List<String> cmdList = Arrays.asList("uname -a".split(" "));			
				String dist = _run(cmdList, NO_PRINTING);
				if(dist.contains(UbuntuCommand.DIST_NAME)) {
					INSTANCE = new UbuntuCommand();
				} else if (dist.contains(DebianCommand.DIST_NAME)) {
					INSTANCE = new DebianCommand();
				} else if (dist.contains(CentosCommand.DIST_NAME)) {
					INSTANCE = new CentosCommand();					
				} else if(dist.contains(RedhatCommand.DIST_NAME)) {
					INSTANCE = new RedhatCommand();
				} else {
					throw new RuntimeException("Linux distribution not supported : " + dist);
				}
			} else {
				INSTANCE = new WindowsCommand();
			}
		}
		return INSTANCE;
	}

	public abstract String osname();
	public abstract boolean isLinux();
	public abstract boolean isWindows();
	
	public abstract void serviceStart(String name);
	public abstract void serviceStop(String name);
	public abstract void serviceRestart(String name);
	public abstract String serviceStatus(String name);

	public abstract void startServiceAtSystemBoot(String name);
	public abstract void doNotStartServiceAtSystemBoot(String name);
	
	public abstract String distribution();
	public abstract void execute(String file);
	public abstract void addRepository(String repository);
	public abstract void installRemotePack(String pack);
	public abstract void installRemotePack(String pack, String version);
	public abstract void installLocalPack(String path);
	public abstract boolean isInstalled(String pack);
	public abstract void uninstallRemotePack(String pack);
	public abstract void uninstallLocalPack(String pack);
	public abstract void unzip(String from, String toDir);
	public abstract void fixTextContent(String file);
	
	public abstract void groupadd(final String group);
	public abstract void groupadd(final String group, final String options);
	public abstract void useradd(final String user);
	public abstract void useradd(final String user, final String options);
	public abstract void userdel(final String user, final String options);	
	public abstract void passwd(final String user, final String passwd);
	
	public abstract void chmod(final String mode, final String file);
	public abstract void chmod(final String mode, final String file, boolean recursive);
	public abstract void chown(final String user, final String path);
	public abstract void chown(final String user, final String path, final boolean recursive);
	public abstract void chown(final String user, final String group, final String path);
	public abstract void chown(final String user, final String group, final String path, final boolean recursive);

	public abstract void ln(final String link, final String targetFile);
	
	public boolean exists(final String file){
		return Files.exists(Paths.get(file));
	}
	
	public String whoami() {
		return System.getProperty("user.name");
	}

	public void mkdir(String dir) {
		logCmdJava(format("mkdir %s", dir));  
		FileUtils.createDirectories(Paths.get(dir));
	}

	public void mv(String from, String to) {
		logCmdJava(format("mv %s to %s", from, to));
		FileUtils.move(Paths.get(from), Paths.get(to));
		LOGGER.info(String.format("Moving file %s to %s", from, to));
	}

	public List<String> ls(String path) {
		logCmdJava(format("ls %s", path));
		return FileUtils.ls(Paths.get(path)).stream().map(p -> p.toString()).collect(toList());
	}

	public void rm(String path) {
		logCmdJava(format("rm %s", path));
		FileUtils.delete(Paths.get(path));
	}

	public void cp(String from, String to) {
		logCmdJava(format("cp %s to %s", from, to));
		FileUtils.copy(Paths.get(from), Paths.get(to));
	}
	
	// --- run ---
	
	public String run(String cmd) {
		List<String> cmdList = Arrays.asList(cmd.split(" "));
		return run(cmdList);
	}
	
	public String run(List<String> cmdList) {
		return run(cmdList, true);
	}

	public String run(String cmd, final boolean prettyPrint) {
		List<String> cmdList = Arrays.asList(cmd.split(" "));
		return run(cmdList, prettyPrint);
	}
	
	public String run(List<String> cmdList, final boolean shouldPrintOutput) {
		return run(cmdList, new Interaction() {
			@Override
			boolean shouldPrintOutput() {
				return shouldPrintOutput;
			}
		});
	}

	public String run(String cmd, final int... successfulExec) {
		List<String> cmdList = Arrays.asList(cmd.split(" "));
		return run(cmdList, successfulExec);
	}
	
	public String run(List<String> cmdList, final int... successfulExec) {
		return run(cmdList, new Interaction() {
			@Override
			boolean isTheExecutionSuccessful(int waitfor) {
				return Arrays.binarySearch(successfulExec, waitfor) != -1;
			}
		});
	}
	
	public String run(String cmd, Interaction interact) {
		List<String> cmdList = Arrays.asList(cmd.split(" "));
		return run(cmdList, interact);
	}
	
	public String run(List<String> cmdList, Interaction interact) { 
		return _run(cmdList, interact);
	}
	
	// --- run with no iteraction ---
	
	/**
	 * Este metodo nao faz interacao nenhuma. Isto é, ele não mostra o output e
	 * nem o erro. Mas se o retorno do comando for diferente de 0, ele continua
	 * lancando uma exception
	 *
	 * OBS> este metodo foi criado pois alguns executaveis travavam lendo o
	 * output
	 */
	public String runWithNoInteraction(String cmd) {
		List<String> cmdList = Arrays.asList(cmd.split(" "));
		return runWithNoInteraction(cmdList);
	}
		
	public String runWithNoInteraction(List<String> cmdList) {
		return run(cmdList, (Interaction) null);
	}


	// ----------- Private methods -------------
	
	private static String _run(List<String> cmd, Interaction interact) {
		ThreadedStreamHandler handler = null;
		ThreadedStreamHandler errorHandler = null;
		OutputStream out = null;
		Path tempPath = null;
		StringBuilder noInteractionHandler = new StringBuilder();
		try {
			if (interact == null) {
				// se não tem iteração joga o conteúdo no arquivo para
				// depois ler
				tempPath = Files.createTempFile("cmd", ".out");
			}

			final Process process = run(cmd, tempPath, interact.shouldPrintCommand());
			if (interact != null) {
				InputStream in = process.getInputStream();
				InputStream ein = process.getErrorStream();
				out = process.getOutputStream();

				handler = new ThreadedStreamHandler(in, interact.shouldPrintOutput());
				errorHandler = new ThreadedStreamHandler(ein, interact.shouldPrintOutput());
				handler.start();
				errorHandler.start();

				// se o usuario definiu algum tipo de interacao
				interact.interact(out);
				out.flush();
			}
			int waitFor = process.waitFor();

			if (interact != null) {
				handler.interrupt();
				errorHandler.interrupt();
				handler.join();
				errorHandler.join();
			} else {
				// se não teve interação, imprime todo o stdout aqui após finalizar o processo
				// isto é necessário para o usuário saber o que está acontecendo
				for (String line : Files.readAllLines(tempPath, Charset.defaultCharset())) {
					LOGGER.info("\t\t" + line);
					noInteractionHandler.append(line).append("\n");
				}
			}

			if (interact != null) {
				if (!interact.isTheExecutionSuccessful(waitFor)) {
					throwExecutionException(errorHandler, waitFor);
				}
			} else if (waitFor != 0) {
				throwExecutionException(errorHandler, waitFor);
			}
			return (interact != null) ? handler.getOutput() : noInteractionHandler.toString();
		} catch (Exception e) {
			String msg = "Error executing command: " + cmd2String(cmd) + ".";
			throw new RuntimeException(msg, e);
		} finally {
			quitellyClose(out);
			deleteTempFile(tempPath);
		}
		
	}

	private static void deleteTempFile(Path tempPath) {
		if (tempPath != null) {
			// se não teve interação, apaga o arquivo temporário
			try {
				Files.delete(tempPath);
			} catch (IOException e) {
				// ignore
			}
		}
	}

	private static void throwExecutionException(ThreadedStreamHandler errorHandler, int waitFor) {
		String output = "The process has ended with status: " + waitFor + " | Output: " + errorHandler.getOutput();
		throw new RuntimeException(output);
	}

	private static void quitellyClose(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	private static Process run(List<String> cmd, Path tempPath, boolean shouldPrint) throws IOException {
		if(shouldPrint) {
			LOGGER.info("\tExecuting cmd: " + cmd2String(cmd));
		}

		final ProcessBuilder pb = new ProcessBuilder(cmd);
		if (tempPath != null) {
			// entra aqui quando não tem interação..
			// motivo: salva em um arquivo o stdout para imprimir tudo no final
			// isto foi necessário pois alguns casos o programa travava com interação
			pb.redirectErrorStream(true);
			pb.redirectOutput(tempPath.toFile());
			if(shouldPrint) {
				LOGGER.info("\t\t!!! WHAIT !!! "
					+ "This command will print the result only at the end of its execution!"
					+ "Temp file output: " + tempPath);
			}
		}

		return pb.start();
	}

	private static String cmd2String(List<String> cmd) {
		final StringBuilder builder = new StringBuilder();
		for (String st : cmd) {
			builder.append(st).append(" ");
		}
		return builder.toString();
	}
	
	private void logCmdJava(String msg) {
		LOGGER.info(String.format("\tExecuting cmd: %s (JAVA) ", msg));
	}

	
	private static class ThreadedStreamHandler extends Thread {

		private final InputStream in;
		private boolean shouldPrint = false;
		private final StringBuilder output = new StringBuilder();

		ThreadedStreamHandler(InputStream in, boolean shouldPrint) {
			this.in = in;
			this.shouldPrint = shouldPrint;
		}

		@Override
		public void run() {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					if (shouldPrint) {
						LOGGER.info("\t\t" + line);
					}
					output.append(line + "\n");
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public String getOutput() {
			return output.toString();
		}
	}
}