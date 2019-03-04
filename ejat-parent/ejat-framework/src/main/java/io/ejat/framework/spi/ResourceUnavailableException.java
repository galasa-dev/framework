package io.ejat.framework.spi;

public class ResourceUnavailableException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public ResourceUnavailableException() {
    }

    public ResourceUnavailableException(String message) {
        super(message);
    }

    public ResourceUnavailableException(Throwable cause) {
        super(cause);
    }

    public ResourceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceUnavailableException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
