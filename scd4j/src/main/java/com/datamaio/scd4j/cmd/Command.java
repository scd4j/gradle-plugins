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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;

import com.datamaio.scd4j.cmd.linux.debian.DebianCommand;
import com.datamaio.scd4j.cmd.linux.debian.UbuntuCommand;
import com.datamaio.scd4j.cmd.linux.redhat.CentosCommand;
import com.datamaio.scd4j.cmd.linux.redhat.FedoraCommand;
import com.datamaio.scd4j.cmd.linux.redhat.RedhatCommand;
import com.datamaio.scd4j.cmd.windows.WindowsCommand;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;

/**
 * 
 * @author Fernando Rubbo
 */
public abstract class Command {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);	
	private static final Interaction NO_PRINTING = new Interaction() {
		@Override
		public boolean shouldPrintCommand() {
			return false;
		}
		@Override
		public boolean shouldPrintOutput() {
			return false;
		}		
	};
	
	public static Command INSTANCE;
	public static synchronized final Command get() {
		if(INSTANCE==null) {
			String os = System.getProperty("os.name");
			if(os.toUpperCase().contains("LINUX")) {
				List<String> cmdList = Arrays.asList("cat /proc/version".split(" "));			
				String dist = _run(cmdList, NO_PRINTING);
				if(dist.toUpperCase().contains(UbuntuCommand.DIST_NAME.toUpperCase())) {
					INSTANCE = new UbuntuCommand();
				} else if (dist.toUpperCase().contains(DebianCommand.DIST_NAME.toUpperCase())) {
					INSTANCE = new DebianCommand();
				} else if (dist.toUpperCase().contains(FedoraCommand.DIST_NAME.toUpperCase())) {
					INSTANCE = new FedoraCommand();										
				} else if (dist.toUpperCase().contains(CentosCommand.DIST_NAME.toUpperCase())) {
					INSTANCE = new CentosCommand();					
				} else if(dist.toUpperCase().contains(RedhatCommand.DIST_NAME.toUpperCase())) {
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
	public abstract void userdel(final String user);
	public abstract void userdel(final String user, final String options);	
	public abstract void passwd(final String user, final String passwd);
	
	public abstract void chmod(final String mode, final String file);
	public abstract void chmod(final String mode, final String file, boolean recursive);
	public abstract void chown(final String user, final String path);
	public abstract void chown(final String user, final String path, final boolean recursive);
	public abstract void chown(final String user, final String group, final String path);
	public abstract void chown(final String user, final String group, final String path, final boolean recursive);

	public abstract void ln(final String link, final String targetFile);
	
	protected abstract void replaceLineSeparator(String file);
	
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
			public boolean shouldPrintOutput() {
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
			public boolean isTheExecutionSuccessful(int waitfor) {
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
	
	/**
	 * Try to discover what is the machine IP.
	 * <p>
	 * Note that this method has unexpected behavior if you have more than one network card/ip 
	 * 
	 * @return the ip address
	 */
    public String whatIsMyIp()
    {
        try {
        	return getLocalHostLANAddress().getHostAddress().trim();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
    }
    
	/**
	 * Try to discover what is the machine host name.
	 * <p>
	 * Note that this method has unexpected behavior if you have more than one network card/ip
	 *  
	 * @return the ip address
	 */
    public String whatIsMyHostName()
    {
        try {
			final InetAddress addr = InetAddress.getLocalHost();
	        return addr.getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
    }

	// ----------- Protected methods -------------
	
	protected void wait(BooleanSupplier supplier) {
		wait(supplier, 45);
	}
	
	protected void wait(BooleanSupplier supplier, int maxRetries) {
		int count = 0;
		while(count < maxRetries) {
			if( supplier.getAsBoolean() ){
				break;
			} 
			count++;
			sleep(1000);
		}
	}
	
	protected void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}


	// ----------- Private methods -------------
	
	private static final class ProcessInteraction extends Interaction {
		private final Interaction interaction;
		private final Path noIteractionTempFile;
		
		ProcessInteraction(Interaction interaction){
			this.interaction = interaction;
			this.noIteractionTempFile = buildTemp();
		}
		
		boolean hasInteraction(){
			return interaction != null;
		}
		Path getNoIteractionTempFile(){
			return noIteractionTempFile;
		}
		void cleanup() {
			if (!hasInteraction()) {
				try {
					Files.delete(noIteractionTempFile);
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		@Override
		public boolean shouldPrintCommand() {
			return interaction.shouldPrintCommand();
		}
		@Override
		public boolean shouldPrintOutput() {
			return interaction.shouldPrintOutput();
		}
		@Override
		public void interact(OutputStream out) throws Exception {
			interaction.interact(out);
		}
		@Override
		public boolean isTheExecutionSuccessful(int processReturn) {
			if(hasInteraction()) {
				return interaction.isTheExecutionSuccessful(processReturn);
			} 
			return processReturn == 0;
		}
		
		private Path buildTemp() {
			try {
				Path tempPath = null;
				if (!hasInteraction()) {
					// if do not have interaction, put the output in a temp file
					tempPath = Files.createTempFile("cmd", ".out");
				}
				return tempPath;
			} catch (IOException e) {
				throw new RuntimeException("Error creating output commant temp file", e);
			}
		}
	}
	
	private static String _run(List<String> cmd, Interaction inter) {
		ProcessInteraction pInter = new ProcessInteraction(inter);
		
		OutputStream out = null;
		ThreadedStreamHandler cmdStdHandler = null;
		ThreadedStreamHandler cmdErrorHandler = null;
		String cmdOutput = "";
		
		try {
			final Process process = startProcess(cmd, pInter);
			if (pInter.hasInteraction()) {
				cmdStdHandler = new ThreadedStreamHandler(process.getInputStream(), pInter.shouldPrintOutput());
				cmdErrorHandler = new ThreadedStreamHandler(process.getErrorStream(), pInter.shouldPrintOutput());
				cmdStdHandler.start();
				cmdErrorHandler.start();

				out = process.getOutputStream();
				pInter.interact(out);				
				out.flush();
			}
			
			int waitFor = process.waitFor();

			if (pInter.hasInteraction()) {
				cmdOutput = readInteractionCmdOutput(cmdStdHandler, cmdErrorHandler);
			} else {
				cmdOutput = readNoInteractionCmdOutput(pInter.getNoIteractionTempFile());
			}

			if(!pInter.isTheExecutionSuccessful(waitFor)){
				String error = pInter.hasInteraction() ? cmdErrorHandler.getOutput() : cmdOutput;
				throwExecutionException(waitFor, error);	
			}
			
			return cmdOutput;
		} catch (Exception e) {
			String msg = "Error executing command: " + cmd2String(cmd) + ".";
			throw new RuntimeException(msg, e);
		} finally {
			quitellyClose(out);
			pInter.cleanup();
		}		
	}
	
	static String readInteractionCmdOutput(ThreadedStreamHandler cmdStdHandler, ThreadedStreamHandler cmdErrorHandler) 
			throws InterruptedException {
		stopInteractionThreads(cmdStdHandler, cmdErrorHandler);
		return cmdStdHandler.getOutput();
	}

	static void stopInteractionThreads(ThreadedStreamHandler cmdStdHandler, ThreadedStreamHandler cmdErrorHandler)
			throws InterruptedException {
		cmdStdHandler.interrupt();
		cmdErrorHandler.interrupt();
		cmdStdHandler.join();
		cmdErrorHandler.join();
	}

	static String readNoInteractionCmdOutput(Path tempPath) throws IOException {
		// If did not have interaction, prints the standard output only at the end of the process 
		// execution. This is important because the user must know what was executed
		StringBuilder buff = new StringBuilder();
		for (String line : Files.readAllLines(tempPath, Charset.defaultCharset())) {
			LOGGER.info("\t\t" + line);
			buff.append(line).append("\n");
		}
		return buff.toString();
	}

	private static void throwExecutionException(int waitFor, String error) {
		String output = "The process has ended with status: " + waitFor + " | Output: " + error;
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

	private static Process startProcess(List<String> cmd, ProcessInteraction pInter) throws IOException {
		if(pInter.shouldPrintCommand()) {
			LOGGER.info("\tExecuting cmd: " + cmd2String(cmd));
		}

		final ProcessBuilder pb = new ProcessBuilder(cmd);				
		if (!pInter.hasInteraction()) {
			Path temp = pInter.getNoIteractionTempFile();			
			pb.redirectOutput(temp.toFile());
			pb.redirectErrorStream(true);
			if(pInter.shouldPrintCommand()) {
				LOGGER.info("\t\t!!! WHAIT !!! "
					+ "This command will print the result only at the end of its execution!"
					+ "Temp file output: " + temp);
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
	
	/**
	 * FROM: discussion https://issues.apache.org/jira/browse/JCS-40
	 * 
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
     * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
     * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
     * specify the algorithm used to select the address returned under such circumstances, and will often return the
     * loopback address, which is not valid for network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
     * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
     * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
     * first site-local address if the machine has more than one), but if the machine does not hold a site-local
     * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
     * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
     * <p/>
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        }
                        else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        }
        catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }
}