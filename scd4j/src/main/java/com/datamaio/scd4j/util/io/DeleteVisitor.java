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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.apache.log4j.Logger;

/**
 * 
 * @author Fernando Rubbo
 */
public class DeleteVisitor extends SimpleFileVisitor<Path> {
	private static final int NONE = Integer.MAX_VALUE;
	private static final Logger LOGGER = Logger.getLogger(DeleteVisitor.class);
	
	private int level = 0;			
    private int goingToDeleteDirAtLevel = NONE;
    private final PathMatcher matcher;

	public DeleteVisitor(){
    	this("*");
    }
    
    public DeleteVisitor(String glob){
    	Objects.requireNonNull(glob);
    	this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
    	LOGGER.trace("VISITOR INITIALIZED (Matcher: " + glob + ")");
    }
    
    public DeleteVisitor(PathMatcher matcher) {
    	Objects.requireNonNull(matcher);    	
    	this.matcher = matcher;
    	LOGGER.trace("VISITOR INITIALIZED (Matcher: " + matcher + ")");
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {		    	
    	if(goingToDeleteDirAtLevel==NONE && matcher.matches(dir.getFileName())) {
			goingToDeleteDirAtLevel = level;
		}
    	
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(tabs() + "PRE VISIT DIR : " + dir + "(going to delete? " + (level>=goingToDeleteDirAtLevel) + ")");
    	}
    	
		level++;		
		return super.preVisitDirectory(dir, attrs);
    }
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		boolean goingToDelete = mustDelete(file);
		
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(tabs() + "VISIT FILE "+ file  + "(going to delete? " + goingToDelete + ")");
		}
		
		if(goingToDelete) {
			delete(file);	
		}
		return FileVisitResult.CONTINUE;
	}

	protected void delete(Path file) throws IOException {
		Files.delete(file);
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
		level--;
		boolean goingToDelete = mustDelete(dir);

		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(tabs() + "POST VISIT DIR : " + dir + "(going to delete? " + goingToDelete + ")");
		}

		if(goingToDeleteDirAtLevel==level) {
			goingToDeleteDirAtLevel = NONE;
		}
    	
		if (e == null) {
			if(goingToDelete) {
				delete(dir);	
			}			
			return FileVisitResult.CONTINUE;
		}
		throw e;
	}

	protected boolean mustDelete(Path path) {
		return level>goingToDeleteDirAtLevel || matcher.matches(path.getFileName());
	}

	private String tabs() {
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<level; i++)
			builder.append("\t");
		return builder.toString();
	}
}