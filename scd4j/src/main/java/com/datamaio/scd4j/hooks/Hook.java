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
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.datamaio.scd4j.cmd.Command;
import com.datamaio.scd4j.cmd.Command.Interaction;
import com.datamaio.scd4j.cmd.ServiceAction;
import com.datamaio.scd4j.conf.ConfEnvironments;
import com.datamaio.scd4j.conf.Configuration;

/**
 * This class is the "father" of all hook scripts.
 * <p>
 * This class publish the following:
 * <ul>
 * 	<li> {@link #pre()} and {@link #post()} methods to define hooks semantics, pre and post (module or file) respectively.
 *  <li> delegates and helper functions to be used in the {@link #pre()} and {@link #post()} implementation
 * </ul>
 * 
 * @author Fernando Rubbo
 */
public abstract class Hook extends Script {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final Map<String, String> HOSTS = new HashMap<String, String>();
	
	protected Configuration conf;
	protected ConfEnvironments envs;
	protected Map<String, String> props;	
	protected Map<String, String> temporaryProps;
	protected final Command command;

	/** Default constructor */
	public Hook(){
		this.command = Command.get();
	}
	
	//--------- method which can be overridden in hook files ------------

	/**
	 * Override this method whenever you need to:
	 * <ul>
	 * <li>Conditionally install a module or a file
	 * <li>Execute any programming logic before installing a module or a file. For example:
	 * <ul>
	 *  <li> Execute different logic according environment which we are running (dev, test, hom, prod)
	 *  <li> Execute different logic OS which we are running (Ubuntu, CentOs, Windows)
	 * 	<li> Stop a service (sometimes required to update a file, for example)
	 * </ul>
	 * </ul>
	 * 
	 * @return <code>true</code> to installing the respective module or file,
	 *         false otherwise. Default is <code>true</code>
	 */
	public boolean pre() {return true;}
	
	/**
	 * Override this method whenever you need to:
	 * <ul>
	 * <li>Execute any programming logic after installing a module or a file. For example:
	 * <ul>
	 * 	<li> Change the file permission
	 * 	<li> Rename or link the file
	 * 	<li> Start up a service
	 * </ul>
	 * </ul>
	 * 
	 * @return <code>true</code> to installing the respective module or file,
	 *         false otherwise. Default is <code>true</code>
	 */	
	public void post() {}	

	//---------------- init command delegates ---------------
	
	/** Rerturns the name of the OS. The same as Java System Property "os.name" */
	public String osname() {
		return Command.osname();
	}

	/** Returns <code>true</code> if the we are running in a Linux environment */
	public boolean isLinux() {
		return Command.isLinux();
	}
	
	/** Returns <code>true</code> if the we are running in a Windows environment */
	public boolean isWindows(){
        return Command.isWindows();
    }

	/** 
	 * Returns the distribution of the OS.
	 * For example, it may return "CentOS", "Ubuntu", "Windows XYZ", "N/A", etc 
	 */
	public String distribution() {
		return command.distribution();
	}
	
	/** 
	 * Execute the file. <br>
	 * If the file is not executable, we try to make it executable. 
	 * If it was not possible, an exception is thrown  
	 */
	public void execute(String file) {
		command.execute(file);
	}
	
	/** 
	 * DSL for {@link #normalizeTextContent(String)}
	 * <p>
	 * How to use this DSL:
	 * 
	 * <pre>
	 * normalize "/opt/test/my_text_file" content
	 * </pre>
	 */
	public Normalization normalize(String file){
		return new Normalization() {			
			@Override
			public void content() {
				normalizeTextContent(file);
			}
		};
	}
	
	/** 
	 * Convert a text file into the patterns of the OS we are running on<br>
	 * This is mostly required whenever you create a file in windows and than run it on Linux
	 * <p>
	 * Note: Usually config files are not an issue, but executable files are!!
	 * 
	 * @param file a text file
	 */
	public void normalizeTextContent(String file) {
		command.normalizeTextContent(file);		
	}

	/** 
	 * Creates a group of users
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param group the name of the group
	 */
	public void groupadd(final String group) {
		command.groupadd(group);
	}

	/**
	 * Creates a group of users with options.
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param group the name of the group
	 * @param options the options you would pass in the line command
	 */
	public void groupadd(final String group, final String options) {
		command.groupadd(group, options);
	}

