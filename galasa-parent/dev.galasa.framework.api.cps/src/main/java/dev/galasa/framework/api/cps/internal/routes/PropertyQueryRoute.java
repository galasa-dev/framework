/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.cps.internal.verycommon.InternalServletException;
import dev.galasa.framework.api.cps.internal.verycommon.QueryParameters;
import dev.galasa.framework.api.cps.internal.verycommon.ResponseBuilder;
import dev.galasa.framework.api.cps.internal.verycommon.ServletError;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import static dev.galasa.framework.api.cps.internal.verycommon.ServletErrorMessage.*;

public class PropertyQueryRoute extends CPSRoute{

    private static final String path = "/cps/(.*)/properties([?]?|[^/])+$";

    public PropertyQueryRoute(ResponseBuilder responseBuilder, IFramework framework) {
        super(responseBuilder, path , framework);
    }

   
    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String properties = getNamespaceProperties(namespace, queryParams);
        return getResponseBuilder().buildResponse(response, "application/json", properties, HttpServletResponse.SC_OK); 
    }

    private String getNamespaceProperties(String namespace, QueryParameters queryParams) throws FrameworkException{
        String properties = "";
         try {
            nameValidator.assertNamespaceCharPatternIsValid(namespace);
            if (hiddenNameSpaces.contains(namespace)) {
            ServletError error = new ServletError(GAL5016_CPS_HIDDEN_NAMESPACE_ERROR, namespace);
			throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            String prefix = queryParams.getSingleString("prefix", null);
            String suffix = queryParams.getSingleString("suffix", null);
            properties = getProperties(namespace, prefix, suffix);
        }catch (FrameworkException f){
            if (f instanceof InternalServletException){
                throw f;
            }else{
                ServletError error = new ServletError(GAL5017_INVALID_NAMESPACE_ERROR,namespace);  
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
        }
        return properties;
    }
    
    
    private String getProperties(String namespace, String prefix, String suffix) throws ConfigurationPropertyStoreException {
        Map<String, String> properties = getAllProperties(namespace);
       
        if (prefix != null){
            properties = filterPropertiesByPrefix(properties,prefix);
        }
        if (suffix != null){
            properties = filterPropertiesBySuffix(properties,suffix);
        }
        
        JsonArray propertyArray = new JsonArray();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            JsonObject cpsProp = new JsonObject();
            cpsProp.addProperty("name", entry.getKey());
            cpsProp.addProperty("value", getProtectedValue(entry.getValue(),namespace));
            propertyArray.add(cpsProp);
        }
        return gson.toJson(propertyArray);
    }
    
    protected  Map<String, String> filterPropertiesByPrefix( Map<String, String> properties , String prefix){
        Map<String, String> filteredProperties = new HashMap<String,String>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().toString().startsWith(prefix)){
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

}
