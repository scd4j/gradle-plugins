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

import static com.datamaio.scd4j.hooks.Action.CONTINUE_INSTALLATION;
import groovy.lang.Closure;
import groovy.lang.Script;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.datamaio.scd4j.cmd.LinuxCommand;
import com.datamaio.scd4j.conf.Env;
import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.exception.DependencyNotFoundException;
import com.datamaio.scd4j.hooks.file.FileHook;
import com.datamaio.scd4j.hooks.module.ModuleHook;

/**
 * This class is the "father" of all hook scripts. A hook script is where you
 * can put your configuration/installation logic. In SCD4J we have two types of
 * hooks:
 * <ol>
 * <li> {@link ModuleHook}: one per module (optional)
 * <li> {@link FileHook}: one per file (optional)
 * </ol>
 * <p>
 * This class publishes the following:
 * <ul>
 * <li> {@link #pre(Closure)} and {@link #post(Closure)} methods. Those methods
 * may or may not be called into hook files in order to pre-define before and after
 * installation semantics, respectively
 * <li>Delegate and helper functions to be used inside of hook files
 * </ul>
 * 
 * @author Fernando Rubbo
 */
public abstract class Hook extends Script {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final Map<String, String> HOSTS = new HashMap<String, String>();
	
	protected Configuration conf;
	protected Env envs;
	protected Map<String, String> props;	
	protected Map<String, String> temporaryProps;
	protected final Command command;	
	protected Closure<Action> pre;
	protected Closure<Void> post;

	/** Default constructor */
	public Hook() {
		this.command = Command.get();
	}
	
	//---------------- pre and post closure setters ---------------
	
	/**
	 * This method sets the closure that must be executed before the module/file
	 * installation
	 * <p>
	 * You must call this method whenever you need to:
	 * <ul>
	 * <li>Conditionally install a module or a file
	 * <li>Execute any programming logic before a module or a file installation.
	 * For example:
	 * <ul>
	 * <li>Execute different logic according to the environment in which you are
	 * running on (development, testing, staging and/or production)
	 * <li>Execute different logic depending on the operational system you are
	 * running on (Ubuntu, CentOS, Windows)
	 * <li>Stop a service (sometimes required to update a file)
	 * <li>and many others...
	 * </ul>
	 * </ul>
	 * 
	 * @param closure
	 *            The closure containing the pre-condition logic. <br>
	 *            Note that this closure must return an {@link Action}. If your
	 *            implementation does not return anything we assume
	 *            {@link Action#CONTINUE_INSTALLATION}
	 */
	protected final void pre(Closure<Action> closure) {
		this.pre = closure;
	}	

	/**
	 * This method sets the closure that must be executed after the module/file
	 * installation
	 * <p>
	 * You must call this method whenever you need to:
	 * <ul>
	 * <li>Execute any programming logic after installing a module or a file.
	 * For example:
	 * <ul>
	 * <li>Change the file permission
	 * <li>Rename or link the just installed file
	 * <li>Start up a service
	 * <li>and many others...
	 * </ul>
	 * </ul>
	 * 
	 * @param closure
	 *            The closure containing the post-condition logic.
	 */
	protected final void post(Closure<Void> closure){
		this.post = closure;
	}

	//---------------- helper and delegates methods ---------------
	
	/**
	 * Returns the name of the operational system 
	 * 
	 * @return The OS name (i.e. Linux or Windows)
	 */
	public String osname() {
		return command.osname();
	}
	
	/**
	 * Returns <code>true</code> if you are running on a Linux environment,
	 * false otherwise.
	 */
	public boolean isLinux() {
		return command.isLinux();
	}
	
	/**
	 * Returns <code>true</code> if you are running on a Windows environment,
	 * false otherwise.
	 */
	public boolean isWindows() {
		return command.isWindows();
	}

	/**
	 * Returns the distribution of the operational system
	 * <br>
	 * For example, it may return "CentOS", "Ubuntu" OR "Windows <version>"
	 */
	public String distribution() {
		return command.distribution();
	}
	
