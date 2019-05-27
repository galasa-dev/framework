package dev.cirillo.k8s.controller;

import io.ejat.framework.spi.FrameworkException;

public class K8sControllerException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public K8sControllerException() {
	}

	public K8sControllerException(String message) {
		super(message);
	}

	public K8sControllerException(Throwable cause) {
		super(cause);
	}

	public K8sControllerException(String message, Throwable cause) {
		super(message, cause);
	}

	public K8sControllerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
