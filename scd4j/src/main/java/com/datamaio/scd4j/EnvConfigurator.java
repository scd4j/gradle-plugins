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
import com.datamaio.scd4j.hooks.file.FileHookEvaluator;
import com.datamaio.scd4j.hooks.module.ModuleHookEvaluator;
import com.datamaio.scd4j.util.BackupHelper;
import com.datamaio.scd4j.util.LogHelper;
import com.datamaio.scd4j.util.PathHelper;
import com.datamaio.scd4j.util.io.CopyVisitor;
import com.datamaio.scd4j.util.io.DeleteVisitor;
import com.datamaio.scd4j.util.io.FileUtils;

/**
 * OBS: Esta classe não é thread safe devido aos FileHookEmbeddedGroovy. 
 * Se precisar modificar, basta sempre instanciar eles na hora de usar. Ai a classe vira thread safe
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
				if( super.mustDelete(source) ) {
					boolean exists = exists(target);
					boolean pre = hook.pre();
					if ( !exists ) {
						LOGGER.info("\tFile " + target + " does not exists.");
						LOGGER.info("\tNothing to do!");
					}
					return exists && pre;
				}
				return false; 
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
				return !matcher.matches(source.getFileName()) && hook.pre();
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
