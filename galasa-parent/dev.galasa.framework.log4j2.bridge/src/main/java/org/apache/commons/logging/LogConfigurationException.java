/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package org.apache.commons.logging;

public class LogConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LogConfigurationException() {
        super();
    }

    public LogConfigurationException(String message) {
        super(message);
    }

    public LogConfigurationException(Throwable cause) {
        super(cause);
    }

    public LogConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
