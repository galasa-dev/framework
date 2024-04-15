/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap.routes;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.spi.FrameworkException;

public class BootstrapInternalRoute extends BaseRoute {

    private Properties configurationProperties;

    public BootstrapInternalRoute(ResponseBuilder responseBuilder, Properties configurationProperties) {
        super(responseBuilder, "");
        this.configurationProperties = configurationProperties;
        logger.info("Galasa Bootstrap API activated");
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        Properties actualBootstrap = new Properties();
        synchronized (this.configurationProperties) {
            actualBootstrap.putAll(this.configurationProperties);
        }

        actualBootstrap.store(response.getWriter(), "Galasa Bootstrap Properties");
        response = getResponseBuilder().buildResponseHeaders(response, "text/plain", HttpServletResponse.SC_OK);
        return response;
    }

}
