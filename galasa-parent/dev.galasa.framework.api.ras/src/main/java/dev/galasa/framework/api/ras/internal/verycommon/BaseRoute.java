/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.verycommon;

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
