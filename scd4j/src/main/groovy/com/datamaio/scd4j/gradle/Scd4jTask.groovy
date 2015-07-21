/*
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
package com.datamaio.scd4j.gradle

import javax.swing.JOptionPane

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.datamaio.scd4j.EnvConfigurator
import com.datamaio.scd4j.cmd.Command;
import com.datamaio.scd4j.conf.Configuration
import com.datamaio.scd4j.conf.Env
import com.datamaio.scd4j.conf.Install
import com.datamaio.scd4j.conf.Settings
import com.datamaio.scd4j.conf.Template
import com.datamaio.scd4j.ui.AlertMessageDialog;

/**
 * Task used to start SCD4J
 * <p>
 * In order to avoid prompting for confirmation use <code>./gradlew -PassumeYes=true</code>. In other words, 
 * this config automatic answer "yes" all prompts and run non-interactively.
 * <p> 
 *
 * @author Fernando Rubbo
 * @author Mateus M. da Costa
 */
class Scd4jTask extends DefaultTask {
	
	@TaskAction
    def action() {		
		def settings = project.scd4j.settings;
    	def env = project.scd4j.install.env		
		def config = Input.config(project);
		def modules = Input.modules(project)
						
        println "==================== Running scd4j =============================="
		println "====== Version Info ==================="
		println "SCD4J Version : " + getScd4jVersion(project)		
		println "Pack Name     : ${project.archivesBaseName} "
		println "Pack Version  : ${project.version} "
		println "====== Environment Configuration ======"
        println "IP PRODUCTION LIST 	: ${env.production}" 
        println "IP STAGIN  LIST  	: ${env.staging}" 
        println "IP TESTING LIST  	: ${env.testing}" 
		println "IP DESENV  LIST  	: [ANY OTHER]"
		println "====== Instalation Configuration ======"
        println "CONFIG FILE   : $config" 
        println "MODULE DIRS   : $modules" 
		println "=================================================================="
		
		if( Input.validate(modules, config) ) {
			def console = System.console()
			
			if (assumeYes(project)) {
				run(settings, env, modules, config)
			} else if(console) {
				def ok = console.readLine('\nReview the above config. Type "yes/y" to procceed or anything else to abort: ')
				if("yes".equalsIgnoreCase(ok) || "y".equalsIgnoreCase(ok) ) {
					run(settings, env, modules, config)
				} else {
					println "============================"
					println "=== Instalation aborted! ==="
					println "============================"
				}
			} else {
					//If console returns null it will open a dialog for requesting the confirmation
					def envObj = new Env(env.production, env.staging, env.testing)
					AlertMessageDialog alertMessageDialog = 
								new AlertMessageDialog
										(getScd4jVersion(project), project.archivesBaseName, 
											project.version, envObj, config, modules);
			
					def option = alertMessageDialog.showConfirmDialog();
					if(option == JOptionPane.YES_OPTION){
						run(settings, env, modules, config)
					} else {
						println "============================"
						println "=== Instalation aborted! ==="
						println "============================"
					}
			}
		} else {
			println "============================"
			println "=== Instalation aborted! ==="
			println "============================"
		}		
    }

	def run(sett, envs, modules, config) {
		def env = new Env(envs.production, envs.staging, envs.testing)
		def dependencies = mapDependencies2Path();
		for(module in modules) {	
			Install install = new Install(module.toPath(), config.toPath(), env);
			Settings settings = new Settings();
			settings.setTemplate(new Template(sett.template.engine));
			Configuration conf = new Configuration(install, settings, dependencies);
			new EnvConfigurator(conf).execute();
		}
	}

	
	def assumeYes(project) {
		return project.hasProperty("assumeYes") ? "true".equals(project.assumeYes) : false
	}
	
	def mapDependencies2Path(){
		def map = [:]
		def set = []
		project.configurations.scd4j.resolvedConfiguration.firstLevelModuleDependencies?.each {d ->
			d.moduleArtifacts.each { a ->
				def key = "${d.moduleGroup}:${d.moduleName}:${d.moduleVersion}@${a.extension}"
				def file = a.file			
				map.putAt(key, file.toPath())
				set.add(file)
			}
		}
		
		project.configurations.scd4j.files?.each { File f ->
			if(!set.contains(f)){
				map.putAt(f.name, f.toPath())
			}
		}
	
		return map
	}
	
	def getScd4jVersion(project) {
		def plugin = project.buildscript.configurations.classpath.resolvedConfiguration.firstLevelModuleDependencies?.find({ it.moduleName.equals("scd4j") })
		if(plugin!=null) {
			return plugin.moduleVersion
		}
	
		return "N/A"
	}
}