	/**
	 * Executes the given file <br>
	 * If the file is not executable, we try to make it so (Linux only). If it is not
	 * possible, an exception is thrown.
	 * 
	 * @param file
	 *            The executable file path
	 */
	public void execute(final String file) {
		command.execute(file);
	}
	
	/**
	 * Converts the given text file (usually a script file) into the pattern
	 * defined by the operational system that you are running on.
	 * <p>
	 * This is mostly required whenever you create a file on Windows and then
	 * run it on Linux, or vice and versa. Note: Usually configuration files are
	 * not an issue (because they are read by a program that already understand
	 * those difference), but executable scripts on Linux are!
	 * 
	 * @param textFile
	 *            The text file path
	 */
	public void fixText(final String textFile) {
		command.fixTextContent(textFile);
	}

	/**
	 * Creates a group of users. <br />
	 * Note: Currently Linux only.
	 * 
	 * @param group
	 *            The name of the group.
	 */
	public void groupadd(final String group) {
		command.groupadd(group);
	}

	/**
	 * Creates a group of users with options. <br />
	 * Note: Currently Linux only.
	 * 
	 * @param group
	 *            The name of the group.
	 * @param options
	 *            The same options you would like to use in the command line.
	 */
	public void groupadd(final String group, final String options) {
		command.groupadd(group, options);
	}

	/**
	 * Creates an user. <br />
	 * Note: Currently Linux only.
	 * 
	 * @param user
	 *            The user name.
	 */
	public void useradd(final String user) {
		command.useradd(user);
	}

	/**
	 * Creates an user with options. <br />
	 * Note: Currently Linux only.
	 * 
	 * @param user
	 *            The user name.
	 * @param options
	 *            The same options you would like to use in the command line.
	 */
	public void useradd(final String user, final String options) {
		command.useradd(user, options);
	}

	/**
	 * Sets the user password. <br />
	 * <br />
	 * Important Notes:
	 * <ol>
	 * <li>Currently Linux only
	 * <li>if SELinux is turned on, this execution will fail
	 * <li>For Production environments it is a good practice to encrypt
	 * passwords in order to ensure that non authorized people could NOT read
	 * it. To accomplish that, use the command line
	 * <code>'gradlew encrypt'</code>. This will generate an encrypted
	 * property so that you can use it in your installation <br>
	 * In order to see other possibilities, type <code>'gradlew tasks'</code> in
	 * the command line. Probably you will be more interested in those which
	 * show up under <code>'Scd4j Tools tasks'</code> group
	 * </ol>
	 * 
	 * @param user
	 *            The user name.
	 * @param passwd
	 *            User's password.
	 */
	public void passwd(final String user, final String passwd) {
		command.passwd(user, passwd);
	}

	/**
	 * Changes the POSIX file permissions. It is not recursive.<br>
	 * Note: Currently Linux only.
	 * 
	 * @param mode
	 *            The POSIX definition (for example: "777").
	 * @param file
	 *            The file which you would like to change the permissions.
	 */
	public void chmod(final String mode, final String file) {
		command.chmod(mode, file);
	}

	/**
	 * Changes the POSIX file Permissions<br>
	 * Note: Currently Linux only.
	 * 
	 * @param mode
	 *            The POSIX definition (for example: "777")
	 * @param file
	 *            The file which you would like to change the permissions.
	 * @param recursive
	 *            If <code>true</code> apply the same rule for all sub
	 *            directories and files.
	 */
	public void chmod(final String mode, final String file, final boolean recursive) {
		command.chmod(mode, file, recursive);
	}

	/**
	 * Changes ownership of a file or a directory. It is not recursive.<br>
	 * Note: Currently Linux only.
	 * 
	 * @param user
	 *            The new file's owner.
	 * @param path
	 *            The path (file or directory) to change the ownership
	 */
	public void chown(final String user, final String path) {
		command.chown(user, path);
	}
	
	/**
	 * Changes ownership of a file, possibly recursively. <br>
	 * Note: Currently Linux only.
	 * 
	 * @param user
	 *            The new file's owner.
	 * @param path
	 *            The path (file or directory) to change the ownership.
	 * @param recursive
	 *            If <code>true</code> apply the same rule for all sub
	 *            directories and files.
	 */
	public void chown(final String user, final String path, final boolean recursive) {
		command.chown(user, path, recursive);
	}
	
