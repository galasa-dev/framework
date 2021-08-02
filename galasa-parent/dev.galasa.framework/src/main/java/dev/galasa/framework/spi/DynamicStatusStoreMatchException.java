/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.framework.spi;

public class DynamicStatusStoreMatchException extends DynamicStatusStoreException {
    private static final long serialVersionUID = 1L;

    public DynamicStatusStoreMatchException() {
    }

    public DynamicStatusStoreMatchException(String message) {
        super(message);
    }

    public DynamicStatusStoreMatchException(Throwable cause) {
        super(cause);
    }

    public DynamicStatusStoreMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicStatusStoreMatchException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
