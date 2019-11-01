/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.docker.controller;

import dev.galasa.framework.spi.FrameworkException;

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