	/**
	 * Changes ownership of a file. It is not recursive.<br>
	 * Note: Currently Linux only.
	 * 
	 * @param user
	 *            The new file's owner.
	 * @param group
	 *            The new group.
	 * @param path
	 *            The path (file or directory) to change ownership.
	 */
	public void chown(final String user, final String group, final String path) {
		command.chown(user, path, path);
	}

	/**
	 * Changes ownership of a file, possibly recursively.<br>
	 * Note: Currently Linux only.
	 * 
	 * @param user
	 *            The new file's owner.
	 * @param group
	 *            The new group.
	 * @param path
	 *            The path (file or directory) to change ownership.
	 * @param recursive
	 *            If <code>true</code> apply the same rule for all sub
	 *            directories and files.
	 */
	public void chown(final String user, final String group, final String path, final boolean recursive) {
		command.chown(user, group, path, recursive);
	}

	/**
	 * DSL for {@link #ln(String, String)}. <br />
	 * <br />
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
	 * Create a symbolic link <br>
	 * Note: Currently Linux only.
	 * 
	 * @param link
	 *            The link path.
	 * @param targetFile
	 *            The target path (file or directory) to be linked.
	 */
	public void ln(final String link, final String targetFile) {
		command.ln(link, targetFile);
	}

	/**
	 * Returns the current user name.
	 */
	public String whoami() {
		return command.whoami();
	}
	
	/**
	 * Checks if the given path (file or directory) exists.
	 * 
	 * @param file
	 *            The target path (file or directory).
	 * @return <code>true</code> if the file exists, <code>false</code>
	 *         otherwise
	 */
	public boolean exists(final String file) {
		return command.exists(file);
	}