	/** 
	 * Creates an user
	 * <p>
	 * Note: Currently Linux only
	 */
	public void useradd(final String user) {
		command.useradd(user);
	}

	/**
	 * Creates an user with options. In the second param set the exactly options
	 * you would pass in the line command
	 * <p>
	 * Note: Currently Linux only
	 */
	public void useradd(final String user, final String options) {
		command.useradd(user, options);
	}

	/**
	 * Sets the user password
	 * <p>
	 * Note - 1: Currently Linux only<br>
	 * Note - 2: If nobody should read the password of a prod environment, for
	 * example, don't forget to encrypt it using <code>gradlew encrypt</code>.
	 * The see all possibilities, type <code>gradlew tasks</code>.
	 * Probably you are more interested in those which show up at
	 * <code>Others</code> section<br>
	 * Note - : In Linux, if SELinux is turned on, thi execution will fail
	 */
	public void passwd(final String user, final String passwd) {
		command.passwd(user, passwd);
	}

	/** 
	 * Changes the Posix File Permissions<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param mode is the posix definition, ex: "777"
	 * @param file is the file which we would like to change the permissions 
	 */
	public void chmod(String mode, String file) {
		command.chmod(mode, file);
	}

	/** 
	 * Changes the Posix File Permissions<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param mode is the posix definition, ex: "777"
	 * @param file is the file which we would like to change the permissions
	 * @param recursive if <code>true</code> apply the same rule for all sub dirs 
	 */
	public void chmod(String mode, String file, boolean recursive) {
		command.chmod(mode, file, recursive);
	}

	/** 
	 * Changes ownership of a file<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param user the new owner. the same information is used for the group
	 * @param file the file to change ownership 
	 */
	public void chown(String user, String file) {
		command.chown(user, file);
	}

	/** 
	 * Changes ownership of a file<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param user the new owner 
	 * @param group the new group
	 * @param file the file to change ownership 
	 * @param recursive if <code>true</code> apply the same rule for all sub dirs
	 */
	public void chown(String user, String group, String file, boolean recursive) {
		command.chown(user, group, file, recursive);
	}

	/** 
	 * DSL for {@link #ln(String, String)}
	 * <p>
	 * How to use this DSL:
	 * 
	 * <pre>
	 * link "/etc/init.d/my_text_link" to "/opt/test/my_existing_file" 
	 * </pre> 
	 */
	public Destination link(final String link) {
		return new Destination() {
			@Override
			public void to(String targetFile) {
				ln(link, targetFile);
			}
		};		
	}
	
	/** 
	 * Create a simbolic link<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param link the link path
	 * @param targetFile the file to be linked 
	 */
	public void ln(final String link, final String targetFile) {
		command.ln(link, targetFile);
	}
	
	/** Checks if the given file exists */
	public boolean exists(String file){
		return command.exists(file);
	}
	
	/** Returns the current user name */
	public String whoami() {
		return command.whoami();
	}

	/** Creates a directory */
	public void mkdir(String dir) {
		command.mkdir(dir);
	}

	/** 
	 * DSL for {@link #mv(String, String)}
	 * <p>
	 * How to use this DSL:
	 * <pre>
	 * move "/opt/test/my_existing_file" to "/opt/test/new_name" 
	 * move "/opt/test/my_existing_dir" to "/opt/test/new_name"
	 * </pre> 
	 */
	public Destination move(String from) {
		return new Destination() {
			@Override
			public void to(String to) {
				mv(from, to);	
			}
		};		
	}

	/**  Move a file or a directory */
	public void mv(String from, String to) {
		command.mv(from, to);
	}
	
	/** List the files of a directory */
	public List<String> ls(String path) {
		return command.ls(path);
	}

	/** 
	 * DSL for {@link #rm(String)}
	 * <p> 
	 * How to use this DSL:
	 * <pre>
	 * remove "/opt/test/my_existing_file" 
	 * remove "/opt/test/my_existing_dir"
	 * </pre>  
	 */
	public void remove(String path) {
		rm(path);
	}

	/** Remove a file or a directory */
	public void rm(String path) {
		command.rm(path);
	}
	
