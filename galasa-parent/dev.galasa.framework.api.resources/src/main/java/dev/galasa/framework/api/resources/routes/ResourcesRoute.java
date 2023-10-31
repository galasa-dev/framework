/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class ResourcesRoute  extends BaseRoute{
    static final Gson gson = GalasaGsonBuilder.build();

    private IFramework framework;

    public ResourcesRoute(ResponseBuilder responseBuilder,  IFramework framework ) {
        super(responseBuilder, "\\/?");
        this.framework = framework;
    }

    protected IFramework getFramework() {
        return this.framework;
    }

    @Override
     public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters, 
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        
        checkRequestHasContent(request);
        return response;
    }
}
