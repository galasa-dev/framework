/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap.routes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

public class BootstrapInternalRoute extends BaseRoute{

    private IFramework               framework;  

    private final Properties        configurationProperties = new Properties();
    
    private final ArrayList<String> bootstrapKeys           = new ArrayList<>(Arrays.asList("framework.config.store",
            "framework.extra.bundles", "framework.testcatalog.url"));

    public BootstrapInternalRoute(ResponseBuilder responseBuilder, IFramework framework) {
        super(responseBuilder, "");
        this.framework = framework;
        logger.info("Galasa Bootstrap API activated");
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, 
            HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, FrameworkException {
        getLatestProperitesFromFramework();
        Properties actualBootstrap = new Properties();
        synchronized (this.configurationProperties) {
            actualBootstrap.putAll(this.configurationProperties);
        }
        actualBootstrap.store(response.getWriter(), "Galasa Bootstrap Properties");
        response = getResponseBuilder().buildResponseHeaders(response, "text/plain", HttpServletResponse.SC_OK);
        return response;
    }


    private void getLatestProperitesFromFramework() throws ConfigurationPropertyStoreException {
        Map<String,String> properties = framework.getConfigurationPropertyService("bootstrap").getAllProperties();
        synchronized (configurationProperties) {
            for (String key : bootstrapKeys) {
                String value = (String) properties.get(key);
                if (value != null) {
                    this.configurationProperties.put(key, value);
                } else {
                    this.configurationProperties.remove(key);
                }
            }
        }
    }

}