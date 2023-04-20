 /*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;


public class InternalServletException extends Exception {
    
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
    
}