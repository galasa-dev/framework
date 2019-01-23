package io.ejat.framework.spi;

public class ConfigurationPropertyStoreException extends Exception {
	private static final long serialVersionUID = 1L;

	public ConfigurationPropertyStoreException() {
	}

	public ConfigurationPropertyStoreException(String message) {
		super(message);
	}

	public ConfigurationPropertyStoreException(Throwable cause) {
		super(cause);
	}

	public ConfigurationPropertyStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationPropertyStoreException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