	/** 
	 * DSL for {@link #cp(String, String)}
	 * <p> 
	 * How to use this DSL:
	 * <pre>
	 * copy "/opt/test/my_existing_file" to "/opt/test2" 
	 * copy "/opt/test/my_existing_dir" to "/opt/test2"
	 * </pre>  
	 */
	public Destination copy(String from) {
		return new Destination() {
			@Override
			public void to(String to) {
				cp(from, to);
			}
		};
	}
	
	/** Copy a file or a directory from a destination to another */
	public void cp(String from, String to) {
		command.cp(from, to);
	}
	
	// --- run ----

	/** 
	 * Execute an line command. OS dependent.
	 * 
	 * @param cmd the line command 
	 */
	public String run(String cmd) {
		return command.run(cmd);
	}

	/** 
	 * Execute an line command. OS dependent.
	 * 
	 * @param cmd the line command
	 * @param printOutput you can choose to not print output in logs
	 */
	public String run(String cmd, final boolean printOutput) {
		return command.run(cmd, printOutput);
	}

	/** 
	 * Execute an line command. OS dependent
	 * 
	 * @param cmd the line command
	 * @param successfulExec a list of successful results. Linux default is 0
	 */
	public String run(String cmd, final int... successfulExec) {
		return command.run(cmd, successfulExec);
	}

	/**
	 * Execute an line command. OS dependent.
	 * <p>
	 * Note: in Linux, if SELinux is turned on, this execution will fail
	 * 
	 * @param cmd
	 *            the line command
	 * @param interact
	 *            allow you to programmatically interact with the process
	 *            similarly in a way a user would interact
	 */
	public String run(String cmd, Interaction interact) {
		return command.run(cmd, interact);
	}

	/**
	 * This method does no iteraction at all. In other words, it will only show
	 * the output when the process finish.
	 * <p>
	 * 
	 * Note - 1: This method was created because very rarely executions in Linux
	 * hangs reading output.<br>
	 * Note - 2: If possible use {@link #run(String)} variant
	 * methods once this one may be removed in future releases
	 */
	public String runWithNoInteraction(String cmd) {
		return command.runWithNoInteraction(cmd);
	}

    // --- env methods ---

	/**
	 * Returns <code>true</code> if the environment is development<br>
	 * In other words, if it is NOT Test environment, NOT Homologation
	 * environment and NOT Production environment.
	 * 
	 * @see {@link #isTest()}, {@link #isHom()}, {@link #isProd()}
	 */
	protected boolean isDesenv(){
		return !isTst() && !isHom() && !isProd();
	}

	/** 
	 * Returns <code>true</code> if the environment is Test<br>
	 * This is configured in the build.gradle file.
	 * <p>
	 * For example:
	 * <pre>
	 * scd4j {
	 * 	...
	 * 	env {
	 * 		prod = ["192.168.10.20", "192.168.10.21", "192.168.10.22", "192.168.10.23"]
	 * 		hom  = ["192.168.7.20", "192.168.7.21"]
	 * 		test = ["192.168.3.20", "192.168.3.21"] 
	 * 	}
	 * }
	 * </pre>
	 * 
	 * <br>
	 * Note: none of env ips are required. So if you have a very simple
	 * environment, just ignore it. Everything will be development 
	 */
	protected boolean isTst(){
		final String address = whatIsMyIp();
		return envs.isTst(address);
	}

	/** 
	 * Returns <code>true</code> if the environment is Homolog<br>
	 * This is configured in the build.gradle file.
	 * <p>
	 * For example:
	 * <pre>
	 * scd4j {
	 * 	...
	 * 	env {
	 * 		prod = ["192.168.10.20", "192.168.10.21", "192.168.10.22", "192.168.10.23"]
	 * 		hom  = ["192.168.7.20", "192.168.7.21"]
	 * 		test = ["192.168.3.20", "192.168.3.21"] 
	 * 	}
	 * }
	 * </pre>
	 * 
	 * <br>
	 * Note: none of env ips are required. So if you have a very simple
	 * environment, just ignore it. Everything will be development 
	 */
	protected boolean isHom(){
		final String address = whatIsMyIp();
		return envs.isHom(address);
	}

