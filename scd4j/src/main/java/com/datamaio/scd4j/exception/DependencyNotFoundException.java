package com.datamaio.scd4j.exception;


public class DependencyNotFoundException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public DependencyNotFoundException() {
		super();
	}

	public DependencyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DependencyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DependencyNotFoundException(String message) {
		super(message);
	}

	public DependencyNotFoundException(Throwable cause) {
		super(cause);
	}

}
