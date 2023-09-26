/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class PropertyRoute extends CPSRoute{

    private static final String path = "/cps/([a-zA-Z0-9]+)/properties([?]?|[^/])+$";

    public PropertyRoute(ResponseBuilder responseBuilder, IFramework framework) {
        super(responseBuilder, path , framework);
    }

    /*
     * Property Query
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,HttpServletRequest req, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String properties = getNamespaceProperties(namespace, queryParams);
        return getResponseBuilder().buildResponse(response, "application/json", properties, HttpServletResponse.SC_OK); 
    }

    private String getNamespaceProperties(String namespace, QueryParameters queryParams) throws FrameworkException{
        String properties = "";
         try {
            nameValidator.assertNamespaceCharPatternIsValid(namespace);
            if (super.isHiddenNamespace(namespace)) {
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR, namespace);
			throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            String prefix = queryParams.getSingleString("prefix", null);
            String suffix = queryParams.getSingleString("suffix", null);
            properties = getProperties(namespace, prefix, suffix);
        }catch (FrameworkException f){
                ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespace);  
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return properties;
    }
    
    
    private String getProperties(String namespace, String prefix, String suffix) throws ConfigurationPropertyStoreException {
        Map<String, String> properties = getAllProperties(namespace);
       
        if (prefix != null){
            properties = filterPropertiesByPrefix(namespace, properties,prefix);
        }
        if (suffix != null){
            properties = filterPropertiesBySuffix(properties,suffix);
        }
        
        return buildResponseBody(namespace, properties);
    }
    
    protected  Map<String, String> filterPropertiesByPrefix(String namespace, Map<String, String> properties , String prefix){
        Map<String, String> filteredProperties = new HashMap<String,String>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().toString().startsWith(namespace + "."+prefix)){
                filteredProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredProperties;
    }

    protected  Map<String, String> filterPropertiesBySuffix( Map<String, String> properties , String suffix){
        Map<String, String> filteredProperties = new HashMap<String,String>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().toString().endsWith(suffix)){
                filteredProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredProperties;
    }

    /*
     * Property Create
     */
    @Override
    public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response)
            throws  IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        if (!checkRequestHasContent(request)){
            ServletError error = new ServletError(GAL5411_NO_REQUEST_BODY,pathInfo);  
            throw new InternalServletException(error, HttpServletResponse.SC_LENGTH_REQUIRED);
        }
        Map.Entry<String,String> property = getPropertyFromRequestBody(request);
        setProperty(namespace, property );
        String responseBody = String.format("Successfully created property %s in %s",property.getKey(), namespace);
        return getResponseBuilder().buildResponse(response, "application/json", responseBody, HttpServletResponse.SC_CREATED); 
    }

    /**
     * Returns an entry of <propertyName, propertyValue> from the request body that should be encoded in UTF-8 format
     * @param request
     * @return Map.Entry<String,String> 
     * @throws IOException
     */
    private Map.Entry<String,String> getPropertyFromRequestBody (HttpServletRequest request) throws IOException{
        String body = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(body);
        String propertyName = jsonElement.getAsJsonObject().get("name").getAsString();
        String propertyValue = jsonElement.getAsJsonObject().get("value").getAsString();
        return Map.entry(propertyName,propertyValue);
    }

    private void setProperty(String namespace, Map.Entry<String,String> property) throws FrameworkException {

        if (!checkPropertyExists(namespace, property.getKey())){
            getFramework().getConfigurationPropertyService(namespace).setProperty(property.getKey(), property.getValue());
        }else{
            ServletError error = new ServletError(GAL5018_PROPERTY_ALREADY_EXISTS_ERROR, property.getKey() ,namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
