package com.datamaio.scd4j.util.io;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.apache.log4j.Logger;

/**
 * 
 * @author Mateus M. da Costa
 *
 */
public class MoveVistor extends SimpleFileVisitor<Path> {
	private static final Logger LOGGER = Logger.getLogger(MoveVistor.class);

	private int level = 0;
	protected Path fromPath;
	protected Path toPath;

	
    public MoveVistor(Path from, Path to){
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);
		
		this.fromPath = from;
		this.toPath = to;
    	LOGGER.trace("VISITOR INITIALIZED )");
    }
    

	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Path resolvedTargetDir = toPath;
		if(!fromPath.equals(dir)){
			final Path relativize = fromPath.relativize(dir);
			resolvedTargetDir = toPath.resolve(relativize);			
		}
		resolvedTargetDir = resolveVars(resolvedTargetDir);
		
		boolean goingToCreate = Files.notExists(resolvedTargetDir);
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(tabs() + "PRE VISIT DIR : " + dir + " (going to create target directory? " + goingToCreate + ")");
    	}
		
		if (goingToCreate) {
			Files.createDirectories(resolvedTargetDir);
		} 
		
		level++;
		return FileVisitResult.CONTINUE;
	}



	private boolean isEmpty(Path dir) {
		return dir.toFile().list().length == 0;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		final Path relativize = fromPath.relativize(file);
		Path resolvedTargetFile = toPath.resolve(relativize);
		resolvedTargetFile = resolveVars(resolvedTargetFile);
		
	
		LOGGER.trace(tabs() + "Moving FILE "+ file + " to " + resolvedTargetFile);
		move(file, resolvedTargetFile);
		

		return FileVisitResult.CONTINUE;
	}

	protected void remove(Path dir) throws IOException {
		Files.delete(dir);
	}

	protected void move(Path file, final Path resolvedTargetFile) throws IOException {
		Files.move(file, resolvedTargetFile, REPLACE_EXISTING);
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
		level--;
		
		if(isEmpty(dir)) {
			remove(dir);
		}

		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(tabs() + "POST VISIT DIR : " + dir );
		}
		
		return super.postVisitDirectory(dir, e);
	}
	
	private String tabs() {
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<level; i++)
			builder.append("\t");
		return builder.toString();
	}
	
	protected Path resolveVars(Path path) {
		return path;
	}
}
