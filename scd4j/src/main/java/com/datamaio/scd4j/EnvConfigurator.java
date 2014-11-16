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
package com.datamaio.scd4j;

import static com.datamaio.scd4j.conf.Configuration.DELETE_SUFFIX;
import static com.datamaio.scd4j.conf.Configuration.HOOK_SUFFIX;
import static com.datamaio.scd4j.conf.Configuration.TEMPLATE_SUFFIX;
import static java.nio.file.Files.exists;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import groovy.text.SimpleTemplateEngine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.datamaio.scd4j.conf.ConfEnvironments;
import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.hooks.Hook;
import com.datamaio.scd4j.hooks.file.FileHookEvaluator;
import com.datamaio.scd4j.hooks.module.ModuleHookEvaluator;
import com.datamaio.scd4j.util.BackupHelper;
import com.datamaio.scd4j.util.LogHelper;
import com.datamaio.scd4j.util.PathHelper;
import com.datamaio.scd4j.util.io.CopyVisitor;
import com.datamaio.scd4j.util.io.DeleteVisitor;
import com.datamaio.scd4j.util.io.FileUtils;

/**
 * This class é the heart of scd4j tool. <br>
 * It is responsible to start the process, to delete target files, copy and
 * merge templates. Beoynd that this class is responsible to initiate and
 * execute all existing hooks.
 * <p>
 * Note. This class is not thread safe due to FileHookEmbeddedGroovy.
 * 
 * @author Fernando Rubbo
 */
public class EnvConfigurator {
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private Configuration conf;
	private final PathHelper pathHelper;
	private final BackupHelper backupHelper;

	public EnvConfigurator(Path properties, Path module2Install) {
		this(properties, module2Install, new ConfEnvironments(), new HashMap<>());
	}
	
	EnvConfigurator(Map<String, String> instalationProperties, Path module2Install) {
		this(new Configuration(Paths.get(new File(".").getAbsolutePath(), "config"), instalationProperties, module2Install));
	}
	
	public EnvConfigurator(Path properties, Path module2Install, ConfEnvironments environments, Map<String, Path> dependencies) {
		this(new Configuration(properties, module2Install, environments, dependencies));
	}
	
	public EnvConfigurator(Configuration conf) {
		this.conf = conf;
		this.pathHelper = new PathHelper(conf);
		this.backupHelper = new BackupHelper(conf);
		new LogHelper(conf).startup();
	}

	/**
	 * This method starts the installation and configuration process.<br>
	 * 
	 * Whenever a file <code>Module.hook</code> exists in the root of the module
	 * directory, it will be used to perform <code>pre()</code> and
	 * <code>post()<code> semantics of the module
	 * Installation.
	 */
	public void exec() {
		conf.printProperties();
		Path module = conf.getModuleDir();
		try {			
			final ModuleHookEvaluator hook = new ModuleHookEvaluator(conf);
			try{
				if (hook.pre()) {
					deleteFiles();
					copyFiles();
					hook.post();
				} else {
					LOGGER.warning("Modulo " + module + " nao foi instalado neste ambiente pois o Module.hook retornou false");
				}
			} finally {
				hook.finish();
			}
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Erro inesperado.", e);
			throw new RuntimeException("Erro inesperado. Causa: " + e.getMessage(), e);
		}
	}

	/**
	 * Deletes all files configured inside of the sub folders of a module
	 * <p>
	 * 
	 * Suppose you would like to delete a file found in<br>
	 * &nbsp;&nbsp;<code>/opt/my_app/my_file.txt</code><br>
	 * Then, in scd4j you should create a file<br>
	 * &nbsp;&nbsp;
	 * <code>< scd4j_home >/module/< my_module_name >/opt/my_app/my_file.txt.del</code>
	 * <p>
	 * 
	 * Note that you have to use exactly the same path of the original file
	 * appending <code>.del</code> at the end. This tells to scd4j to deleted
	 * such file.<br>
	 * Although the file content has no importance during delete operations, it
	 * is strongly recommended to keep the content inside the <code>.del</code>
	 * file in order keep the original information safe.
	 * <p>
	 * 
	 * Another important feature implemented in this method is the ability to
	 * execute a hook before and after the deletion.
	 * <br>
	 * In oder to accomplish that you need to implement another file called 
	 * <br>
	 * &nbsp;&nbsp; <code>< scd4j_home >/module/< my_module_name >/opt/my_app/my_file.txt.del.hook</code>
	 * <br>
	 * Note that it must have exactly the same name of the <code>.del</code>
	 * file appending <code>.hook</code> at the end. This tells to scd4j that
	 * exists a semantis to be executed before and after the deletion. 
	 * 
	 * <p>
	 * For more information about how to write a <code>.hook</code> file see
	 * {@link Hook#pre()} and {@link Hook#post()}
	 */
	protected void deleteFiles() {		
		Path module = conf.getModuleDir();
		
		FileUtils.deleteDir(module, new DeleteVisitor("*" + DELETE_SUFFIX){
			private FileHookEvaluator hook;
			
			@Override /** Verificar se o arquivo existe antes de tentar deletar */
			protected boolean mustDelete(Path source) {
				if(source.toString().endsWith(HOOK_SUFFIX)){
					return false;
				}
				
				final Path target = pathHelper.getTargetWithoutSuffix(source, DELETE_SUFFIX);
				hook = new FileHookEvaluator(source, target, conf);

				boolean mustDelete = super.mustDelete(source);
				boolean fileExists = exists(target);
				boolean pre = true;
				try {						
					return mustDelete && fileExists && (pre = hook.pre());
				} finally {
					if(!pre) {
						hook.finish();
					}
				}
			}
			
			@Override /** Deleta o target e não source */ 
			protected void delete(Path source) throws IOException {
				try {
					Path target = pathHelper.getTargetWithoutSuffix(source, DELETE_SUFFIX);
					backupHelper.backupFileOrDir(target);
					FileUtils.delete(target);
					LOGGER.info(" :DELETED");
					LOGGER.info("\t" + target);
					hook.post();
				} finally {
					hook.finish();
				}
			}
		});
	}
	
