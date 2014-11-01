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
package com.datamaio.scd4j.hooks;

import groovy.lang.Script;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.datamaio.scd4j.cmd.Command;
import com.datamaio.scd4j.cmd.ServiceAction;
import com.datamaio.scd4j.cmd.Command.Interaction;
import com.datamaio.scd4j.conf.ConfEnvironments;
import com.datamaio.scd4j.conf.Configuration;

public abstract class Hook extends Script {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final Map<String, String> HOSTS = new HashMap<String, String>();
	
	protected ConfEnvironments envs;
	protected Map<String, String> props;
	protected Configuration conf;
	private final Properties transientProps = new Properties();
	protected final Command command;

	public Hook(){
		command = Command.get();
	}

	public boolean pre() {return true;}
	public void post() {}
	public void finish() {
		Set<Object> keySet = transientProps.keySet();
		for (Object key : keySet) {
			props.remove(key);
		}
	}

	//---------------- init command delegates ---------------
	
	public String osname() {
		return Command.osname();
	}

	public boolean isLinux() {
		return Command.isLinux();
	}
	
	public boolean isWindows(){
        return Command.isWindows();
    }
	
	public void execute(String file) {
		command.execute(file);
	}
	
	public String distribution() {
		return command.distribution();
	}

	// TODO: mudar o nome para convert file to current platform. assim pode ser usado no windows também
	public void dos2unix(String file) {
		command.dos2unix(file);		
	}

	public void groupadd(final String group) {
		command.groupadd(group);
	}

	public void groupadd(final String group, final String options) {
		command.groupadd(group, options);
	}

	public void useradd(final String user) {
		command.useradd(user);
	}

	public void useradd(final String user, final String options) {
		command.useradd(user, options);
	}

	public void passwd(final String user, final String passwd) {
		command.passwd(user, passwd);
	}

	public void chmod(String mode, String file) {
		command.chmod(mode, file);
	}

	public void chmod(String mode, String file, boolean recursive) {
		command.chmod(mode, file, recursive);
	}

	public void chown(String user, String file) {
		command.chown(user, file);
	}

	public void chown(String user, String file, boolean recursive) {
		command.chown(user, file, recursive);
	}

	public void chown(String user, String group, String file, boolean recursive) {
		command.chown(user, group, file, recursive);
	}

	public void ln(final String linkFile, final String targetFile) {
		link(linkFile).to(targetFile);
	}
	
	public Destination link(final String linkFile) {
		return new Destination() {
			@Override
			public void to(String targetFile) {
				command.ln(linkFile, targetFile);				
			}
		};		
	}

	public boolean exists(String file){
		return command.exists(file);
	}
	
	public String whoami() {
		return command.whoami();
	}

	public void mkdir(String dir) {
		command.mkdir(dir);
	}

	public void mv(String from, String to) {
		move(from).to(to);
	}
	
	public Destination move(String from) {
		return new Destination() {
			@Override
			public void to(String to) {
				command.mv(from, to);				
			}
		};		
	}

	public List<String> ls(String path) {
		return command.ls(path);
	}

	public void rm(String path) {
		remove(path);
	}
	
	public void remove(String path) {
		command.rm(path);
	}
	
	public void cp(String from, String to) {
		copy(from).to(to);
	}
	
	public Destination copy(String from) {
		return new Destination() {
			@Override
			public void to(String to) {
				command.cp(from, to);				
			}
		};
	}

	// --- run ----

	public String run(String cmd) {
		return command.run(cmd);
	}

	public String run(List<String> cmdList) {
		return command.run(cmdList);
	}

	public String run(String cmd, final boolean printOutput) {
		return command.run(cmd, printOutput);
	}

	public String run(List<String> cmdList, final boolean printOutput) {
		return command.run(cmdList, printOutput);
	}

	public String run(String cmd, final int... successfulExec) {
		return command.run(cmd, successfulExec);
	}

	public String run(List<String> cmdList, final int... successfulExec) {
		return command.run(cmdList, successfulExec);
	}

	public String run(String cmd, Interaction interact) {
		return command.run(cmd, interact);
	}

	public String run(List<String> cmdList, Interaction interact) {
		return command.run(cmdList, interact);
	}

	/**
	 * Este metodo nao faz interacao nenhuma. Isto é, ele não mostra o output e
	 * nem o erro. Mas se o retorno do comando for diferente de 0, ele continua
	 * lancando uma exception
	 *
	 * OBS> este metodo foi criado pois alguns executaveis travavam lendo o
	 * output
	 */
	public String runWithNoInteraction(String cmd) {
		return command.runWithNoInteraction(cmd);
	}

	public String runWithNoInteraction(List<String> cmdList) {
		return command.runWithNoInteraction(cmdList);
	}

    // --- env methods ---

	protected boolean isDesenv(){
		return !isTst() && !isHom() && !isProd();
	}

	protected boolean isTst(){
		final String address = whatIsMyIp();
		return envs.isTst(address);
	}

	protected boolean isHom(){
		final String address = whatIsMyIp();
		return envs.isHom(address);
	}

	protected boolean isProd(){
		final String address = whatIsMyIp();
		return envs.isProd(address);
	}

