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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.datamaio.scd4j.util.Encryptor

/**
 *
 * @author Fernando Rubbo
 */
class ChangePasswordTask extends DefaultTask {
	
	@TaskAction
    def action() {
		def config = Input.config(project)		
		println "\nHelper tool to change the password of file : $config"
		
		if( Input.validateConfig(config) ) {
			if( config.text.contains(Encryptor.PREFIX) ) {
				Console console = System.console()
				if (console) {				
					def currentPass = new String(console.readPassword('\nCurrent Password: '))
					def newPass = new String(console.readPassword('New Password: '))
					def cNewPass = new String(console.readPassword('Confirm New Password: '))
					if (newPass==cNewPass) {
						changePassword(currentPass, newPass, config);
					} else {
						println "==============================="
						println "=== Password does not match ==="
						println "==============================="
					}
				} else {
					println "ERROR: Cannot get console."
				}
			} else {
				println "Provided config file does not have any encrypted property"
				println "============================="
				println "=== Nothing has been done ==="
				println "============================="
			}
		} else {
			println "============================="
			println "=== Nothing has been done ==="
			println "============================="
		}
    }
	
	void changePassword(currentPass, newPass, config) {
		Encryptor decryptor = Encryptor.get(currentPass);
		Encryptor encryptor = Encryptor.get(newPass);
		
		try {
			boolean success = true;
			int count = 0;
			PropertiesConfiguration props = new PropertiesConfiguration(config);
			Iterator<String> keys = props.getKeys();
			while (keys.hasNext()) {
				String key = keys.next();
				String encryptedValue = (String) props.getString(key);
				if(encryptedValue!=null && encryptedValue.startsWith(Encryptor.PREFIX)) {
					count++;
					try {
						String decrypt = decryptor.decrypt(encryptedValue);
						String newEncryptedValue = encryptor.encryptProp(decrypt);
						props.setProperty(key, newEncryptedValue);
						println "$count. Converting property $key => from $encryptedValue to $newEncryptedValue"
					} catch (Exception e) {
						println "\t***********************************************************************************************************"
						println "\t********************** COULD NOT DECRYPT PROPERTY: " + key + " *****************"
						println "\t***********************************************************************************************************"
						success = false
					}
				}
			}
			println "\n\nNUMBER OF ENCRYPTED PROPERTIES FOUND: $count";
			if(success) {
				props.save();
				println "\n\nConfig file $config successfully save!";
			} else {				
				println "\n\nConfig file $config was not updated because there are errors. Fix it and try again!";
				println "============================="
				println "=== Nothing has been done ==="
				println "============================="
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
