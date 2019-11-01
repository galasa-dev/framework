/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

public class FrameworkException extends Exception {
	private static final long serialVersionUID = 1L;

	public FrameworkException() {
	}

	public FrameworkException(String message) {
		super(message);
	}

	public FrameworkException(Throwable cause) {
		super(cause);
	}

	public FrameworkException(String message, Throwable cause) {
		super(message, cause);
	}

	public FrameworkException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
