/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.gendocs;

public class ManagerDocsException extends Exception {
    private static final long serialVersionUID = 1L;

    public ManagerDocsException() {
    }

    public ManagerDocsException(String message) {
        super(message);
    }

    public ManagerDocsException(Throwable cause) {
        super(cause);
    }

    public ManagerDocsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagerDocsException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
