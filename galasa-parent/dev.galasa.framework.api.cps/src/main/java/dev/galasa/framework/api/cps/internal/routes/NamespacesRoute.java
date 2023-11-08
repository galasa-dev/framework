/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.cps.internal.common.Namespace;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;


/**
 * An abstract route used by all the Property-related routes.
 */
public class NamespacesRoute extends CPSRoute {

    private static final Gson gson = GalasaGsonBuilder.build();


    public NamespacesRoute(ResponseBuilder responseBuilder, IFramework framework ) {
		/* Regex to match endpoints: 
		*  -> /cps/
		*/
		super(responseBuilder, "\\/?", framework);
	}

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        String namespaces = getNamespaces(req.getRequestURI());
		return getResponseBuilder().buildResponse(response, "application/json", namespaces, HttpServletResponse.SC_OK); 
    }

    private String getNamespaces(String url) throws InternalServletException {
        logger.debug("Getting the list of namespaces");
        List<Namespace> namespaceArray = new ArrayList<Namespace>();
        try {
            List<String> namespaces;
            namespaces = getFramework().getConfigurationPropertyService("framework").getCPSNamespaces();
            Collections.sort(namespaces);
            for (String name : namespaces) {
                Namespace namespace = new Namespace(name, propertyUtility.getNamespaceType(name), url);
                if (!propertyUtility.isHiddenNamespace(name) ) {
                    namespaceArray.add(namespace);
                }
            }
        } catch (ConfigurationPropertyStoreException e) {
            ServletError error = new ServletError(GAL5015_INTERNAL_CPS_ERROR);
			throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return gson.toJson(namespaceArray);
    }

}