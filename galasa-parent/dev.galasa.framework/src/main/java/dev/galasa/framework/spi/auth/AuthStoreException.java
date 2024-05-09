/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import dev.galasa.framework.spi.FrameworkException;

public class AuthStoreException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public AuthStoreException() {
    }

    public AuthStoreException(String message) {
        super(message);
    }

    public AuthStoreException(Throwable cause) {
        super(cause);
    }

    public AuthStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
