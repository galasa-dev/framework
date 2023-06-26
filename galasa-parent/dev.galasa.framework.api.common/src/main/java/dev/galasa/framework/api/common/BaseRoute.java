/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseRoute implements IRoute {

    protected Log logger = LogFactory.getLog(this.getClass());
	
	private final ResponseBuilder responseBuilder ;
	
    private final String path;

    public BaseRoute(ResponseBuilder responseBuilder , String path) {
        this.path = path;
		this.responseBuilder = responseBuilder ;
    }

    public String getPath() {
        return path;
    }

	public ResponseBuilder getResponseBuilder() {
		return this.responseBuilder;
	}

}
