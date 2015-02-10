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
package com.datamaio.scd4j.gradle

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 *
 * @author Fernando Rubbo
 */
class NewProjectTask extends DefaultTask {

	@TaskAction
	def action() {
		def config = Input.config(project)
		def modules = Input.modules(project)
		println """\nHelper tool to create an empty project:
	- Config file: $config
	- Module inputs: $modules"""

		if(!Input.validateConfig(config)) {
			println "Creating Config $config"
			config.createNewFile();
			config.text = """######## Required Property ###########
# Must contain one of the following: #
#    - development                   #
#    - testing                       #
#    - staging                       #
#    - production                    #
###################################### 
env=development
"""
		}
		
		for( module in modules ){
			if(!Input.validateModule(module)) {
				println "Creating Module $module"
				module.mkdirs();
				new File(module, "Module.hook").text = """pre{
	// put here your pre module installation logic
	if( isLinux() ) {
		return CONTINUE
	}
	return ABORT
}

post{
	// put here your post module installation logic
}"""
			}
		}
	}

}
