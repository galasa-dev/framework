/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import dev.galasa.framework.spi.FrameworkException;

public class UserStoreException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public UserStoreException() {
    }

    public UserStoreException(String message) {
        super(message);
    }

    public UserStoreException(Throwable cause) {
        super(cause);
    }

    public UserStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserStoreException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
