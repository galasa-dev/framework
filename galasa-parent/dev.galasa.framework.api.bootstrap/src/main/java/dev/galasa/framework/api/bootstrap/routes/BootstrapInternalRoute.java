/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap.routes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.bootstrap.internal.BootstrapProperties;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

public class BootstrapInternalRoute extends BaseRoute{

    private final ArrayList<String> bootstrapKeys           = new ArrayList<>(Arrays.asList("framework.config.store",
            "framework.extra.bundles", "framework.testcatalog.url"));

    private BootstrapProperties bootstrap;

    public BootstrapInternalRoute(ResponseBuilder responseBuilder, IFramework framework) {
        super(responseBuilder, "");
        this.bootstrap = new BootstrapProperties(framework, bootstrapKeys);
        logger.info("Galasa Bootstrap API activated");
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, 
            HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, FrameworkException {
        Properties actualBootstrap = bootstrap.getProperties();
        actualBootstrap.store(response.getWriter(), "Galasa Bootstrap Properties");
        response = getResponseBuilder().buildResponseHeaders(response, "text/plain", HttpServletResponse.SC_OK);
        return response;
    }

}