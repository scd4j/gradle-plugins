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
package com.datamaio.scd4j.gradle;

/**
 *
 * @author Fernando Rubbo
 */
class Scd4jExtension {
}

/**
 *
 * @author Fernando Rubbo
 */
class EnvNestedExtension{
	String[] production = []
	String[] staging  = []
	String[] testing = []
	void production(String... ips) {
		production = ips
	}
	void staging(String... ips) {
		staging = ips
	}
	void testing(String... ips) {
		testing = ips
	}
}

/**
 *
 * @author Fernando Rubbo
 */
class InstallNestedExtension{
	String config = ""
	String[] modules = []
	void config(String conf){
		config = conf
	}
	void modules(String... mods){
		modules = mods
	}
}

/**
 *
 * @author Fernando Rubbo
 */
class SettingsNestedExtention{
}

/**
 *
 * @author Fernando Rubbo
 */
class TemplateNestedExtention{
	String engine = "groovy"
	void engine(String eng){
		engine = eng
	}
}

/**
 *
 * @author Fernando Rubbo
 */
class LinuxNestedExtention{
//	boolean usesudo = false
}

/**
 *
 * @author Fernando Rubbo
 */
class WindowsNestedExtention{
}