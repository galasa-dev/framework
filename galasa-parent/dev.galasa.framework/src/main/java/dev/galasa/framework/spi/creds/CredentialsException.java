/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi.creds;

import dev.galasa.framework.spi.FrameworkException;

public class CredentialsException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public CredentialsException() {
	}

	public CredentialsException(String message) {
		super(message);
	}

	public CredentialsException(Throwable cause) {
		super(cause);
	}

	public CredentialsException(String message, Throwable cause) {
		super(message, cause);
	}

	public CredentialsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
