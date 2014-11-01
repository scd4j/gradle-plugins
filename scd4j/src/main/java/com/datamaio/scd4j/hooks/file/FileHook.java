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


public abstract class FileHook extends Hook {
	protected String src;
	protected String target;
	
	protected void chmod(String mode) {
		chmod(mode, target);
	}
	
	protected void chmod(String mode, boolean recursive) {
		chmod(mode, target, recursive);
	}
	
	protected void dos2unix() {
		dos2unix(target);	
	}
	
	protected void chown(String user) {
		chown(user, target);
	}
	
	protected void chown(String userAndGroup, boolean recursive) {
		chown(userAndGroup, target, recursive);
	}
	
	protected void ln(String link) {
		ln(link, target);
	}

	protected Destination rename() {
		return new Destination() {
			@Override
			public void to(String to) {
				command.ln(target, to);				
			}
		};		
    }
	
	protected void renameTo(String to) {
        mv(target, to);
    }
	
	protected void reloadFile() {
		execute(target);
	}
	
	protected String getTargetDirectory(){
		Path path = Paths.get(target);
		if(Files.isDirectory(path)){
			return target;
		}
		return path.getParent().toString();
	}

	protected void setSrc(String srcPath) {
		this.src = srcPath;
	}

	protected void setTarget(String targetPath) {
		this.target = targetPath;
	}	
}