/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.verycommon;

import dev.galasa.framework.spi.FrameworkException;

public class InternalServletException extends FrameworkException {
    
    ServletError servletError;
    int httpFailureCode;

    public InternalServletException(ServletError servletError, int httpFailureCode ){
        this.servletError = servletError;
        this.httpFailureCode = httpFailureCode;
    }

    public ServletError getError() {
        return this.servletError;
    }
    public int getHttpFailureCode(){
        return this.httpFailureCode;
    }
    
    @Override
    public String getMessage() {
        return servletError.toString();
    }
}