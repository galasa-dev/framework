/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.ResourceNameValidator;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.GalasaProperty;
import dev.galasa.framework.api.common.resources.GalasaPropertyName;
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
    CPSFacade cps;

    public CPSRoute(ResponseBuilder responseBuilder, String path , IFramework framework) {
        super(responseBuilder, path);
        this.framework = framework;
    }

    protected IFramework getFramework() {
        return this.framework;
    }

    protected  boolean checkPropertyNamespaceMatchesURLNamespace(@NotNull CPSProperty property , @NotNull String namespace){
        return namespace.toLowerCase().trim().equals(property.getNamespace().toLowerCase().trim());
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
    public boolean checkPropertyExists (String namespace, String propertyName) throws InternalServletException{
        return retrieveSingleProperty(namespace, propertyName) != null;
    }

    /**
     * Returns a single property from a given namespace.
     * If the namespace provided is hidden, does not exist or has no matching property, it returns null
     * If the namespace provided does not match any existing namepsaces an exception will be thrown
     * @param namespaceName
     * @param propertyName
     * @return Map.Entry of String, String
     * @throws InternalServletException
     */
    public CPSProperty retrieveSingleProperty(String namespaceName, String propertyName) throws  InternalServletException {
        CPSProperty property;
        try {
            cps = new CPSFacade(this.framework);
        } catch( ConfigurationPropertyStoreException ex ) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,ex);
        }

        CPSNamespace namespace ;
        try {
            namespace = cps.getNamespace(namespaceName);
        }catch (Exception e){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND,e);
        }
        
        if (namespace.isHidden()){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        } 

        
        try{
            property = namespace.getProperty(propertyName);
        }catch (Exception e){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND,e);
        }
        
        return property;
    }

    protected CPSProperty applyPropertyToStore (HttpServletRequest request, String namespaceName , boolean isUpdateAction) throws IOException, FrameworkException{
        GalasaProperty galasaProperty = GalasaProperty.getPropertyFromRequestBody(request);
        checkNamespaceExists(namespaceName);
        CPSFacade cps = new CPSFacade(framework);
        CPSNamespace namespace = cps.getNamespace(galasaProperty.getNamespace());
        CPSProperty property = namespace.getPropertyFromStore(galasaProperty.getName());
        if(!checkPropertyNamespaceMatchesURLNamespace(property, namespaceName)){
            ServletError error = new ServletError(GAL5028_PROPERTY_NAMESPACE_DOES_NOT_MATCH_ERROR,property.getNamespace(), namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        property.setPropertyToStore(galasaProperty, isUpdateAction);
        return property;
    }

    protected  boolean checkNamespaceExists(String namespaceName) throws ConfigurationPropertyStoreException, InternalServletException {
        boolean valid = false;
        try {
            CPSFacade cps = new CPSFacade(framework);
            CPSNamespace namespace = cps.getNamespace(namespaceName);
            if (namespace.getProperties().size() > 0) {
                valid = true;
            }
        } catch (Exception e ) {
            //Catch the Exception (namespace is invalid) to throw error in if 
        }  
        if (!valid) {
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return valid;
    }

    protected String getPropertyNameFromURL(String pathInfo) throws InternalServletException {
        /*
         * This expects a pathInfo from the cps property endpoint, i.e
         * /cps/<namespace>/properties/<propertyName>
         * This means that the minimum length is going to be 4
         * meaning we should not have an IndexOutOfBoundsException 
         */
        try {
            String[] namespace = pathInfo.split("/");
            return namespace[3];
        } catch (Exception e) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected String getNamespaceFromURL(String pathInfo) throws InternalServletException {
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
        } catch (Exception e) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected String buildResponseBody(String namespace, Map<String, String> properties) {
        /*
         * Builds a json array object from a Map of properties
         */
        JsonArray propertyArray = new JsonArray();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            CPSProperty property = new CPSProperty(entry.getKey(),entry.getValue());
            propertyArray.add(property.toJSON());
        }
        return gson.toJson(propertyArray);
    }

    protected String buildResponseBody(Map<GalasaPropertyName, CPSProperty> properties) {
        /*
         * Builds a json array object from a Map of properties
         */
        JsonArray propertyArray = new JsonArray();
        for (Map.Entry<GalasaPropertyName, CPSProperty> entry : properties.entrySet()) {
            CPSProperty property = entry.getValue();
            propertyArray.add(new GalasaProperty(property).toJSON());
        }
        return gson.toJson(propertyArray);
    }

    protected String buildResponseBody(String namespace, Map.Entry<String, String> entry) {
        /*
         * Builds a json array object from a single Map.Entry containing a property
         */
        JsonArray propertyArray = new JsonArray();
        if (entry != null) {
            JsonObject cpsProp = new JsonObject();
            cpsProp.addProperty("name", entry.getKey());
            cpsProp.addProperty("value", entry.getValue());
            propertyArray.add(cpsProp);
        }
        return gson.toJson(propertyArray);
    }

    protected String buildResponseBody(CPSProperty property) {
        /*
         * Builds a json array object from a single GalasaProperty containing a property
         */
        JsonArray propertyArray = new JsonArray();
        if (property != null){
            propertyArray.add(new GalasaProperty(property).toJSON());
        }
        return gson.toJson(propertyArray);
    }
}