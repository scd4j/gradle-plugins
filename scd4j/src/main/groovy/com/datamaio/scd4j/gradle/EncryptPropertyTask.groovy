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
import org.gradle.api.tasks.TaskAction

import com.datamaio.scd4j.ui.EncryptPropertyDialog;
import com.datamaio.scd4j.ui.dto.EncryptPropertyDTO;
import com.datamaio.scd4j.util.Encryptor

/**
 *
 * @author Fernando Rubbo
 */
class EncryptPropertyTask extends DefaultTask {
	
	@TaskAction
    def action() {		
		println "\nHelper tool to encrypt a property value"
		
		def name =  null
		def value = null
		def newPass = null
		def cNewPass = null
		
		Console console = System.console()
		if (console) {
			 name = console.readLine('\nProperty Name: ')
			 value = new String(console.readPassword('Propert Value: '))
			 newPass = new String(console.readPassword('New Password: '))
			 cNewPass = new String(console.readPassword('Confirm New Password: '))
		} else {
			EncryptPropertyDialog dialog = new EncryptPropertyDialog();
			EncryptPropertyDTO dto = dialog.showDialog();
			
			name = dto.getProperty();
			value = dto.getPropertyValue();
			newPass = dto.getNewPassword();
			cNewPass = dto.getConfirmNewPassword();
		}
		
		if (newPass==cNewPass) {
			def encrypted = Encryptor.get(newPass).encryptProp(value)
			println "\nPut the property bellow in your configuration file"
			println "$name=$encrypted"
		} else {
			println "==============================="
			println "=== Password does not match ==="
			println "==============================="
		}
    }
}
