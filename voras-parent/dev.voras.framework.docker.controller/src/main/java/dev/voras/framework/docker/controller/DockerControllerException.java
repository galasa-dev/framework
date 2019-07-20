package dev.voras.framework.docker.controller;

import dev.voras.framework.spi.FrameworkException;

public class DockerControllerException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public DockerControllerException() {
	}

	public DockerControllerException(String message) {
		super(message);
	}

	public DockerControllerException(Throwable cause) {
		super(cause);
	}

	public DockerControllerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DockerControllerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
