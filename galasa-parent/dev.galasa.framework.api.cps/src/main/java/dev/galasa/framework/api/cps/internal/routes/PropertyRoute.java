/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.GalasaPropertyName;
import dev.galasa.framework.api.cps.internal.common.PropertyComparator;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class PropertyRoute extends CPSRoute{

    private static final String path = "\\/([a-z0-9]+)/properties([?]?|[^/])+$";

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
            CPSFacade cps = new CPSFacade(framework);
            CPSNamespace namespace = cps.getNamespace(namespaceName);
            if (namespace.isHidden()) {
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
    
    
    private String getProperties(CPSNamespace namespace, String prefix, String suffix, List<String> infixes) throws ConfigurationPropertyStoreException {
        Map<GalasaPropertyName, CPSProperty> properties = namespace.getProperties();
        
        if (prefix != null){
            properties = filterPropertiesByPrefix(namespace.getName(), properties,prefix);
        }
        if (suffix != null){
            properties = filterPropertiesBySuffix(properties,suffix);
        }
        if (infixes != null){
            properties = filterPropertiesByInfix(properties, infixes);
        }
        Map<GalasaPropertyName, CPSProperty> sortedProperties = sortResults(properties);
        return buildResponseBody(sortedProperties);
    }
    
    /**
     * Sort the properties provided by key 
     * @param properties
     * @return Sorted Map of properties
     */
    protected Map<GalasaPropertyName, CPSProperty> sortResults(Map<GalasaPropertyName, CPSProperty> properties){
        Collection<GalasaPropertyName> unsortedKeys = properties.keySet();
        PropertyComparator comparator = new PropertyComparator();
        Map<GalasaPropertyName, CPSProperty> sorted = new TreeMap<GalasaPropertyName, CPSProperty>(comparator);

        for( GalasaPropertyName key : unsortedKeys ) {
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
    protected  Map<GalasaPropertyName, CPSProperty> filterPropertiesByPrefix(String namespace, Map<GalasaPropertyName, CPSProperty> properties , String prefix){
        Map<GalasaPropertyName, CPSProperty> filteredProperties = new HashMap<GalasaPropertyName, CPSProperty>();
        for (Map.Entry<GalasaPropertyName, CPSProperty> entry : properties.entrySet()) {
            if (entry.getKey().getFullyQualifiedName().startsWith(namespace + "."+prefix)){
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
    protected  Map<GalasaPropertyName, CPSProperty> filterPropertiesBySuffix( Map<GalasaPropertyName, CPSProperty> properties , String suffix){
       Map<GalasaPropertyName, CPSProperty> filteredProperties = new HashMap<GalasaPropertyName, CPSProperty>();
        for (Map.Entry<GalasaPropertyName, CPSProperty> entry : properties.entrySet()) {
            if (entry.getKey().getFullyQualifiedName().endsWith(suffix)){
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
    protected  Map<GalasaPropertyName, CPSProperty> filterPropertiesByInfix(Map<GalasaPropertyName, CPSProperty>properties, List<String> infixes){
        Map<GalasaPropertyName, CPSProperty> filteredProperties = new HashMap<GalasaPropertyName, CPSProperty>();
        for (Map.Entry<GalasaPropertyName, CPSProperty> entry : properties.entrySet()) {
            GalasaPropertyName key = entry.getKey();
            for (String infix : infixes){
				if (key.getFullyQualifiedName().contains(infix)&& !filteredProperties.containsKey(key)){
                    filteredProperties.put(entry.getKey(), entry.getValue());
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
        checkRequestHasContent(request);
        CPSProperty property = propertyUtility.getPropertyFromRequestBody(request);
        checkNamespaceExists(namespace);
        if(!propertyUtility.checkPropertyNamespaceMatchesURLNamespace(property, namespace)){
            ServletError error = new ServletError(GAL5028_PROPERTY_NAMESPACE_DOES_NOT_MATCH_ERROR,property.metadata.namespace, namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        propertyUtility.setProperty(property, false);
        String responseBody = String.format("Successfully created property %s in %s",property.metadata.name, namespace);
        return getResponseBuilder().buildResponse(response, "text/plain", responseBody, HttpServletResponse.SC_CREATED); 
    }

}
