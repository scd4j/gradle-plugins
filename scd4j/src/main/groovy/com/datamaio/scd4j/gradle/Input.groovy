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

import org.apache.commons.configuration.PropertiesConfiguration

import static com.datamaio.scd4j.conf.Configuration.MODULES_FOLDER;
import static com.datamaio.scd4j.conf.Configuration.CONFIG_FOLDER;
import com.datamaio.scd4j.util.Encryptor


/**
 *
 * @author Fernando Rubbo
 */
class Input {
	static File config(project){
		def install = project.scd4j.install
		def config = install.config;
		if(config.startsWith("config" + File.separator)){
			config = config.replaceFirst("config" + File.separator, "")
		}
		return project.file(CONFIG_FOLDER + "/" + config)
	}
	
	static File[] modules(project){
		def result = []		
		
 		def modules = project.scd4j.install.modules
		for(m in  modules){
			result.add(project.file(MODULES_FOLDER + "/" + m))
		}
		
		return result;
	}
	
	static boolean validate(modules, config) {
		return validateModules(modules) && validateConfig(config);
	}
	
	static boolean validateModules(modules) {
		def ok = true;		
		for( module in modules ){
			ok &= validateModule(module);
		}
		return ok;
	}
	
	static boolean validateModule(module) {
		if(!module.exists() || !module.isDirectory()) {
			println "***************************************************************************************"
			println "*** Module '$module' does not exists or it is not a directory ***"
			println "***************************************************************************************"
			return false
		}
		return true
	}
	
	static boolean validateConfig(config) {
		if(!config.exists() || config.isDirectory()) {
			println "***************************************************************************************"
			println "*** Configuration '$config' does not exists or it is a directory ***"
			println "***************************************************************************************"
			return false
		}
		return true;
	}
	
	static boolean validateConfigEncryption(config) {
		boolean valid = true;
		
		if( config.text.contains(Encryptor.PREFIX) ) {
			Console console = System.console()
			if (console) {
				def pass = new String(console.readPassword('\nPassword: '))
				Encryptor enc = Encryptor.get(pass);
				try {
					int count = 0;
					PropertiesConfiguration props = new PropertiesConfiguration(config);
					Iterator<String> keys = props.getKeys();
					while (keys.hasNext()) {
						String key = keys.next();
						String value = (String) props.getString(key);
						if(value!=null && value.startsWith(Encryptor.PREFIX)) {
							count++;
							println count + ". Validating encrypted property: " + key
							try {
								enc.decrypt(value);
								println "\t..OK"
							} catch (Exception e) {
								println "\t*******************************************************************************************************************"
								println "\t********** ENCRYPTION DOES NOT MATCH THE PASSWORD FOR PROPERTY: " + key + " ***********"
								println "\t*******************************************************************************************************************"
								valid = false;
							}
						}
					}
					println "\n\nNUMBER OF ENCRYPTED PROPERTIES FOUND: $count"					
				} catch (Exception e) {
					throw new RuntimeException(e);
				}				
			} else {
				println "ERROR: Cannot get console."
			}
		}
		
		return valid;
	}
}
