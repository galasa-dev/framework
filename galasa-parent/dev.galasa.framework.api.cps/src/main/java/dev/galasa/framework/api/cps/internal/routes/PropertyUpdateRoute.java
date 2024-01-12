/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
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
        checkNamespaceExists(namespace);
        String property = retrieveProperty(namespace,propertyName);
		return getResponseBuilder().buildResponse(response, "application/json", property, HttpServletResponse.SC_OK); 
    }

    private String retrieveProperty (String namespaceName, String propertyName) throws FrameworkException {
        CPSFacade cps = new CPSFacade(framework);
        CPSNamespace namespace = cps.getNamespace(namespaceName);
        CPSProperty property = namespace.getProperty(propertyName);
        return buildResponseBody(property);
    }

    /*
     * Handle Put Request
     */
    @Override
    public HttpServletResponse handlePutRequest(String pathInfo, QueryParameters queryParameters, HttpServletRequest request , HttpServletResponse response)
            throws  IOException, FrameworkException {
        String namespaceName = getNamespaceFromURL(pathInfo);
        String name = getPropertyNameFromURL(pathInfo);
        checkRequestHasContent(request);
        ServletInputStream body = request.getInputStream();
        String jsonString = new String (body.readAllBytes(),StandardCharsets.UTF_8);
        body.close();
        checkNameMatchesRequest(name, jsonString);
        checkNamespaceExists(namespaceName);
        CPSProperty property = applyPropertyToStore(jsonString, namespaceName, true);
        String responseBody = String.format("Successfully updated property %s in %s",property.getName(), property.getNamespace());
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
        if (checkPropertyExists(namespace, propertyName)){
            framework.getConfigurationPropertyService(namespace).deleteProperty(propertyName);
        }else{
            ServletError error = new ServletError(GAL5017_PROPERTY_DOES_NOT_EXIST_ERROR,propertyName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }


}