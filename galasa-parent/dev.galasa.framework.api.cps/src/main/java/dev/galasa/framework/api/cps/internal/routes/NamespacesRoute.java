/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static dev.galasa.framework.api.cps.internal.verycommon.ServletErrorMessage.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import dev.galasa.framework.api.cps.internal.verycommon.InternalServletException;
import dev.galasa.framework.api.cps.internal.verycommon.QueryParameters;
import dev.galasa.framework.api.cps.internal.verycommon.ResponseBuilder;
import dev.galasa.framework.api.cps.internal.verycommon.ServletError;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;


/**
 * An abstract route used by all the Property-related routes.
 */
public class NamespacesRoute extends CPSRoute {

    static final Gson gson = GalasaGsonBuilder.build();

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };


    public NamespacesRoute(ResponseBuilder responseBuilder, IFramework framework ) {
		/* Regex to match endpoints: 
		*  -> /cps/
		*/
		super(responseBuilder, "/cps\\/?", framework);
	}

    protected IFramework getFramework() {
        return super.framework;
    }

    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        String namespaces = getNamespaces();
		return getResponseBuilder().buildResponse(response, "application/json", namespaces, HttpServletResponse.SC_OK); 
    }

    protected String getNamespaces() throws InternalServletException {
        logger.debug("Getting the list of namespaces");
        JsonArray namespaceArray = new JsonArray();
        List<String> namespaces;
        try {
            namespaces = getFramework().getConfigurationPropertyService("framework").getCPSNamespaces();
            for (String name : namespaces) {
                if ( ! hiddenNameSpaces.contains(name) ) {
                    namespaceArray.add(name);
                }
            }
        } catch (ConfigurationPropertyStoreException e) {
            ServletError error = new ServletError(GAL5015_CPS_STORE_ERROR);
			throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return gson.toJson(namespaceArray);
    }

    @Override
    public HttpServletResponse handlePutRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response) throws InternalServletException {
        ServletError error = new ServletError(GAL5405_METHOD_NOT_ALLOWED,pathInfo, request.getMethod());
        throw new InternalServletException(error, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        ServletError error = new ServletError(GAL5405_METHOD_NOT_ALLOWED,pathInfo, request.getMethod()); 
        throw new InternalServletException(error, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

}