/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import dev.galasa.framework.api.cps.internal.common.GalasaProperty;
import dev.galasa.framework.api.cps.internal.common.NamespaceType;
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

    private static final String REDACTED_PROPERTY_VALUE = "********";

    private static final Set<String> hiddenNamespaces = new HashSet<>();
    static {
        hiddenNamespaces.add("dss");
    }

    /**
     * Some namespaces are able to be set, but cannot be queried.
     *
     * When they are queried, the values are redacted
     */
    private static final Set<String> secureNamespaces = new HashSet<>();
    static {
        secureNamespaces.add("secure");
    }

    public CPSRoute(ResponseBuilder responseBuilder, String path , IFramework framework ) {
    super(responseBuilder, path);
    this.framework = framework;
    }

    protected boolean isHiddenNamespace(String namespace){
        return hiddenNamespaces.contains(namespace);
    }

    protected String getNamespaceType(String namespace){
        String type = NamespaceType.NORMAL.toString();
        if (CPSRoute.isSecureNamespace(namespace)){
            type= NamespaceType.SECURE.toString();
        }
        return type;
    }
    

    public static boolean isSecureNamespace(String namespace){
        return secureNamespaces.contains(namespace);
    }

    protected String getProtectedValue(String actualValue , String namespace) {
        String protectedValue ;
        if (secureNamespaces.contains(namespace)) {
            // The namespace is protected, write-only, so should not be readable.
            protectedValue = REDACTED_PROPERTY_VALUE;
        } else {
            protectedValue = actualValue ;
        }
        return protectedValue ;
    }

    protected IFramework getFramework() {
        return this.framework;
    }

    protected Map<String, String> getAllProperties(String namespace) throws ConfigurationPropertyStoreException {
        return framework.getConfigurationPropertyService(namespace).getAllProperties();
    }

    protected  boolean checkNamespaceExists(String namespace) throws ConfigurationPropertyStoreException, InternalServletException {
        boolean valid = false;
        try{
            if (getAllProperties(namespace).size() > 0){
                valid = true;
            }
        }catch (Exception e ){
            //Catch the Exception (namespace is invalid) to throw error in if 
        }  
        if (!valid){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return valid;
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

    /** 
     * Returns a boolean value of whether the property has been located in the given namespace.
     * Hidden namespaces will return a false value as they should not be accessed via the API endpoints
     * @param property
     * @return boolean
     * @throws FrameworkException
     */
    protected boolean checkGalasaPropertyExists (GalasaProperty property) throws FrameworkException{
        return retrieveSingleProperty(property.metadata.namespace, property.metadata.name) != null;
    }

    /**
     * Returns a single property from a given namespace.
     * If the namespace provided is hidden, does not exist or has no matching property, it returns null
     * If the namespace provided does not match any existing namepsaces an exception will be thrown
     * @param namespace
     * @param propertyName
     * @return Map.Entry of String, String
     * @throws FrameworkException
     */
    protected Map.Entry<String, String> retrieveSingleProperty(String namespace, String propertyName) throws  InternalServletException {
        if (isHiddenNamespace(namespace)){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        try{
            Map<String, String> properties = getAllProperties(namespace);
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey().toString();
                if (key.equals(namespace+"."+propertyName)){
                    // Return the key and the redacted value if the namespace is secure, otherwise return the key and value
                    return Map.entry(entry.getKey(),getProtectedValue(entry.getValue(),namespace));
                }
            }
        }catch (Exception e){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return null;
    }

    /**
     * Attempts to update or create a Galasa Property based on the boolean parameter
     * @param property The GalasaProperty to be actioned
     * @param updateProperty Boolean flag indicating if the action to be performed is an update
     * @throws FrameworkException
     */
    protected void setProperty(@NotNull GalasaProperty property, boolean updateProperty) throws FrameworkException {
        boolean propExists = checkGalasaPropertyExists(property);
        /*
         * Logic Table to Determine actions
         * Create Property - The property must not already Exist i.e. propExists == false, updateProperty == false
         * Update Property - The property must exist i.e. propExists == true, updateProperty == true
         * Therefore setting updateProperty to false will force a create property path,
         * whilst setting updateProperty to true will force an update property path
         */
        if (propExists == updateProperty){
            getFramework().getConfigurationPropertyService(property.metadata.namespace).setProperty(property.metadata.name, property.data.value);
        }else{
            if (propExists){
                ServletError error = new ServletError(GAL5018_PROPERTY_ALREADY_EXISTS_ERROR, property.metadata.name, property.metadata.namespace);  
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }else{
                ServletError error = new ServletError(GAL5017_PROPERTY_DOES_NOT_EXIST_ERROR, property.metadata.name);  
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
        }
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

    /**
     * Returns an GalasaProperty from the request body that should be encoded in UTF-8 format
     * @param request
     * @return GalasaProperty 
     * @throws IOException
     * @throws InternalServletException
     */
    protected GalasaProperty getPropertyFromRequestBody (HttpServletRequest request) throws IOException, InternalServletException{
        String body = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
        return getGalasaPropertyfromJsonString(body);
    }

    /**
     * This function casts a json String into a GalasaProperty so that it can be used by the framework.
     * The property Json Structure should match the GalasaProperty Structure, otherwise an exception will be thrown
     * 
     * @param jsonString
     * @return GalasaProperty
     * @throws InternalServletException
     */
    public GalasaProperty getGalasaPropertyfromJsonString (String jsonString) throws InternalServletException{
        GalasaProperty property = null;
        boolean valid = false;
        try {
            property = gson.fromJson(jsonString, GalasaProperty.class);
            valid = property.isValid();
        }catch (Exception e){}
        
        if(!valid){
            ServletError error = new ServletError(GAL5023_UNABLE_TO_CAST_TO_GALASAPROPERTY, jsonString);  
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return property;
    }

    protected String buildResponseBody(String namespace, Map<String, String> properties){
        /*
         * Builds a json array object from a Map of properties
         */
        JsonArray propertyArray = new JsonArray();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            GalasaProperty property = new GalasaProperty(entry.getKey(),getProtectedValue(entry.getValue(),namespace));
            propertyArray.add(property.toJSON());
        }
        return gson.toJson(propertyArray);
    }

    protected String buildResponseBody(String namespace, Map.Entry<String, String> entry){
        /*
         * Builds a json array object from a single Map.Entry containing a property
         */
        JsonArray propertyArray = new JsonArray();
        if (entry != null){
            JsonObject cpsProp = new JsonObject();
            cpsProp.addProperty("name", entry.getKey());
            cpsProp.addProperty("value", getProtectedValue(entry.getValue(),namespace));
            propertyArray.add(cpsProp);
        }
        return gson.toJson(propertyArray);
    }

    protected String buildResponseBody(GalasaProperty property){
        /*
         * Builds a json array object from a single GalasaProperty containing a property
         */
        JsonArray propertyArray = new JsonArray();
        if (property != null){
            propertyArray.add(property.toJSON());
        }
        return gson.toJson(propertyArray);
    }
}