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
package com.datamaio.scd4j.hooks.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.datamaio.scd4j.hooks.Hook;
import com.datamaio.scd4j.hooks.HookEvaluator;

/**
 * This class is the super class of File Hook Scripts<br>
 * This provides some helpful methods and functions
 * 
 * @author Fernando Rubbo
 */
public abstract class FileHook extends Hook {
	protected String src;
	protected String target;
	
	/** 
	 * Changes the Posix File Permissions for the target file. Very useful in the post() hook<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param mode is the posix definition, ex: "777"
	 */
	protected void chmod(String mode) {
		chmod(mode, target);
	}
	
	/** 
	 * Changes the Posix File Permissions for the target file. Very useful in the post() hook<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param mode is the posix definition, ex: "777"
	 * @param recursive if <code>true</code> apply the same rule for all sub dirs
	 */
	protected void chmod(String mode, boolean recursive) {
		chmod(mode, target, recursive);
	}
	
	/** 
	 * Convert the target file into the patterns of the OS we are running on. Very useful in the post() hook<br>
	 * This is mostly required whenever you create a file in windows and than run it on Linux
	 * <p>
	 * Note: Usually config files are not an issue, but executable files are!!
	 */
	protected void normalize() {
		normalizeTextContent(target);	
	}
	
	/** 
	 * Changes ownership of the target file.  Very useful in the post() hook<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param user the new owner. the same information is used for the group
	 */
	protected void chown(String user) {
		chown(user, target);
	}
	
	/** 
	 * Changes ownership of the target file.  Very useful in the post() hook<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param user the new owner 
	 * @param group the new group
	 * @param recursive if <code>true</code> apply the same rule for all sub dirs
	 */	
	protected void chown(String user, String group, boolean recursive) {
		chown(user, group, target, recursive);
	}
	
	/** 
	 * Create a simbolic link to the target file.  Very useful in the post() hook<br>
	 * <p>
	 * Note: Currently Linux only
	 * 
	 * @param link the link path
	 */
	protected void ln(String link) {
		ln(link, target);
	}
	
	/**  Rename the target file/dir. Very useful in the post() hook */
	protected void renameTo(String to) {
        mv(target, to);
    }
	
	/** 
	 * Execute the target file. Very useful in the post() hook <br>
	 * If the file is not executable, we try to make it executable. 
	 * If it was not possible, an exception is thrown  
	 */
	protected void reloadFile() {
		execute(target);
	}
	
	/** returns the target directory. If it is already a dir returns it, otherwise return its parent */
	protected String getTargetDirectory(){
		Path path = Paths.get(target);
		if(Files.isDirectory(path)){
			return target;
		}
		return path.getParent().toString();
	}

	// ------ methods used by the framework only ----
	
	/** Used unically by {@link HookEvaluator} to set variables */
	void setSrc(String srcPath) {
		this.src = srcPath;
	}

	/** Used unically by {@link HookEvaluator} to set variables */
	void setTarget(String targetPath) {
		this.target = targetPath;
	}	
}