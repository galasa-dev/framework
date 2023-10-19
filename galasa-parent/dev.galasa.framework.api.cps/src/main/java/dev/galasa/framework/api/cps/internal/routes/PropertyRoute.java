/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.cps.internal.common.Namespace;
import dev.galasa.framework.api.cps.internal.common.PropertyComparator;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class PropertyRoute extends CPSRoute{

    private static final String path = "\\/([a-zA-Z0-9]+)/properties([?]?|[^/])+$";

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

    private String getNamespaceProperties(String namespaceName, QueryParameters queryParams) throws FrameworkException{
        String properties = "";
         try {
            nameValidator.assertNamespaceCharPatternIsValid(namespaceName);
            Namespace namespace = new Namespace(namespaceName);
            if (isHiddenNamespace(namespaceName)) {
                ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR, namespaceName);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            String prefix = queryParams.getSingleString("prefix", null);
            String suffix = queryParams.getSingleString("suffix", null);
            List<String> infixes = queryParams.getMultipleString("infix", null);
            properties = getProperties(namespace, prefix, suffix, infixes);
        }catch (FrameworkException f){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return properties;
    }
    
    
    private String getProperties(Namespace namespace, String prefix, String suffix, List<String> infixes) throws ConfigurationPropertyStoreException {
        Map<String, String> properties = getAllProperties(namespace.getName());
       
        if (prefix != null){
            properties = filterPropertiesByPrefix(namespace.getName(), properties,prefix);
        }
        if (suffix != null){
            properties = filterPropertiesBySuffix(properties,suffix);
        }
        if (infixes != null){
            properties = filterPropertiesByInfix(properties, infixes);
        }
        Map<String, String> sortedProperties = sortResults(properties);
        return buildResponseBody(namespace.getName(), sortedProperties);
    }
    
    /**
     * Sort the properties provided by key 
     * @param properties
     * @return Sorted Map of properties
     */
    protected Map<String, String> sortResults(Map<String, String> properties){
        Collection<String> unsortedKeys = properties.keySet();
        PropertyComparator comparator = new PropertyComparator();
        Map<String,String> sorted = new TreeMap<String,String>(comparator);

        for( String key : unsortedKeys ) {
            sorted.put(key, properties.get(key));
        }

        return sorted;
    }

    /**
     * Filter a map of provided properties by checking that the properties start with namespace.prefix 
     * using the supplied paramenters
     * @param namespace
     * @param properties
     * @param prefix
     * @return Map of Properties starting with the provided prefix
     */
    protected  Map<String, String> filterPropertiesByPrefix(String namespace, Map<String, String> properties , String prefix){
        Map<String, String> filteredProperties = new HashMap<String,String>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().toString().startsWith(namespace + "."+prefix)){
                filteredProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredProperties;
    }

    /**
     * Filter a map of provided properties by checking that the properties end with the supplied prefix
     * @param properties
     * @param suffix
     * @return Map of Properties ending with the provided suffix
     */
    protected  Map<String, String> filterPropertiesBySuffix( Map<String, String> properties , String suffix){
        Map<String, String> filteredProperties = new HashMap<String,String>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().toString().endsWith(suffix)){
                filteredProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredProperties;
    }

    /**
     * Filter a map of provided properties by checking that the properties contain and match at least of the
     * supplied infixes 
     * @param properties
     * @param infixes
     * @return Map of Properties containing the at least one of the infixes
     */
    protected  Map<String, String> filterPropertiesByInfix(Map<String, String> properties, List<String> infixes){
        Map<String, String> filteredProperties = new HashMap<String,String>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            for (String infix : infixes){
				if (key.contains(infix)&& !filteredProperties.containsKey(key)){
                    filteredProperties.put(key, entry.getValue());
	            }
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
        return getResponseBuilder().buildResponse(response, "text/plain", responseBody, HttpServletResponse.SC_CREATED); 
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