	/**
	 * Copies all files configured inside of the sub folders of a module
	 * 
	 * <p>
	 * Suppose you would like to copy a new file to<br>
	 * &nbsp;&nbsp;<code>/opt/my_app/my_file.txt</code><br>
	 * Then, in scd4j you should create a file<br>
	 * &nbsp;&nbsp;
	 * <code>< scd4j_home >/module/< my_module_name >/opt/my_app/my_file.txt</code>
	 * <br>
	 * OR, if you would like fill out some gaps inside of the file before coping
	 * it to the target place you should create a file like this<br>
	 * &nbsp;&nbsp;
	 * <code>< scd4j_home >/module/< my_module_name >/opt/my_app/my_file.txt.tmpl</code>
	 * <p>
	 * 
	 * Note that you have to use exactly the same path of the original target
	 * file in oder to copy it to the right place. If you intend to fill out
	 * dinamically a database url, for example, you must append
	 * <code>.tmpl</code> at the end. This tells to scd4j to, before coping,
	 * resolve the template of the file. Which can be written using <a
	 * href="http://groovy.codehaus.org/Groovy+Templates">Groovy Templates
	 * sintax</a>.
	 * 
	 * <p>
	 * Another important feature implemented in this method is the ability to
	 * execute a hook before and after the coping.<br>
	 * In oder to accomplish that you need to implement another file called <br>
	 * &nbsp;&nbsp;
	 * <code>< scd4j_home >/module/< my_module_name >/opt/my_app/my_file.txt.hook</code>
	 * <br>
	 * OR <br>
	 * &nbsp;&nbsp;
	 * <code>< scd4j_home >/module/< my_module_name >/opt/my_app/my_file.txt.tmpl.hook</code>
	 * <br>
	 * depending on if you would like to use a template or not.
	 * 
	 * <p>
	 * For more information about how to write a <code>.hook</code> file see
	 * {@link Hook#pre()} and {@link Hook#post()}
	 */
	protected void copyFiles() {
		Path module = conf.getModuleDir();
		final Map<String, String> properties = conf.getProperties();
		
		final SimpleTemplateEngine engine = new SimpleTemplateEngine();
		final Path target = pathHelper.getTarget(module);
		
		FileUtils.copy(new CopyVisitor(module, target, "*" + DELETE_SUFFIX){
			private FileHookEvaluator hook;
			
			@Override /** Não considera os .del */
			protected boolean mustCopy(Path source) {
				if(source.toString().endsWith(HOOK_SUFFIX)){
					return false;
				}
				
				final Path target = pathHelper.getTargetWithoutSuffix(source, TEMPLATE_SUFFIX);
				hook = new FileHookEvaluator(source, target, conf);
				
				boolean mustCopy = !matcher.matches(source.getFileName());
				boolean pre = true;
				try {
					return mustCopy && (pre = hook.pre());
				} finally {
					if(!pre){
						hook.finish();
					}
				}				
			}
			
			@Override /** Copia OU faz o merge do template */
			protected void copy(Path source, final Path target) throws IOException {
				try {
					if(source.toString().endsWith(TEMPLATE_SUFFIX)) {
						File resolvedTargetFile = new File(target.toString().replace(TEMPLATE_SUFFIX, ""));
						backupHelper.backupFile(resolvedTargetFile.toPath());
					    try (Writer out = new BufferedWriter(new FileWriter(resolvedTargetFile))) {
							engine.createTemplate(source.toFile())
								.make(properties)
								.writeTo(out);
						} catch (Exception e) {
							throw new IOException(e);
						}
					    LOGGER.info(" :MERGED");
					    LOGGER.info("\t" + source + " -> " + resolvedTargetFile);
					    hook.post();
					} else {
						backupHelper.backupFile(target);
						Files.copy(source, target, REPLACE_EXISTING);
						LOGGER.info(" :COPIED");
						LOGGER.info("\t" + source + " -> " + target);
						hook.post();
					}
				} finally {
					hook.finish();
				}
			}
		});
	}
}
