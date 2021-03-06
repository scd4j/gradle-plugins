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
package com.datamaio.scd4j.conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.datamaio.scd4j.util.EncodingHelper;
import com.datamaio.scd4j.util.Encryptor;

/**
 * 
 * @author Fernando Rubbo
 */
public class Config extends Properties {
	
	private static final long serialVersionUID = 1L;
	private static final EncodingHelper encodingHelper = new EncodingHelper();
	private static Pattern variable = Pattern.compile("\\$\\{([^\\$\\{])*\\}");
	
	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		super.load(inStream);
		merge();
	}
	
	@Override
	public synchronized void load(Reader reader) throws IOException {
		super.load(reader);
		merge();
	}
	
	@Override
	public synchronized void loadFromXML(InputStream in) throws IOException,
			InvalidPropertiesFormatException {
		super.loadFromXML(in);
		merge();
	}
	
	public synchronized Config load(final Path configFile) {
        if (configFile!=null) {
        	final Charset cs = inferFileCharset(configFile);
    		try (BufferedReader reader = Files.newBufferedReader(configFile, cs)) {
    			super.load(reader);
    		} catch (IOException e) {
    			throw new RuntimeException(e);
    		}
    		   		
    		merge();
        }
        return this;
	}


	private void merge() {
		final Map<String,String> toBeAdded = new HashMap<>();
		final Set<String> toBeRemoved = new HashSet<>();
		
		// faz este loop apenas para garantir que, se tiver alguma propriedade criptografada, ela sera descriptografada
		// também resolve as variáveis
		for (final Object key : keySet()) {
			final String value = (String) get(key);
			final String newKey = replaceDotsWithUnderscore(toBeAdded, toBeRemoved, key.toString(), value);
			resolveAndDecript(toBeAdded, newKey, value);
		}
		toBeRemoved.forEach(keyWithDot -> Config.this.remove(keyWithDot));
		toBeAdded.forEach((newKey, value) -> Config.this.put(newKey, value));

		// pega todas as propriedades do sistema e sobreescreve as configuradas no arquivo de propriedades
		toBeAdded.clear();
		final Properties sysProps = System.getProperties();
		final Set<Object> sysPropsKeySet = sysProps.keySet();
		for (final Object key : sysPropsKeySet) {
			final String value = (String) sysProps.get(key);
			final String newKey = replaceDotsWithUnderscore(key.toString());
			resolveAndDecript(toBeAdded, newKey, value);
		}
		toBeAdded.forEach((newKey, value) -> Config.this.put(newKey, value));
	}
	
	String replaceDotsWithUnderscore(final Map<String,String> toBeAdded, final Set<String> toBeRemoved, final String key, final String value) {
		if(key.contains(".")){
			final String newKey = key.replaceAll("\\.", "_");
			toBeRemoved.add(key);
			toBeAdded.put(newKey, value);
			return newKey;
		}
		return key;
	}
	
	void resolveAndDecript(Map<String,String> toBeAdded, final String key, final String value) {
        // para as propriedades cryptografadas, decriptografa..
		if(value!=null && value.startsWith(Encryptor.PREFIX)) {
			final String decryptedValue = Encryptor.get().decrypt(value);
			toBeAdded.put(key, decryptedValue);
		} else {
			toBeAdded.put(key, resolve(value));
		}
	}
	
	String replaceDotsWithUnderscore(final String key) {
		if(key.contains(".")){
			return key.replaceAll("\\.", "_");
		}
		return key;
	}
	
	String resolve(String value) {
        final Matcher m = variable.matcher(value);
        while(m.find()) {
            final String key = m.group();
            final String keyProp = key.substring(0, key.length()-1).substring(2);

            String innerValue = System.getProperties().getProperty(keyProp);
            if(innerValue==null) {
                innerValue = getProperty(keyProp);
            }

            if(innerValue!=null){
                final Matcher m2 = variable.matcher(innerValue);
                if(m2.find()){
                    innerValue = resolve(innerValue);
                }
                value = value.replace(key, innerValue);
            }
        }

        return value;
    }
	
	private Charset inferFileCharset(final Path configFile) {
		return encodingHelper.getCharset(configFile.toString());
	}
}
