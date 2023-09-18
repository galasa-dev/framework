/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static dev.galasa.framework.api.cps.internal.verycommon.ServletErrorMessage.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
public class PropertyRoute extends CPSRoute {

    static final Gson gson = GalasaGsonBuilder.build();

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };


    public PropertyRoute(ResponseBuilder responseBuilder, IFramework framework ) {
		/* Regex to match endpoints: 
		*  -> /cps/
		*/
		super(responseBuilder, "/cps/(.*)/properties/(.*)", framework);
	}

    protected IFramework getFramework() {
        return super.framework;
    }

    protected String getPropertyNameFromURL(String pathInfo){
        String[] namespace = pathInfo.split("/");
        return namespace[4];
    }
    
    private Map.Entry<String, String> retrieveProperty(String namespace, String propertyName) throws  FrameworkException {
        try{
           Map<String, String> properties = getAllProperties(namespace);
           for (Map.Entry<String, String> entry : properties.entrySet()) {
               String key = entry.getKey().toString();
               if (key.equals(propertyName)){
                   return entry;
               }
           }
        }catch (Exception e){
            ServletError error = new ServletError(GAL5017_INVALID_NAMESPACE_ERROR,namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return null;
    }

    private boolean checkPropertyExists (String namespace, String propertyName) throws FrameworkException{
        Map.Entry<String, String> entry = retrieveProperty(namespace, propertyName);
        if( entry  == null){
            return false;
        }
        return true;
    }

    /*
     * Handle Get Request
     */
    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String propertyName = getPropertyNameFromURL(pathInfo);
        String  property= getProperty(namespace,propertyName);
		return getResponseBuilder().buildResponse(response, "application/json", property, HttpServletResponse.SC_OK); 
    }

    private String getProperty(String namespace, String propertyName) throws FrameworkException {
        Map.Entry<String, String> entry = retrieveProperty(namespace, propertyName);
        JsonArray propertyArray = new JsonArray();
        if (entry != null){
            JsonObject cpsProp = new JsonObject();
            cpsProp.addProperty("name", entry.getKey());
            cpsProp.addProperty("value", getProtectedValue(entry.getValue(),namespace));
            propertyArray.add(cpsProp);
        }
        return gson.toJson(propertyArray);
    }

    /*
     * Handle Put Request
     */
    @Override
    public HttpServletResponse handlePutRequest(String pathInfo, QueryParameters queryParameters, HttpServletRequest request , HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String property = getPropertyNameFromURL(pathInfo);
        try{
        if (request.getContentLength() >0){
            String value = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
            setProperty(namespace, property, value);
        }else{
            ServletError error = new ServletError(GAL5411_NO_REQUEST_BODY,pathInfo);  
            throw new InternalServletException(error, HttpServletResponse.SC_LENGTH_REQUIRED);
        }
        }catch (NullPointerException e ){
            ServletError error = new ServletError(GAL5411_NO_REQUEST_BODY,pathInfo);  
            throw new InternalServletException(error, HttpServletResponse.SC_LENGTH_REQUIRED);
        }
        String responseBody = String.format("Successfully created property %s in %s",property, namespace);
        return getResponseBuilder().buildResponse(response, "application/json", responseBody, HttpServletResponse.SC_CREATED); 
    }

    private void setProperty(String namespace, String propertyName, String value) throws FrameworkException {
        if (!checkPropertyExists(namespace, propertyName)){
            framework.getConfigurationPropertyService(namespace).setProperty(propertyName, value);
        }else{
            ServletError error = new ServletError(GAL5019_PROPERTY_ALREADY_EXISTS_ERROR, propertyName ,namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /*
     * Handle Post Request
     */
    @Override
    public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String property = getPropertyNameFromURL(pathInfo);
        try{
        if (request.getContentLength() >0){
            String value = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
            updateProperty(namespace, property, value);
        }else{
            ServletError error = new ServletError(GAL5411_NO_REQUEST_BODY,pathInfo);  
            throw new InternalServletException(error, HttpServletResponse.SC_LENGTH_REQUIRED);
        }
        }catch (NullPointerException e ){
            ServletError error = new ServletError(GAL5411_NO_REQUEST_BODY,pathInfo);  
            throw new InternalServletException(error, HttpServletResponse.SC_LENGTH_REQUIRED);
        }
        String responseBody = String.format("Successfully updated property %s in %s",property, namespace);
        return getResponseBuilder().buildResponse(response, "application/json", responseBody, HttpServletResponse.SC_CREATED); 
    }

    private void updateProperty(String namespace, String propertyName, String value) throws FrameworkException {
        if (checkPropertyExists(namespace, propertyName)){
            framework.getConfigurationPropertyService(namespace).setProperty(propertyName, value);
        }else{
            ServletError error = new ServletError(GAL5018_PROPERTY_DOES_NOT_EXIST_ERROR,propertyName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /*
     * Handle Delete Request
     */
    private void deleteProperty(String namespace, String propertyName) throws FrameworkException {
            if (checkPropertyExists(namespace, propertyName)){
            framework.getConfigurationPropertyService(namespace).deleteProperty(propertyName);
        }else{
            ServletError error = new ServletError(GAL5018_PROPERTY_DOES_NOT_EXIST_ERROR,propertyName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }


}