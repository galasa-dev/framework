package io.ejat.framework.spi;

public class CredentialsStoreException extends FrameworkException {
	//private static final long serialVersionUID = 1L;

	public CredentialsStoreException() {
	}

	public CredentialsStoreException(String message) {
		super(message);
	}

	public CredentialsStoreException(Throwable cause) {
		super(cause);
	}

	public CredentialsStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public CredentialsStoreException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}