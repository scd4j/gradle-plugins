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

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.TaskAction

import com.datamaio.scd4j.conf.ConfEnvironments
import com.datamaio.scd4j.EnvConfigurator;

/**
 * Task used to start SCD4J
 * <p>
 * In order to avoid prompting for confirmation use <code>./gradlew -PassumeYes=true</code>. In other words, 
 * this config automatic answer "yes" all prompts and run non-interactively.
 * <p> 
 *
 * @author Fernando Rubbo
 */
class Scd4jTask extends DefaultTask {
	
	@TaskAction
    def action() {		
    	def env = project.scd4j.install.env		
		def config = Input.config(project);
		def modules = Input.modules(project)
						
        println "==================== Running scd4j =============================="
		println "====== Version Info ==================="
		println "SCD4J Version : " + getScd4jVersion(project)		
		println "Pack Name     : ${project.archivesBaseName} "
		println "Pack Version  : ${project.version} "
		println "====== Environment Configuration ======"
        println "IP PROD LIST  : ${env.prod}" 
        println "IP HOM  LIST  : ${env.hom}" 
        println "IP TEST LIST  : ${env.test}" 
		println "IP DES  LIST  : [ANY OTHER]"
		println "====== Instalation Configuration ======"
        println "CONFIG FILE   : $config" 
        println "MODULE DIRS   : $modules" 
		println "=================================================================="
		
		if( Input.validate(modules, config) ) {
			def console = System.console()			
			if (assumeYes(project)) {
				run(env, modules, config)
			} else if(console) {
				def ok = console.readLine('\nReview the above config. Type "yes/y" to procceed and "no/n" to abort: ')
				if("yes".equalsIgnoreCase(ok) || "y".equalsIgnoreCase(ok) ) {
					run(env, modules, config)
				} else {
					println "============================"
					println "=== Instalation aborted! ==="
					println "============================"
				}
			} else {
				// Running inside of eclipse, for example..
				println "DEV ONLY: Cannot get console - Will keep processing, but will not accept cryptography in any configuration property"
				def environments = new ConfEnvironments(env.prod, env.hom, env.test)
				def dependencies = mapDependencies2Path();
				for(module in modules) {
					new EnvConfigurator(config.toPath(), module.toPath(), environments, dependencies).exec();
				}
			}
		} else {
			println "============================"
			println "=== Instalation aborted! ==="
			println "============================"
		}		
    }

	def run(env, modules, config) {
		def environments = new ConfEnvironments(env.prod, env.hom, env.test)
		def dependencies = mapDependencies2Path();
		for(module in modules) {						
			new EnvConfigurator(config.toPath(), module.toPath(), environments, dependencies).exec();
		}
	}

	
	def assumeYes(project) {
		return project.hasProperty("assumeYes") ? "true".equals(project.assumeYes) : false
	}
	
	def mapDependencies2Path(){
		def map = [:]
		def set = []
		project.configurations.scd4j.dependencies?.each {Dependency d ->
			if(d.group!=null && d.version!=null) {
				if(d.version.contains("+")) {
					throw new InvalidUserDataException("In 'scd4j' it is not allowed to use '+' modifier!")
				}
				
				def key = "$d.group:$d.name:$d.version"		
				def value = project.configurations.scd4j.files?.find { File f ->
					def name = f.toString();
					return d.group.split("\\.").find{s -> name.contains(s)}!=null && name.contains(d.name) && name.contains(d.version)
				}
				if(value==null){
					throw new InvalidUserDataException("Could not resolve 'scd4j' dependency: $key.")
				}
				map.putAt(key, value.toPath())
				set.add(value)
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
