/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

public class ConfidentialTextException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public ConfidentialTextException() {
	}

	public ConfidentialTextException(String message) {
		super(message);
	}

	public ConfidentialTextException(Throwable cause) {
		super(cause);
	}

	public ConfidentialTextException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfidentialTextException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