	/**
	 * Returns <code>true</code> if the environment is Production<br>
	 * This is configured in the build.gradle file.
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 * scd4j {
	 * 	...
	 * 	env {
	 * 		prod = ["192.168.10.20", "192.168.10.21", "192.168.10.22", "192.168.10.23"]
	 * 		hom  = ["192.168.7.20", "192.168.7.21"]
	 * 		test = ["192.168.3.20", "192.168.3.21"] 
	 * 	}
	 * }
	 * </pre>
	 * 
	 * <br>
	 * Note: none of env ips are required. So if you have a very simple
	 * environment, just ignore it. Everything will be development
	 */
	protected boolean isProd(){
		final String address = whatIsMyIp();
		return envs.isProd(address);
	}

	/**
	 * Try to discover what is the machine IP.
	 * <p>
	 * Note: In order to make this method work as expected the name of the
	 * machine must be in the DNS and you must have an entry in the /etc/hosts
	 * with ip 127.0.0.1 linked to that name
	 * 
	 * @return the ip address
	 */
    protected String whatIsMyIp()
    {
        try {
			final InetAddress addr = InetAddress.getLocalHost();
	        String ip = addr.getHostAddress();
	        if("127.0.0.1".equals(ip)){ 
	        	// this code will execute whenever we have in /etc/hosts a name linked to 127.0.0.1
	        	ip = getIpFromDNS(addr.getHostName());
	        }
			return ip;
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
    }
    
	/**
	 * Try to discover what is the machine host name.
	 * <p>
	 * Note: In order to make this method work as expected the name of the
	 * machine must be in the DNS and you must have an entry in the /etc/hosts
	 * with ip 127.0.0.1 linked to that name
	 * 
	 * @return the ip address
	 */
    protected String whatIsMyHostName()
    {
        try {
			final InetAddress addr = InetAddress.getLocalHost();
	        return addr.getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
    }

    /** Use this method for every log content. So it will be stored in the dir log */
    public void log(String msg) {
        LOGGER.info("\t" + msg);
    }
        
    // --- properties methods ---
    
    /**
	 * Sets a temporary ({@link #setTemporaryProperty(String, Object)}) or a
	 * permanent ({@link #setPermanentProperty(String, Object)}) property.<br>
	 *  
	 * <p>
	 * How to use this DSL:
	 * 
	 * <pre>
	 * set temporary: "my_key" with: "my_string_value"
	 * OR
	 * set temporary: "my_key" with: true
	 * OR
	 * set temporary: "my_key" with: 234
	 * OR
	 * set temporary: "my_key" with: 98.23
	 * 
	 * =======
	 * 
	 * set permanent: "my_key", with: "my_string_value"
	 * OR
	 * set permanent: "my_key", with: true
	 * OR
	 * set permanent: "my_key", with: 234
	 * OR
	 * set permanent: "my_key", with: 98.23	 
	 * </pre>
	 */
    protected void set(Map<String, ? extends Object> values){
    	Object value = values.get("with");
    	if(value==null){
    		throw new RuntimeException("Incorrect use of 'set' try: set temporary: \"MY_KEY\", with:\"MY_VALUE\"");
    	}
    	
    	String keyTemporary = (String)values.get("temporary");
    	if(keyTemporary!=null) {
    		setTemporaryProperty(keyTemporary, value);
    	} else {
    		String keyPermanent = (String)values.get("permanent");
    		if(keyPermanent!=null) {
    			setPermanentProperty(keyPermanent, value);	
    		} else {
    			throw new RuntimeException("Incorrect use of 'set' try: set temporary: \"MY_KEY\", with:\"MY_VALUE\"");
    		}
    	}
    }
    
	/**
	 * Sets a temporary/transient property.<br>
	 * A temporary/transient property stays only for a short period time. In other words,
	 * it is set before the Hook#pre() method be executed and its remains until
	 * the end of Hook#post() method.
	 */
    public void setTemporaryProperty(String key, Object value) {
		props.put(key, value.toString());
		temporaryProps.put(key, value.toString());
    }

    /**
	 * Sets a permanent/persistent property.<br>
	 * A permanent/persistent property, stays up until the end of the program.<br>
     */
    public void setPermanentProperty(String key, Object value) {
		props.put(key, value.toString());
    }
    
    /** 
     * Returns the value of a property.<br>
     * Properties can be set through programming (see {@link #set(String) method}) or via configuration file:
     * 
     * <pre>
	 * scd4j {
	 * 		install {
	 * 			... 
	 * 			config = "my_config_file"
	 * 		}
	 * }
	 * </pre>
     */
	protected String get(final String key){
		if(props==null)
			return null;
	    return props.get(key);
	}

    /** 
     * Checks if a given key was set as a property.<br>
     * Properties can be set through programming (see {@link #set(String) method}) or via configuration file:
     * 
     * <pre>
	 * scd4j {
	 * 		install {
	 * 			... 
	 * 			config = "my_config_file"
	 * 		}
	 * }
	 * </pre>
     */
	protected boolean contains(final String key){
        return props.get(key)!=null;
    }
	

	// --- install methods ---

	// TODO: DSL => add "x" to linux repository
	/**
	 * Add a new repository to the OS. So that it is possible to install
	 * software from different location than the distribution defaults
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 * addRepository "ppa:notepadqq-team/notepadqq"
	 * </pre>
	 *
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param repository
	 *            location
	 */
	public void addRepository(String repository) {
		command.addRepository(repository);
	}

	/**
	 * Install or update to the latest version of a new software.
	 * 
	 * @See {@link #install(String, String)}
	 * 
	 * @param pack the package name
	 */
	public void install(String pack) {
		command.install(pack);
	}

	/**
	 * Install or update to a specific version of a new software.
	 * <p>
	 * Important. Prefer this method over {@link #install(String)} because we
	 * must test it many times before installing in production. Without putting
	 * the version we can test a versio X and install in production a version Y.
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * <p>
	 * For example in Ubuntu:
	 * <pre>
	 * install "lxde", "0.5.0-4ubuntu4"
	 * </pre>
	 * 
	 * @param pack
	 *            the package name
	 * @param version
	 *            the version of the pack
	 */
	public void install(String pack, String version) {
		command.install(pack, version);
	}
	
	/** Uninstall an existing package */
	public void uninstall(String pack) {
		command.uninstall(pack);
	}
	
	// TODO: DSL => resolve and install dependency "x"
	// nova forma do gradle 2.2-rc de ler um arquivo de texto de dentro de uma dependencia
	// config = resources.text.fromArchiveEntry(configurations.checkstyleConfig, "path/to/archive/entry.txt")
	// http://gradle.org/docs/release-candidate/dsl/org.gradle.api.resources.TextResourceFactory.html
	
//	protected InstallVersion install2(String pack) {
//	return new InstallVersion() {			
//		@Override
//		public InstallFrom version(String version) {
//			return new InstallFrom(){
//				@Override
//				public void from(InstallRepository repo) {
//					if(repo == OS) {
//						install(pack, version);
//					} else {
//						installFromScd4j(pack + ":" + version);
//					}
//				}
//			};
//		}
//	};
//}
//interface InstallVersion {
//	InstallFrom version(String version);
//}
//interface InstallFrom {
//	void from(InstallRepository repo);
//}
//InstallRepository OS = InstallRepository.OS;
//InstallRepository SCD4J = InstallRepository.SCD4J;
//public static enum InstallRepository {
//	OS, SCD4J
//}
//
////install "lxde" version:"0.5.0-4ubuntu4" from OS
////install "org.wildfly:wildfly" version:"0.5.0-4ubuntu4" from SCD4J
////OU	
////install "wildfly" from SD4J version:"0.5.0-4ubuntu4" group "org.wildfly"	
	
	/**
	 * Install a package (rpm or deb) which was put in the Artifactory or Nexus.<br>
	 * In order to accomplish that, you need to resolve the dependency in build.gradle.
	 * <p>
	 * For example:
	 * <pre>
	 * dependencies {
	 * 	scd4j 'org.wildfly:wildfly:8.1.0.Final'
	 * }
	 * </pre>
	 * <p>
	 * Then in you hook you need to call
	 * <pre>
	 * installFromScd4j "org.wildfly:wildfly:8.1.0.Final"
	 * </pre>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param depName the full dependency name
	 */
	protected void installFromScd4j(String depName) {
		String path = downloadFromScd4j(depName);
		command.installFromLocalPath(path);
	}
		
	/**
	 * Install a zip which was put in the Artifactory or Nexus.<br>
	 * In order to accomplish that, you need to resolve the dependency in build.gradle.
	 * <p>
	 * For example:
	 * <pre>
	 * dependencies {
	 * 	scd4j 'org.wildfly:wildfly:8.1.0.Final@zip'
	 * }
	 * </pre>
	 * <p>
	 * Then in you hook you need to call
	 * <pre>
	 * unzipFromScd4j "org.wildfly:wildfly:8.1.0.Final"
	 * </pre>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param depName the full dependency name
	 */	
	// TODO: DSL => resolve and unzip dependency "x" to dir "y"
	protected Destination unzipFromScd4j(String depName) {
		return new Destination() {			
			@Override
			public void to(String dir) {
				String from = downloadFromScd4j(depName);
				command.unzip(from, dir);				
			}
		};
	}
	
	/**
	 * Downloads (uses cache to avoid download the same dependency many times) a
	 * dependency which was put in the Artifactory or Nexus.<br>
	 * In order to accomplish that, you need to resolve the dependency in
	 * build.gradle.
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 * dependencies {
	 * 	scd4j 'org.wildfly:wildfly:8.1.0.Final@zip'
	 * }
	 * </pre>
	 * <p>
	 * Then in you hook you need to call
	 * 
	 * <pre>
	 * downloadFromScd4j "org.wildfly:wildfly:8.1.0.Final"
	 * </pre>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param depName
	 *            the full dependency name
	 */		
    protected String downloadFromScd4j(String depName) {
    	Path file = conf.getDependency(depName);
    	if(file==null)
    		throw new RuntimeException("Could not resolve dependency: " + depName);
    	
    	return file.toString();
    }
	
    // --- services ---

    /** 
     * Starts an OS service
	 * <p>
	 * Note: Currently Linux only
	 *  
     * @param name the service name
     */
	public void start(String name) {
    	command.service(name, ServiceAction.start);
	}
    
    /** 
     * Stops an OS service
	 * <p>
	 * Note: Currently Linux only
	 *  
     * @param name the service name
     */
	public void stop(String name) {
    	command.service(name, ServiceAction.stop);
	}

    /** 
     * restart an OS service
	 * <p>
	 * Note: Currently Linux only
	 *  
     * @param name the service name
     */
	public void restart(String name) {
    	command.service(name, ServiceAction.restart);
	}
	
    /** 
     * Checks the OS service status
	 * <p>
	 * Note: Currently Linux only
	 *  
     * @param name the service name
     * @return status information of the service
     */
	public String status(String name) {
    	return command.service(name, ServiceAction.status);
	}

	// ------ methods used by the framework only ----
	
    /** Used only by {@link HookEvaluator} to set variables */
	void setConf(Configuration conf) {
		this.conf = conf;
		this.envs = conf.getEnvironments();
		this.props = conf.getProperties();
		this.temporaryProps = conf.getTemporaryProperties();
	}
	
	/** Used unically by {@link HookEvaluator} to cleanup transient properties */
	void finish() {
		temporaryProps.forEach((k, v) -> props.remove(k) );
		temporaryProps.clear();
	}
	
	// ---- interfaces for dsl -----
	
	protected interface Destination {
		void to(String to);
	}
	interface SetDuration {
		WithValue temporary(String key);
		WithValue permanent(String key);
	}
	interface Service {
		void start();
		void stop();
		void restart();
		String status();
	}
	interface WithValue {
		void with(String value);
		void with(Boolean value);
		void with(Integer value);
		void with(Double value);
	}
	interface Normalization {
		void content();
	}
	
	// ------ private methods ------

	private String getIpFromDNS(String hostName) {
		if (!HOSTS.containsKey(hostName)) {
			System.out.println("\t\t\tFinding IP in DNS for host " + hostName);
			final List<String> dnsRecs = getDNSRecs(hostName, "A");
			final String ip = dnsRecs.size() > 0 ? dnsRecs.get(0) : "127.0.0.1";
			HOSTS.put(hostName, ip);
		}
		return HOSTS.get(hostName);
	}

	/**
	 * Returns all registries from DNS for a given domain
	 *
	 * @param domain
	 *            domínio, e.g. xyz.datamaio.com, in which you want to know the
	 *            registries in DNS.
	 * @param types
	 *            e.g."MX","A" .
	 *            <ul>
	 *            <li>MX: the result contains the priority (lower better)
	 *            followed by mailserver
	 *            <li>A : the result contains the IP
	 *            </ul>
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
			LOGGER.severe("It was not possible to find a registry in DNS for domain " + domain);
		}
		return results;
	}
}
