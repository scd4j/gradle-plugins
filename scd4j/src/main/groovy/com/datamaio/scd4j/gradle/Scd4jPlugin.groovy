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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.wrapper.Wrapper

import com.datamaio.scd4j.gradle.util.URLDownloader;

/**
 *
 * @author Fernando Rubbo
 */
class Scd4jPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
    	project.defaultTasks 'scd4j'
    	project.configurations {
    		scd4j
    	}
		
		// -- create scd4j extensions
        project.extensions.create("scd4j", Scd4jExtension)
        project.scd4j.extensions.create("install", InstallNestedExtension)		
        project.scd4j.install.extensions.create("env", EnvNestedExtension)
		project.scd4j.extensions.create("settings", SettingsNestedExtention)
		project.scd4j.settings.extensions.create("template", TemplateNestedExtention)		
		project.scd4j.settings.extensions.create("linux", LinuxNestedExtention)
		project.scd4j.settings.extensions.create("windows", WindowsNestedExtention)
		
		project.extensions.add('url', { url ->
			def file = new URLDownloader().download(url, project);
			return project.files(file)
		});

		// -- configure tools tasks	
		project.task('newproject', type:NewProjectTask){
			group = "scd4j Tools"
			description = "Helper tool to create an empty project"
		}
		project.task('encrypt', type:EncryptPropertyTask){
			group = "scd4j Tools"
			description = "Helper to encrypt a property"
		}
		project.task('decrypt', type:DecryptPropertyTask){
			group = "scd4j Tools"
			description = "Helper to dencrypt a property"
		}
		project.task('validate', type:ValidateTask){
			group = "scd4j Tools"
			description = "Helper to perform a basic sanity check in the configuration (includes password check)"
		}
		project.task('changepassword', type:ChangePasswordTask){
			group = "scd4j Tools"
			description = "Helper to change password for encrypted properties, all at once"
		}			

		
		// -- configure the wrapper to execute automatically and create directories
		project.task('wrapper', type: Wrapper) {
			description = "Generate gradle wrapper"
			gradleVersion = '2.2'
		}		
		project.tasks["wrapper"].execute()
		newDir (project, 'modules')
		newDir (project, 'config')
		
		
		// -- configure basic tasks 
		project.apply (plugin: 'base')
		project.task('scd4j', type:Scd4jTask){
			group = "scd4j"
			description = "Automatically isntall and configure the environment"
		}
		project.task('pack', type:Zip) {
			group = "scd4j"
			description = "Generates a zip to be installed in another environment. Triggered in the default gradle artifacts generation."
			from '.'
			exclude 'build', 'log', 'backup', 'target', ".gradle"
		}
		project.configurations {
			archives
		}
		project.artifacts {
			archives project.pack
		}
		
		// -- override the default behaviour to delete more folders
		project.clean {
			description = "Deletes the following folders: build, backup, log and target"
			delete "backup","log", "target"
		}
    }
	
	boolean newDir(project, dir){
		File f = new File("${project.projectDir}/$dir");
		if(!f.exists()) {
			println "Creating directory ${f.absolutePath}"
			f.mkdirs();
			return true;
		}
		return false;
	}
}
