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
package com.datamaio.fwk.io;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

import org.apache.log4j.Logger;

public final class FileUtils {
	
	private static final Logger LOGGER = Logger.getLogger(FileUtils.class);
	
	private FileUtils(){}
	
	/** Deleta um arquivo ou um diretorio inteiro */
	public static void delete(Path path) {
		delete(path, "*");
	}
	
	/** 
	 * Deleta um arquivo ou um diretorio
	 * 
	 * @param path caminho que se deseja apagar
	 * @param glob padrao glob para arquivos. Ex: <code>*.txt</code> ira apagar apenas arquivos .txt 
	 */
	public static void delete(Path path, String glob) {
		try{ 
			if(Files.exists(path)) {
				if(Files.isDirectory(path)) {
					Files.walkFileTree(path, new DeleteVisitor(glob));
				} else {
					PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
					if(matcher.matches(path.getFileName())) {
						Files.deleteIfExists(path);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void deleteDir(Path dir, DeleteVisitor visitor) {
		try{ 
			if(Files.exists(dir) && Files.isDirectory(dir)) {
				Files.walkFileTree(dir, visitor);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	/** 
	 * Copia todos arquivos da fonte para o destino. <br>
	 * Isto irá sobrescrever todo e qualquer arquivo do destino
	 * 
	 * @param source fonte. Pode ser aquivo ou diretório
	 * @param target destino. Pode ser aquivo ou diretório
	 */
	public static void copy(Path source, Path target) {
		copy(source, target, "*");						
	}

	/** 
	 * Copia ALGUNS arquivos da fonte para o destino.<br>
	 * Isto irá sobrescrever todo e qualquer arquivo do destino
	 * 
	 * @param source fonte. Pode ser aquivo ou diretório
	 * @param target destino. Pode ser aquivo ou diretório
	 * @param glob padrão glob para arquivos. Ex: <code>*.txt</code> irá copiar apenas arquivos .txt 
	 */
	public static void copy(Path source, Path target, String glob) {
		try{
			if(Files.isDirectory(source)) {	
				Files.walkFileTree(source, new CopyVisitor(source, target, glob));
			} else if (Files.isRegularFile(source)) {
				if(Files.isDirectory(target)) {
					Path targetFile = PathUtils.get(target, source.getFileName());
					Files.copy(source, targetFile, REPLACE_EXISTING);
				} else {
					// Se o diretório onde estamos querendo colocar o arquivo Não existir, lança uma NoSuchFileException
					// Motivo: eu Não tenho como inferir se o target é um dir ou um file
					Files.copy(source, target, REPLACE_EXISTING);
				}
			} else {
				throw new RuntimeException("Não implementado!");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void copy(CopyVisitor visitor) {
		try {
			Files.walkFileTree(visitor.fromPath, visitor);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void copyFile(Path source, Path target) {
		try {
			Files.copy(source, target, REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/** Método helper para criar um arquivo em um determinado diretório com um determinado nome */
	public static Path createFile(Path dir, String name) {
		try {
			return Files.createFile(PathUtils.get(dir, name));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
	}

	public static Path createDirectories(Path dir) {
		try {
			if (Files.notExists(dir)) {
				LOGGER.trace(dir + " does not exist. Creating...");
				Files.createDirectories(dir);
			}
			return dir;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
	}
	
	public static String read(Path file) {
		return read(file, Charset.defaultCharset());
	}
	
	public static String read(Path file, Charset charset) {
		try {
			return new String(Files.readAllBytes(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<Path> ls(Path dir) {
		try {
			return Files.list(dir).collect(toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void move(Path from, Path to) {
		try {
			Files.move(from, to, ATOMIC_MOVE, REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void createSymbolicLink(Path link, Path targetFile) {
		try {
			Files.createSymbolicLink(link, targetFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
