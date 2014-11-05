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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.datamaio.scd4j.conf.Configuration;

public final class PathHelper {
	private Map<String, String> properties;
	private Path module;

	public PathHelper(Configuration conf){
		this(conf.getProperties(), conf.getModuleDir());
	}
	
	PathHelper(Map<String, String> properties, Path module){
		this.properties = properties;
		this.module = module;
	}
	
	public final Path getTargetWithoutSuffix(Path path, String sufix) {
		// remove o .delete do final
		String fileName = path.getFileName().toString();
		fileName = fileName.replaceAll(sufix, "");
		path = path.resolveSibling(fileName);
		return getTarget(path);
	}
	
	public final Path getTarget(Path path) {
		// pega o arquivo destino
		final Path relativized = this.module.relativize(path);
		final Path resolved = Paths.get("/").resolve(relativized);

		// resolve as variaveis de diretório
    	String str = replacePathVars(resolved.toString());
    	return Paths.get(str);
    }

	protected String replacePathVars(String srcPath) {
		final Pattern p = Pattern.compile("@([^@])*@");
    	final Matcher m = p.matcher(srcPath);
    	while(m.find()) {
    		final String key = m.group();
			final String value = (String) properties.get(key.replaceAll("@", ""));
			if(value!=null)
				srcPath = srcPath.replace(key, value);
			else 
				throw new IllegalStateException("Variavel " + key + " não foi declarada nas configuracoes. " +
						"Utilize ExternalConf ou System.properties");
    	}
		return srcPath;
	}
}