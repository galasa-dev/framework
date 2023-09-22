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
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

/**
 * A route used by all the Property-related Requests.
 */
public class PropertyRoute extends CPSRoute {

    public PropertyRoute(ResponseBuilder responseBuilder, IFramework framework ) {
		/* Regex to match endpoints: 
		*  -> /cps/<namespace>/properties/<propertyName>
		*/
		super(responseBuilder, "/cps/(.*)/properties/(.*)", framework);
	}

    protected String getPropertyNameFromURL(String pathInfo){
        String[] namespace = pathInfo.split("/");
        return namespace[4];
    }
    
    private Map.Entry<String, String> retrieveSingleProperty(String namespace, String propertyName) throws  FrameworkException {
        try{
            Map<String, String> properties = getAllProperties(namespace);
           for (Map.Entry<String, String> entry : properties.entrySet()) {
               String key = entry.getKey().toString();
               if (key.equals(propertyName)){
                   return entry;
               }
           }
        }catch (Exception e){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return null;
    }

    private boolean checkPropertyExists (String namespace, String propertyName) throws FrameworkException{
        Map.Entry<String, String> entry = retrieveSingleProperty(namespace, propertyName);
        boolean exists = true;
        if( entry  == null){
            exists = false;
        }
        return exists;
    }

    private void checkRequestHasContent(HttpServletRequest request, String pathInfo) throws InternalServletException{
        boolean valid = false;
        try{
            if (request.getContentLength() >0){
                valid = true;
            }
        }catch (NullPointerException e ){
            //Catch the NullPointerException (empty request body) 
            //Exception is thrown by the if below
        }  
        if (!valid){
            ServletError error = new ServletError(GAL5411_NO_REQUEST_BODY,pathInfo);  
            throw new InternalServletException(error, HttpServletResponse.SC_LENGTH_REQUIRED);
        }
    }

    /*
     * Handle Get Request
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String propertyName = getPropertyNameFromURL(pathInfo);
        String  property= retrieveProperty(namespace,propertyName);
		return getResponseBuilder().buildResponse(response, "application/json", property, HttpServletResponse.SC_OK); 
    }

    private String retrieveProperty (String namespace, String propertyName) throws FrameworkException {
        Map.Entry<String, String> entry = retrieveSingleProperty(namespace, propertyName);
        return buildResponseBody(namespace, entry);
    }

    /*
     * Handle Put Request
     */
    @Override
    public HttpServletResponse handlePutRequest(String pathInfo, QueryParameters queryParameters, HttpServletRequest request , HttpServletResponse response)
            throws  IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String property = getPropertyNameFromURL(pathInfo);
        checkRequestHasContent(request, pathInfo);
        String value = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
        setProperty(namespace, property, value);
        String responseBody = String.format("Successfully created property %s in %s",property, namespace);
        return getResponseBuilder().buildResponse(response, "application/json", responseBody, HttpServletResponse.SC_CREATED); 
    }

    private void setProperty(String namespace, String propertyName, String value) throws FrameworkException {
        if (!checkPropertyExists(namespace, propertyName)){
            getFramework().getConfigurationPropertyService(namespace).setProperty(propertyName, value);
        }else{
            ServletError error = new ServletError(GAL5018_PROPERTY_ALREADY_EXISTS_ERROR, propertyName ,namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /*
     * Handle Post Request
     */
    @Override
    public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response)
            throws  IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String property = getPropertyNameFromURL(pathInfo);
        checkRequestHasContent(request, pathInfo);
        String value = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
        updateProperty(namespace, property, value);
        String responseBody = String.format("Successfully updated property %s in %s",property, namespace);
        return getResponseBuilder().buildResponse(response, "application/json", responseBody, HttpServletResponse.SC_CREATED); 
    }

    private void updateProperty(String namespace, String propertyName, String value) throws FrameworkException {
        if (checkPropertyExists(namespace, propertyName)){
            getFramework().getConfigurationPropertyService(namespace).setProperty(propertyName, value);
        }else{
            ServletError error = new ServletError(GAL5017_PROPERTY_DOES_NOT_EXIST_ERROR,propertyName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
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
        return getResponseBuilder().buildResponse(response, "application/json", responseBody, HttpServletResponse.SC_OK);
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