    protected String whatIsMyIp()
    {
        try {
			final InetAddress addr = InetAddress.getLocalHost();
	        String ip = addr.getHostAddress();
	        if("127.0.0.1".equals(ip)){ 
	        	// Cai aqui quando tem no /etc/hosts a identificação do nome com 127.0.0.1
	        	ip = getIpFromDNS(addr.getHostName());
	        }
			return ip;
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
    }
    
    protected String whatIsMyHostName()
    {
        try {
			final InetAddress addr = InetAddress.getLocalHost();
	        return addr.getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void log(String msg) {
        LOGGER.info("\t" + msg);
    }
        
    // --- properties methods ---
    
	protected WithValue set(final String key){
		return new WithValue() {
			@Override
			public void with(Double value) {
				this.with(""+value);
			}
			
			@Override
			public void with(Integer value) {
				this.with(""+value);
			}
			
			@Override
			public void with(Boolean value) {
				this.with(""+value);
			}
			
			@Override
			public void with(String value) {
				props.put(key, value);
				transientProps.put(key, value);
			}
		};
	}

	protected WithValue permanentSet(final String key){
		return new WithValue() {
			@Override
			public void with(Double value) {
				this.with(""+value);
			}
			
			@Override
			public void with(Integer value) {
				this.with(""+value);
			}
			
			@Override
			public void with(Boolean value) {
				this.with(""+value);
			}
			
			@Override
			public void with(String value) {
				props.put(key, value);
			}
		};		
	}

	protected String get(final String key){
		if(props==null)
			return null;
	    return props.get(key);
	}

	protected boolean contains(final String key){
        return props.get(key)!=null;
    }
	

	// --- install methods ---

	// TODO: DSL => add "x" to linux repository
	public void addRepository(String repository) {
		command.addRepository(repository);
	}
	
	public void install(String pack) {
		command.install(pack);
	}

	public void uninstall(String pack) {
		command.uninstall(pack);
	}
	
	// TODO: DSL => resolve and install dependency "x"
	// nova forma do gradle 2.2-rc de ler um arquivo de texto de dentro de uma dependencia
	// config = resources.text.fromArchiveEntry(configurations.checkstyleConfig, "path/to/archive/entry.txt")
	// http://gradle.org/docs/release-candidate/dsl/org.gradle.api.resources.TextResourceFactory.html
	protected void installDependency(String depName) {
		String path = resolveDependency(depName);
		command.installFromLocalPath(path);
	}
	
	// TODO: DSL => resolve and unzip dependency "x" to dir "y"
	protected Destination unzipDependency(String depName) {
		return new Destination() {			
			@Override
			public void to(String dir) {
				String from = resolveDependency(depName);
				command.unzip(from, dir);				
			}
		};
	}
	
    protected String resolveDependency(String name) {
    	Path file = conf.getDependency(name);
    	if(file==null)
    		throw new RuntimeException("Could not resolve dependency: " + name);
    	
    	return file.toString();
    }
	
    // --- services ---
    
    protected void service(Map<String, String> m) { 
    	//TODO: validate the key.. must allow only ServiceAction strings
    	String action = m.keySet().iterator().next();    	
    	command.service(m.get(action), ServiceAction.valueOf(action));
    }


	// ------ private methods ------

	private String getIpFromDNS(String hostName) {
		if(!HOSTS.containsKey(hostName)) { 
			System.out.println("\t\t\tBuscando IP no DNS para o host " + hostName);
			final List<String> dnsRecs = getDNSRecs(hostName, "A");
			final String ip = dnsRecs.size()>0 ? dnsRecs.get(0) : "127.0.0.1";
			HOSTS.put(hostName, ip);
		}
		return HOSTS.get(hostName);
	}
	
	 /**
     * Rertorna todos os registros do DNS para um dado dominio
     *
     * @param domain domínio, e.g. xyz.dbserver.com.br, no qual você deseja conhecer os registros do DNS.
     * @param types  e.g."MX","A" para descrever quais registros vc deseja.
     * 			<ul>
     * 				<li> MX: o resultado contém a prioridade (lower better) seguido pelo mailserver
     * 				<li> A: o resultado contém apenas o IP
     * 			</ul>
     *
     * @return lista de resultados
     */
	private List<String> getDNSRecs(String domain, String... types) {
		
		List<String> results = new ArrayList<String>(15);

		try {
			final Hashtable<String, String> env = new Hashtable<String, String>();
			env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
			
			final DirContext ictx = new InitialDirContext(env);
			final Attributes attrs = ictx.getAttributes(domain, types);
			for (NamingEnumeration<? extends Attribute> e = attrs.getAll(); e.hasMoreElements();) {
				final Attribute a = (Attribute) e.nextElement();
				for (int i = 0; i < a.size(); i++) {
					results.add((String) a.get(i));
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
		if (results.size() == 0) {
			System.err.println("Falha para encontrar um registro no DNS para o domínio " + domain);
		}
		return results;
	}
    
	protected void setEnvs(ConfEnvironments envs) {
		this.envs = envs;
	}
	protected void setProps(Map<String, String> props) {
		this.props = props;
	}
	protected void setConf(Configuration conf) {
		this.conf = conf;
	}
	
	// ---- interfaces for dsl -----
	protected interface Destination {
		void to(String to);
	}
	interface WithValue {
		void with(String value);
		void with(Boolean value);
		void with(Integer value);
		void with(Double value);
	}
}
