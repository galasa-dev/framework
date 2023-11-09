/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaProperty;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

/**
 * A route used by all the Property-related Requests.
 */
public class PropertyUpdateRoute extends CPSRoute {

    public PropertyUpdateRoute(ResponseBuilder responseBuilder, IFramework framework ) {
		/* Regex to match endpoints: 
		*  -> /cps/<namespace>/properties/<propertyName>
		*/
		super(responseBuilder, "\\/([a-z0-9]+)/properties/([a-zA-Z0-9.]+)", framework);
	}

    /*
     * Handle Get Request
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String propertyName = getPropertyNameFromURL(pathInfo);
        String property = retrieveProperty(namespace,propertyName);
		return getResponseBuilder().buildResponse(response, "application/json", property, HttpServletResponse.SC_OK); 
    }

    private String retrieveProperty (String namespaceName, String propertyName) throws FrameworkException {
        GalasaProperty property = null;
        Map.Entry<String, String> entry = propertyUtility.retrieveSingleProperty(namespaceName, propertyName);
        if (entry != null){
            property = new GalasaProperty(entry);
        }
        return buildResponseBody(property);
    }

    /*
     * Handle Put Request
     */
    @Override
    public HttpServletResponse handlePutRequest(String pathInfo, QueryParameters queryParameters, HttpServletRequest request , HttpServletResponse response)
            throws  IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String name = getPropertyNameFromURL(pathInfo);
        checkRequestHasContent(request);
        String value = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
        GalasaProperty property = new GalasaProperty(namespace, name, value);
        propertyUtility.setProperty(property, true);
        String responseBody = String.format("Successfully updated property %s in %s",name, namespace);
        return getResponseBuilder().buildResponse(response, "text/plain", responseBody, HttpServletResponse.SC_OK); 
    }


    /*
     * Handle Delete Request
     */
    public HttpServletResponse handleDeleteRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response)
            throws FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String property = getPropertyNameFromURL(pathInfo);
        deleteProperty(namespace, property);
        String responseBody = String.format("Successfully deleted property %s in %s",property, namespace);
        return getResponseBuilder().buildResponse(response, "text/plain", responseBody, HttpServletResponse.SC_OK);
    }


    private void deleteProperty(String namespace, String propertyName) throws FrameworkException {
        if (propertyUtility.checkPropertyExists(namespace, propertyName)){
            framework.getConfigurationPropertyService(namespace).deleteProperty(propertyName);
        }else{
            ServletError error = new ServletError(GAL5017_PROPERTY_DOES_NOT_EXIST_ERROR,propertyName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }


}