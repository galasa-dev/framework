/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dev.galasa.framework.api.cps.internal.common.Namespace;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.ResourceNameValidator;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

/**
 * An abstract route used by all the Run-related routes.
 */
public abstract class CPSRoute extends BaseRoute {

    static final ResourceNameValidator nameValidator = new ResourceNameValidator();
    static final Gson gson = GalasaGsonBuilder.build();

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };

    protected IFramework framework;

    public CPSRoute(ResponseBuilder responseBuilder, String path , IFramework framework ) {
        super(responseBuilder, path);
        this.framework = framework;
        }

    protected IFramework getFramework() {
        return this.framework;
    }

    protected Map<String, String> getAllProperties(String namespace) throws ConfigurationPropertyStoreException {
        return framework.getConfigurationPropertyService(namespace).getAllProperties();
    }

    /**
     * Returns a boolean value of whether the property has been located in the given namespace.
     * Hidden namespaces will return a false value as they should not be accessed via the API endpoints
     * 
     * @param namespace
     * @param propertyName
     * @return boolean
     * @throws FrameworkException
     */
    protected boolean checkPropertyExists (String namespace, String propertyName) throws FrameworkException{
        return retrieveSingleProperty(namespace, propertyName) != null;
    }

    protected boolean checkRequestHasContent(HttpServletRequest request) throws InternalServletException{
        boolean valid = false;
        try{
            if (request.getContentLength() >0){
                valid = true;
            }
        }catch (NullPointerException e ){
            //Catch the NullPointerException (empty request body) to throw error in if 
        }  
        return valid;
    }

    /**
     * Returns a single property from a given namespace.
     * If the namespace provided is hidden, does not exist or has no matching property, it returns null
     * If the namespace provided does not match any existing namepsaces an exception will be thrown
     * @param namespaceName
     * @param propertyName
     * @return Map.Entry of String, String 
     * @throws ConfigurationPropertyStoreException
     * @throws FrameworkException
     */
    protected Map.Entry<String, String> retrieveSingleProperty(String namespaceName, String propertyName) throws  InternalServletException, ConfigurationPropertyStoreException {
        Namespace namespace = new Namespace(namespaceName);
        if (namespace.isHiddenNamespace()){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        try{
            Map<String, String> properties = getAllProperties(namespaceName);
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey().toString();
                if (key.equals(namespaceName+"."+propertyName)){
                    return entry;
                }
            }
        }catch (Exception e){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return null;
    }

    protected String getPropertyNameFromURL(String pathInfo) throws InternalServletException{
        /*
         * This expects a pathInfo from the cps property endpoint, i.e
         * /cps/<namespace>/properties/<propertyName>
         * This means that the minimum length is going to be 4
         * meaning we should not have an IndexOutOfBoundsException 
         */
        try {
            String[] namespace = pathInfo.split("/");
            return namespace[3];
        } catch (Exception e){
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
    }
    }

    protected String getNamespaceFromURL(String pathInfo) throws InternalServletException{
        /*
         * This expects a pathInfo from the cps endpoints, i.e.
         * /cps/<namespace>
         * /cps/<namespace>/properties
         * /cps/<namespace>/properties?prefix=<prefix>&suffix=<suffix>
         * /cps/<namespace>/properties/<propertyName>
         * This means that the minimum length is going to be 2
         * meaning we should not have an IndexOutOfBoundsException 
         */
        try {
            String[] namespace = pathInfo.split("/");
            return namespace[1];
        } catch (Exception e){
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected String buildResponseBody(Namespace namespace, Map<String, String> properties){
        /*
         * Builds a json array object from a Map of properties
         */
        JsonArray propertyArray = new JsonArray();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            JsonObject cpsProp = new JsonObject();
            cpsProp.addProperty("name", entry.getKey());
            cpsProp.addProperty("value", namespace.getProtectedValue(entry.getValue()));
            propertyArray.add(cpsProp);
        }
        return gson.toJson(propertyArray);
    }

    protected String buildResponseBody(Namespace namespace, Map.Entry<String, String> entry){
        /*
         * Builds a json array object from a single Map.Entry containing a property
         */
        JsonArray propertyArray = new JsonArray();
        if (entry != null){
            JsonObject cpsProp = new JsonObject();
            cpsProp.addProperty("name", entry.getKey());
            cpsProp.addProperty("value", namespace.getProtectedValue(entry.getValue()));
            propertyArray.add(cpsProp);
        }
        return gson.toJson(propertyArray);
    }

}