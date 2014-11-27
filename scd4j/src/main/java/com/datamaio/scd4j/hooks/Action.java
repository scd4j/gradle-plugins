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
package com.datamaio.scd4j.hooks;

/**
 * Actions taken during pre Module and File installations
 * 
 * @author Fernando Rubbo
 */
public enum Action {
	/**
	 * Keep executing the installation
	 * <br>
	 * Can be used in both Module and File Hooks
	 */
	CONTINUE_INSTALLATION,
	/**
	 * Stop the installation where it is. 
	 * <br>
	 * Can be used in both Module and File Hooks. 
	 * <br>
	 * INPORTANT: although it can be used in File Hooks we strongly recommend
	 * not to do it because this option abruptly stops the installation. Prefer
	 * use this <code>CANCEL_INSTALLATION</code> in Module Hooks
	 */
	CANCEL_INSTALLATION, 
	/**
	 * Skip current file, but Keep executing the installation
	 * <br>
	 * Can be used only in File Hooks 
	 */
	SKIP_FILE_INSTALLATION;
	
	public boolean isValidForModuleHook(){
		return this.equals(CONTINUE_INSTALLATION) || this.equals(CANCEL_INSTALLATION);
	}
	
	public boolean isValidForFileHook(){
		return this.equals(CONTINUE_INSTALLATION) || this.equals(CANCEL_INSTALLATION) || this.equals(SKIP_FILE_INSTALLATION);
	}
}
