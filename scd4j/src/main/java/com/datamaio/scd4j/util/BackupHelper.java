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
package com.datamaio.scd4j.util;

import java.nio.file.Files;
import java.nio.file.Path;

import com.datamaio.scd4j.conf.Configuration;
import com.datamaio.scd4j.util.io.FileUtils;
import com.datamaio.scd4j.util.io.PathUtils;

/**
 * 
 * @author Fernando Rubbo
 * @author Mateus M. da Costa
 */
public class BackupHelper {
	private Path dir;

	public BackupHelper(Configuration conf) {
		init(conf);
	}

	public void init(Configuration conf) {
		this.dir = conf.getBackupDir();
	}

	public void backupFileOrDir(Path fileOrDir) {
		if(Files.exists(fileOrDir)) {
			Path bkp = PathUtils.get(dir, fileOrDir);
			if(Files.isDirectory(bkp)) {
				FileUtils.createDirectories(bkp);
			} else {
				FileUtils.createDirectories(bkp.getParent());
			}
			FileUtils.copy(fileOrDir, bkp);
		}
	}

	public void backupFile(final Path file) {
		if(Files.exists(file)) {
			Path bkp = PathUtils.get(dir, file);
			FileUtils.createDirectories(bkp.getParent());
			FileUtils.copyFile(file, bkp);
		}
	}
}
