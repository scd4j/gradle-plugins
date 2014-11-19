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
package com.datamaio.scd4j.util.io;

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 
 * @author Fernando Rubbo
 */
public final class PathUtils {
	
	private PathUtils(){}
	
	/** Delegate para @link {@link Paths#get(String, String...)} */
	public static Path get(String first, String... more) {
		return Paths.get(first, more);
	}
	
	/** Método helper para pegar o caminho de um arquivo */
	public static Path get(Path dir, Path... more) {
		String[] moreStr = Arrays.stream(more)
							.map(p -> p.toString())
							.collect(toList())
							.toArray(new String[]{});
		return get(dir, moreStr);
	}
	
	/** Método helper para pegar o caminho de um arquivo */
	public static Path get(Path dir, String... more) {
		String[] moreStr = Arrays.stream(more)
				.map(p -> path2str(p))
				.collect(toList())
				.toArray(new String[]{});
		return Paths.get(dir.toString().replace("\\", "/"), moreStr);
	}

	static String path2str(String p) {
		return p.replace("\\", "/").replaceFirst("([a-zA-Z]\\:)", "");
	}
	
	/** Método helper para resolver o caminho de um arquivo em um destino */
	public static Path resolve(Path file, Path srcDir, Path destDir) {
		return destDir.resolve(srcDir.relativize(file));
	}
}
