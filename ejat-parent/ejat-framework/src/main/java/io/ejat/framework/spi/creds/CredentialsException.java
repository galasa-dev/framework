package io.ejat.framework.spi.creds;

import io.ejat.framework.spi.FrameworkException;

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