	/**
	 * Creates a directory and all nonexistent parent directories first.
	 * 
	 * @param dir
	 *            Directory path to create.
	 */
	public void mkdir(final String dir) {
		command.mkdir(dir);
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
	public void remove(final String path) {
		rm(path);
	}

	/**
	 * Remove a file or a directory (include the parameterized path and all of
	 * its sub entries).
	 * 
	 * @param path
	 *            Path to file or directory to be exclude.
	 */
	public void rm(final String path) {
		command.rm(path);
	}

	/** 
	 * DSL for {@link #mv(String, String)}.
	 * <p>
	 * How to use this DSL:
	 * <pre>
	 * move "/opt/test/my_existing_file" to "/opt/test/new_name" 
	 * move "/opt/test/my_existing_dir" to "/opt/test/new_name"
	 * </pre> 
	 */
	public Destination move(final String from) {
		return new Destination() {
			@Override
			public void to(String to) {
				mv(from, to);	
			}
		};		
	}

	/**
	 * Move a origin path (file or a directory) to another path (must be the
	 * same type as <code>from</code> parameter).
	 * 
	 * @param from
	 *            Origin/source path.
	 * @param to
	 *            Destination path.
	 */
	public void mv(final String from, final String to) {
		command.mv(from, to);
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
	public Destination copy(final String from) {
		return new Destination() {
			@Override
			public void to(String to) {
				cp(from, to);
			}
		};
	}
	
	/**
	 * Copy a file or a directory from a destination to another.
	 * 
	 * @param from
	 *            Accepts a file path or a dependency.
	 * @param to
	 *            The destination directory
	 */
	public void cp(final String from, final String to) {
		String newFrom = from;
		if (!Files.exists(Paths.get(from))) {
			try {
				newFrom = resolve(from);
			} catch (DependencyNotFoundException e) {
				throw new RuntimeException("It was not possible to find file/dir '" + from + "' to copy!");
			}
		}
		command.cp(newFrom, to);
	}
	
	/** 
	 * DSL for {@link #ls(String)}
	 * <p>
	 * How to use this DSL:
	 * <pre>
	 * def files = list "/opt/test" 
	 * </pre>
	 */
	public List<String> list(final String path) {
		return ls(path);
	}
	
	/** 
	 * List the entries (files and directories) of a given directory path.
	 * 
	 * @param path Path to list entries.
	 * @return List of all files and directories matched.
	 */
	public List<String> ls(final String path) {
		return command.ls(path);
	}

	// --- run ----

	/** 
	 * Execute a command line. OS dependent.
	 * 
	 * @param cmd the command line  
	 */
	public String run(String cmd) {
		return command.run(cmd);
	}

	/** 
	 * Execute a command line. OS dependent.
	 * 
	 * @param cmd the command line  
	 * @param printOutput you can choose to not print output in logs informing <code>false</code>
	 */
	public String run(String cmd, final boolean printOutput) {
		return command.run(cmd, printOutput);
	}

	/** 
	 * Execute a command line. OS dependent.
	 * 
	 * @param cmd the command line  
	 * @param successfulExec a list of successful results. Linux default is 0.
	 */
	public String run(String cmd, final int... successfulExec) {
		return command.run(cmd, successfulExec);
	}

	/**
	 * Execute a command line. OS dependent.
	 * 
	 * <p>
	 * Note: in Linux, if SELinux is turned on, this execution will fail
	 * 
	 * @param cmd
	 *            the command line 
	 * @param interact
	 *            allow you to programmatically interact with the process
	 *            similarly in the way a user would interact
	 */
	public String run(String cmd, Interaction interact) {
		return command.run(cmd, interact);
	}

	/**
	 * This method does no iteraction at all. In other words, it will only show
	 * the output after the process has finish.
	 * <p>
	 * Important Notes:
	 * <ol>
	 * 	<li>This method was created because, in very rare executions in
	 * Linux, the process hangs reading output. We are still figuring out the
	 * issue, but we are expecting to resolve this with Java 9 new Process API<br>
	 * 	<li> If possible use {@link #run(String)} variant methods once this
	 * one may be removed in future releases
	 * </ol>
	 * 
	 * @param cmd the command line 
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
	 * @see {@link #isTesting()}, {@link #isStaging()}, {@link #isProduction()}
	 */
	protected boolean isDevelopment(){
		return !isTesting() && !isStaging() && !isProduction();
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
	 * 		...
	 * 		test = ["192.168.3.20", "192.168.3.21"] 
	 * 	}
	 * }
	 * </pre>
	 * 
	 * <br>
	 * Important Notes:
	 * <ol>
	 * 	<li>In order to make this method work as expected the name of the
	 * machine must be in the DNS and you must have an entry in the /etc/hosts
	 * with ip 127.0.0.1 using the same name you have used in DNS
	 * 	<li>None of env ips are required. So if you have a very simple
	 * environment, just ignore it. Everything will be development
	 * </ol>
	 */
	protected boolean isTesting(){
		final String address = whatIsMyIp();
		return envs.isTesting(address);
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
	 * 		...
	 * 		hom  = ["192.168.7.20", "192.168.7.21"]
	 * 	}
	 * }
	 * </pre>
	 * 
	 * <br>
	 * Important Notes:
	 * <ol>
	 * 	<li>In order to make this method work as expected the name of the
	 * machine must be in the DNS and you must have an entry in the /etc/hosts
	 * with ip 127.0.0.1 using the same name you have used in DNS
	 * 	<li>None of env ips are required. So if you have a very simple
	 * environment, just ignore it. Everything will be development
	 * </ol>
	 */
	protected boolean isStaging(){
		final String address = whatIsMyIp();
		return envs.isStagging(address);
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
	 * Important Notes:
	 * <ol>
	 * 	<li>In order to make this method work as expected the name of the
	 * machine must be in the DNS and you must have an entry in the /etc/hosts
	 * with ip 127.0.0.1 using the same name you have used in DNS
	 * 	<li>None of env ips are required. So if you have a very simple
	 * environment, just ignore it. Everything will be development
	 * </ol>
	 */
	protected boolean isProduction(){
		final String address = whatIsMyIp();
		return envs.isProduction(address);
	}

	/**
	 * Try to discover what is the machine IP.
	 * <p>
	 * Note: In order to make this method work as expected the name of the
	 * machine must be in the DNS and you must have an entry in the /etc/hosts
	 * with ip 127.0.0.1 using the same name you have used in DNS
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
	 * with ip 127.0.0.1 using the same name you have used in DNS
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

    /**
	 * Use this method for every log content. So it will be stored in the dir
	 * log<br>
	 * Please, do not use pintln. Otherwise you will print the log information
	 * only in the starndard output and you will will lose it if you close the console.
	 */
    public void log(String msg) {
        LOGGER.info("\t" + msg);
    }
        
    // --- properties methods ---
    
    /**
     * EXPERIMENTAL:
     * <br>
	 * Sets a temporary ({@link #setTempProperty(String, Object)}) or a
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
	 * set permanent: "my_key" with: "my_string_value"
	 * OR
	 * set permanent: "my_key" with: true
	 * OR
	 * set permanent: "my_key" with: 234
	 * OR
	 * set permanent: "my_key" with: 98.23	 
	 * </pre>
	 */
    protected void set(Map<String, ? extends Object> values){
    	Object value = values.get("with");
    	if(value==null){
    		throw new RuntimeException("Incorrect use of 'set' try: set temporary: \"MY_KEY\", with:\"MY_VALUE\"");
    	}
    	
    	String keyTemporary = (String)values.get("temporary");
    	if(keyTemporary!=null) {
    		setTempProperty(keyTemporary, value);
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
     * EXPERIMENTAL:
     * <br>
	 * Sets a temporary ({@link #setTempProperty(String, Object)}) or a
	 * permanent ({@link #setPermanentProperty(String, Object)}) property.<br>
	 *  
	 * <p>
	 * How to use this DSL:
	 * 
	 * <pre>
	 * set "my_key" with "my_string_value" _as temp
	 * OR
	 * set "my_key" with true _as temp
	 * OR
	 * set "my_key" with 234 _as temp
	 * OR
	 * set "my_key" with 98.23 _as temp
	 * 
	 * =======
	 *
	 * set "my_key" with "my_string_value" _as permanent
	 * OR
	 * set "my_key" with true _as permanent
	 * OR
	 * set "my_key" with 234  _as permanent
	 * OR
	 * set "my_key" with 98.23  _as permanent
	 * </pre>
	 */
    protected SetValue set(String key){
    	return new SetValue(){
			@Override
			public SetDuration with(String value) {
				return new SetDuration(){
					@Override
					public void _as(PropertyDuration d) {
						if(PropertyDuration.permanent.equals(d)){
							setPermanentProperty(key, value);
						} else {
							setTempProperty(key, value);
						}
					}
				};
			}
			@Override
			public SetDuration with(Boolean value) {
				return new SetDuration(){
					@Override
					public void _as(PropertyDuration d) {
						if(PropertyDuration.permanent.equals(d)){
							setPermanentProperty(key, ""+value);
						} else {
							setTempProperty(key, ""+value);
						}						
					}
				};
			}
			@Override
			public SetDuration with(Integer value) {
				return new SetDuration(){
					@Override
					public void _as(PropertyDuration d) {
						if(PropertyDuration.permanent.equals(d)){
							setPermanentProperty(key, ""+value);
						} else {
							setTempProperty(key, ""+value);
						}						
					}
				};
			}
			@Override
			public SetDuration with(Double value) {
				return new SetDuration(){
					@Override
					public void _as(PropertyDuration d) {
						if(PropertyDuration.permanent.equals(d)){
							setPermanentProperty(key, ""+value);
						} else {
							setTempProperty(key, ""+value);
						}						
					}
				};
			}    		
    	};
    }
    protected PropertyDuration permanent = PropertyDuration.permanent;
    protected PropertyDuration temp = PropertyDuration.temp;    
    enum PropertyDuration {permanent, temp}
    protected interface SetDuration{
    	void _as(PropertyDuration d); //TODO: can not use 'as' because it is a keyword in groovy
    }
    protected interface SetValue {
		SetDuration with(String value);
		SetDuration with(Boolean value);
		SetDuration with(Integer value);
		SetDuration with(Double value);
	}  
    
    /**
     * Delegates to {@link #setTempProperty(String, Object)} 
     */
    public void set(String key, Object value) {
    	setTempProperty(key, value);
    }
    
	/**
	 * Sets a temporary/transient property.<br>
	 * A temporary/transient property stays only for a short period of time. In
	 * other words, if it is set in Hook#pre() method, it will remain
	 * until the end of Hook#post() method execution.
	 */
    public void setTempProperty(String key, Object value) {
		props.put(key, value.toString());
		temporaryProps.put(key, value.toString());
    }

    /**
	 * Sets a permanent/persistent property.<br>
	 * A permanent/persistent property stays up until the end of the program.<br>
     */
    public void setPermanentProperty(String key, Object value) {
		props.put(key, value.toString());
    }
    
    /** 
     * Returns the value of a property.<br>
	 * Properties can be set through programming (see
	 * {@link #setTempProperty(String, Object)} and {@link #setPermanentProperty(String,
	 * Object)} methods) or via configuration file:
     * 
     * <pre>
	 * scd4j {
	 * 	install {
	 * 		... 
	 * 		config = "my_config_file.conf"
	 * 	}
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
	 * Properties can be set through programming (see
	 * {@link #setTempProperty(String, Object)} and {@link #setPermanentProperty(String,
	 * Object)} methods) or via configuration file:
	 * 
	 * <pre>
	 * scd4j {
	 * 	install {
	 * 		... 
	 * 		config = "my_config_file.conf"
	 * 	}
	 * }
	 * </pre>
	 */
	protected boolean contains(final String key){
        return props.get(key)!=null;
    }
	

	// --- install methods ---

	/**
	 * Configures a given service to start at system boot<br>
	 * This method used default levels for each operational system
	 * <p>
	 * Note: Currently Linux only
	 *   	
	 * @param serviceName the name of the service
	 */
	public void activeAtBoot(String serviceName) {
		command.activeAtBoot(serviceName);
	}

	/**
	 * Configures a given service to not start at system boot<br>
	 * This method used default levels for each operational system
	 * <p>
	 * Note: Currently Linux only
	 *   	
	 * @param serviceName the name of the service
	 */
	public void deactiveAtBoot(String serviceName) {
		command.deactiveAtBoot(serviceName);
	}
	
	/**
	 * Install or update a package.
	 * <p>
	 * This method is capable to install three types of packages depending on the parameter it receives:
	 * <ul>
	 * 
	 * 	<li> Install a package (rpm, deb or executable in windows) which is located inside of scd4j directory.
	 * 		<br>
	 * 		In order to accomplish that, you need to configure the dependencies in build.gradle.
	 * 		<br>
	 * 		For example:
	 * 		<pre>
	 * 		dependencies {
	 * 			scd4j files('dependencies/my_dependency.deb')
	 * 		}
	 * 		</pre>
	 * 		<br>
	 * 		Then, in your hook file, you will need to call
	 * 		<br>
	 * 		<pre>
	 * 		install "my_dependency.deb"
	 * 		</pre>
	 * 
	 * 	<li> Install a package (rpm, deb or executable in windows) which is located inside of Artifactory or Nexus binary repository.
	 * 		<br>
	 * 		In order to accomplish that, you need to configure the dependencies in build.gradle.
	 * 		<br>
	 * 		For example:
	 * 		<pre>
	 * 		dependencies {
	 * 			scd4j 'my_group:my_pack_name:my_pack_version@rpm',
	 * 		}
	 * 		</pre>
 	 *		Then, in your hook file, you will need to call
	 * 		<pre>
	 * 		install 'my_group:my_pack_name:my_pack_version@rpm'
	 * 		</pre>
	 * 
	 * 	<li> Install an operational system package (not supported on Windows)
	 * 		<br>
	 * 		For example, in your hook file you will need to call:
	 * 		<pre>
	 * 		install "lxde=0.5.0-4ubuntu4"
	 * 		</pre>
	 * </ol>
	 * 
	 * @param pack the package name
	 */
	public void install(String pack) {
		try { 
			String path = resolve(pack);
			installLocalPack(path);
		} catch (DependencyNotFoundException e) {		
			installRemotePack(pack);
		}
	}

	void installLocalPack(String path) {
		command.installLocalPack(path);
	}
	
	void installRemotePack(String pack) {
		command.installRemotePack(pack);
	}

	/** 
	 * Uninstall an existing package.
	 * <p>
	 * This method is capable to uninstall two types of packages depending on the parameter it receives:
	 * <ul>
	 * 
	 * 	<li> Uninstall a package (rpm or deb) which was installed locally
	 * 		<br>
	 * 		In order to accomplish that, in your hook file, you will need to call
	 * 		<br>
	 * 		<pre>
	 * 		uninstall 'foo'
	 * 		</pre>
	 * 		Where foo is the name of your deb package
	 * 
	 * 	<li> Uninstall a package (rpm or deb) which was installed from the OS repositories
	 * 		<br>
	 * 		In order to accomplish that, in your hook file, you will need to call
	 * 		<pre>
	 * 		uninstall 'lxde'
	 * 		</pre>
	 * 		Where lxde is the name of your package in the OS repository
	 * 
	 * </ol>
	 * 
	 * <p>
	 * Currently only supports Linux operational system packages uninstall 
	 * 
	 * @param pack the package name without the version
	 */
	public void uninstall(String pack) {
		if(command.isInstalled(pack)){
			try {
				uninstallLocalPack(pack);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: It would not be better to uninstall using the same strategy than installation?
				// Asking this, because it is unistalling with uninstallLocalPack even though it has been installed using apt-get install
				uninstallRemotePack(pack);
			}
		}
	}

	void uninstallRemotePack(String pack) {
		command.uninstallRemotePack(pack);
	}

	void uninstallLocalPack(String pack) {
		command.uninstallLocalPack(pack);
	}
		
	/**
	 * DSL for {@link #unzip(String, String)}
	 * <p>
	 * Unzip a file which was put in the Artifactory, Nexus or which is located inside of scd4j directory.<br>
	 * In order to accomplish that, you need to configure the dependency in
	 * build.gradle.
	 * <p>
	 * 
	 * For example:
	 * <pre>
	 * dependencies {
	 * 	scd4j 'org.wildfly:wildfly:8.1.0.Final@zip',
	 *		files('dependencies/my_dependency.zip')
	 * 	}
	 * </pre>
	 * 
	 * Then, in your hook file, you will need to call
	 * <pre>
	 * 	unzip "org.wildfly:wildfly:8.1.0.Final@zip" to "/opt/example_wildfly_dir"
	 * 	unzip "my_dependency.zip" to "/opt/another_example_dir"
	 * </pre>
	 * 
	 * Note that the first line will get the dependency from a binary
	 * repository (Artifactory or Nexus) and the second line will get it from a scd4j 
	 * relative directory
	 * <p>
	 * 
	 * @param depName
	 *            the full dependency name
	 */	
	protected Destination unzip(String depName) {
		return new Destination() {			
			@Override
			public void to(String dir) {
				unzip(depName, dir);				
			}
		};
	}
	
	/**
	 * Unzip a file which was put in the Artifactory, Nexus or which is located inside of scd4j directory.<br>
	 * In order to accomplish that, you need to configure the dependency in
	 * build.gradle.
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 * dependencies {
	 * 	scd4j 'org.wildfly:wildfly:8.1.0.Final@zip',
	 *		files('dependencies/my_dependency.zip')
	 * 	}
	 * </pre>
	 * 
	 * <br>
	 * Then, in your hook file, you will need to call
	 * <pre>
	 * 	unzip("org.wildfly:wildfly:8.1.0.Final@zip", "/opt/example_wildfly_dir")
	 * 	unzip("my_dependency.zip", "/opt/another_example_dir")
	 * </pre>
	 * 
	 * Note that the first line will get the dependency from a binary
	 * repository (Artifactory or Nexus) and the second line will get it from a scd4j 
	 * relative directory
	 * <p>
	 * 
	 * @param depName
	 *            the full dependency name
	 * @param doDir
	 * 				destination directory
	 */
	protected void unzip(String depName, String toDir) {
		String from = resolve(depName);
		command.unzip(from, toDir);				
	}
	
	/**
	 * Downloads (uses cache to avoid downloading the same dependency many
	 * times) a dependency which was put in the Artifactory or Nexus. <br>
	 * In order to accomplish that, you need to configure the dependency in
	 * build.gradle.
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 * 	dependencies {
	 * 		scd4j 'org.wildfly:wildfly:8.1.0.Final@zip',
	 * 			'com.xyz:foo:0.1@deb',
	 * 			'com.xyz:oof:1.0@rpm'
	 * 	}
	 * </pre>
	 * 
	 * Then, in you hook file, you will need to call
	 * 
	 * <pre>
	 * 	def path = resolve "com.xyz:foo:0.1@deb"
	 * 	OR
	 * 	def path = resolve "com.xyz:oof:1.0@rpm"
	 * 	OR
	 * 	def path = resolve "org.wildfly:wildfly:8.1.0.Final@zip"
	 * </pre>
	 * <p>
	 * 
	 * If you do not provide the file extention (@deb, @rpm, @zip, @ear, @war or
	 * @jar) this method tries to infer it according to the following rule:
	 * <ol>
	 * <li>Tries to resolve the provided dependency name
	 * <li>If it does not work and you are running on linux, tries to resolve
	 * the distribution dependency (i.e. @deb for ubuntu and @rpm for CentOs)
	 * <li>If it does not work tries to resolve the dependency in the following order: @zip, 
	 * @ear, @war or @jar
	 * </ol>
	 * 
	 * @param depName
	 *            the full dependency name
	 * @return the full path where is located the file
	 */		
    protected String resolve(final String depName) {
    	Path file = conf.getDependency(depName);
    	
    	if(file==null && isLinux()){
			String packExtension = ((LinuxCommand)command).getPackExtension();
			file = conf.getDependency(depName + "@" + packExtension);	   		
    	}
    	
    	if(file==null) {
	    	List<String> extentions = Arrays.asList("zip", "ear", "war", "jar");
	    	String ext = extentions.stream()
	    		.filter(e -> conf.getDependency(depName + "@" + e)!=null )	    		
	    		.findFirst().orElse(null);
	    	file = conf.getDependency(depName + "@" + ext);
    	}
    	    	
    	if(file==null)
    		throw new DependencyNotFoundException("Could not resolve dependency: " + depName);

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
    	command.serviceStart(name);
	}
    
    /** 
     * Stops an OS service
	 * <p>
	 * Note: Currently Linux only
	 *  
     * @param name the service name
     */
	public void stop(String name) {
		command.serviceStop(name);
	}

    /** 
     * restart an OS service
	 * <p>
	 * Note: Currently Linux only
	 *  
     * @param name the service name
     */
	public void restart(String name) {
		command.serviceRestart(name);
	}
	
    /** 
     * Checks the OS service status
	 * <p>
	 * Note: Currently Linux only
	 *  
     * @param name the service name
     * @return status information of the service, or the error message if it does not exists
     */
	public String status(String name) {
		try {
			return command.serviceStatus(name);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	// ------ methods used by the framework only ----
	
    /**
     * Not a public API.<br> 
     * Used only by {@link HookEvaluator} to set variables 
     */
	final void setConf(Configuration conf) {
		this.conf = conf;
		this.envs = conf.getEnv();
		this.props = conf.getProps();
		this.temporaryProps = conf.getTempProps();
	}
	
	
	/**
     * Not a public API.<br> 
	 * Used only by {@link HookEvaluator} to call the pre condition closure 
	 */
	final Action _pre(){
		if (pre != null) {
			Action action = pre.call();
			if (action == null) { 
				// pre{} does not return anything				
				return CONTINUE_INSTALLATION;
			} 
			
			validateAction(action);
			return action;	
		}
		
		// pre{} was not defined
		return CONTINUE_INSTALLATION;
	}

	/**
     * Not a public API.<br>   
	 */
	protected abstract void validateAction(Action action);
	
	/**
     * Not a public API.<br> 
	 * Used only by {@link HookEvaluator} to call the post condition closure 
	 */
	final void _post() {
		if(post!=null){
			post.call(); 
		}
	}	
	
	/**
     * Not a public API.<br> 
	 * Used only by {@link HookEvaluator} to cleanup transient properties 
	 */
	final void _finish() {
		temporaryProps.forEach((k, v) -> props.remove(k) );
		temporaryProps.clear();
	}
	
	// ---- interfaces for dsl -----
	
	protected interface Destination {		
		void to(String to);
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
