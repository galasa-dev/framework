/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.spi;

/**
 * An exception which also holds a FrameworkErrorCode to provide more fine-grained detail about the problem
 * being raised/thrown, which is programmatically accessible. ie: Not just the error text, or instance of the
 * exception instance.
 *
 * Error codes can be explicitly set when the exception is created, or will default to
 * {@link FrameworkErrorCode#UNKNOWN}, or to the same error code from the cause (when that cause is also a
 * {@link FrameworkException}.
 *
 * This allows one type of FrameworkException to wrap another, and the original cause error code will
 * be propagated to the top-most FrameworkException in the chain.
 *
 */
public class FrameworkException extends Exception {
    private static final long serialVersionUID = 1L;

    private FrameworkErrorCode errorCode ;

    public FrameworkException() {
        this(FrameworkErrorCode.UNKNOWN);
    }


    public FrameworkException(FrameworkErrorCode code) {
        super();
        this.errorCode = code ;
    }

    public FrameworkException(String message) {
        this(FrameworkErrorCode.UNKNOWN , message);
    }

    public FrameworkException(FrameworkErrorCode code, String message) {
        super(message);
        this.errorCode = code ;
    }

    /**
     * If the cause is a framework exception, then the error code from the cause is propagated
     * into this exception instance.
     */
    public FrameworkException(Throwable cause) {
        this( (cause instanceof FrameworkException) ?
                ((FrameworkException)cause).getErrorCode() :
                FrameworkErrorCode.UNKNOWN
            , cause );
    }

    public FrameworkException(FrameworkErrorCode code, Throwable cause) {
        super(cause);
        this.errorCode = code ;
    }

    /**
     * If the cause is a framework exception, then the error code from the cause is propagated
     * into this exception instance.
     */
    public FrameworkException(String message, Throwable cause) {
        this((cause instanceof FrameworkException) ?
                        ((FrameworkException)cause).getErrorCode() :
                        FrameworkErrorCode.UNKNOWN,
                message, cause);
    }


    public FrameworkException(FrameworkErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = code;
    }

    /**
     * If the cause is a framework exception, then the error code from the cause is propagated
     * into this exception instance.
     */
    public FrameworkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        this((cause instanceof FrameworkException) ?
                ((FrameworkException)cause).getErrorCode() :
                FrameworkErrorCode.UNKNOWN,
            message, cause, enableSuppression, writableStackTrace);
    }


    public FrameworkException(FrameworkErrorCode code, String message, Throwable cause, boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = code;
    }



    public FrameworkErrorCode getErrorCode() {
        return this.errorCode;
    }
}
