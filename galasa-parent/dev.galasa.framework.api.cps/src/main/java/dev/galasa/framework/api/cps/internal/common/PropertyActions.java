/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class PropertyActions {

    private static final Set<String> validActions = Set.of("apply","update");
    static final Gson gson = GalasaGsonBuilder.build();
    IFramework framework;

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

    public PropertyActions(IFramework framework){
        this.framework = framework;
    }

    private IFramework getFramework(){
        return this.framework;
    }


    public boolean isPropertyValid(GalasaProperty property) throws InternalServletException {
        if (!property.isValid()){
            ServletError error = null;
            if (!property.metadata.name.isEmpty()){
                error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"name",property.metadata.name);
            }
            if (!property.metadata.namespace.isEmpty()){
                error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"namespace",property.metadata.namespace);
            }
            if (!property.data.value.isEmpty()){
                error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"value",property.data.value);
            };
            if (!property.apiVersion.isEmpty()){
                error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"apiVersion",property.apiVersion);
            };
            if (error != null){
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        return true;
    }

    public boolean isHiddenNamespace(String namespace){
        return hiddenNamespaces.contains(namespace);
    }

    public String getNamespaceType(String namespace){
        String type = NamespaceType.NORMAL.toString();
        if (PropertyActions.isSecureNamespace(namespace)){
            type= NamespaceType.SECURE.toString();
        }
        return type;
    }
    
    public static boolean isSecureNamespace(String namespace){
        return secureNamespaces.contains(namespace);
    }

    public Map<String, String> getAllProperties(String namespace) throws ConfigurationPropertyStoreException {
        return framework.getConfigurationPropertyService(namespace).getAllProperties();
    }

    public String getProtectedValue(String actualValue , String namespace) {
        String protectedValue ;
        if (secureNamespaces.contains(namespace)) {
            // The namespace is protected, write-only, so should not be readable.
            protectedValue = REDACTED_PROPERTY_VALUE;
        } else {
            protectedValue = actualValue ;
        }
        return protectedValue ;
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
    public boolean checkPropertyExists (String namespace, String propertyName) throws FrameworkException{
        return retrieveSingleProperty(namespace, propertyName) != null;
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
    public Map.Entry<String, String> retrieveSingleProperty(String namespace, String propertyName) throws  InternalServletException {
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
     * Returns a boolean value of whether the property has been located in the given namespace.
     * Hidden namespaces will return a false value as they should not be accessed via the API endpoints
     * @param property
     * @return boolean
     * @throws FrameworkException
     */
    protected boolean checkGalasaPropertyExists (GalasaProperty property) throws InternalServletException{
        return retrieveSingleProperty(property.metadata.namespace, property.metadata.name) != null;
    }


    public void setGalasaProperty (GalasaProperty property, String action) throws FrameworkException{
        boolean updateProperty = false;
            if (property.isValid() && validActions.contains(action)&& checkGalasaPropertyExists(property)){
                updateProperty = true;
            }
            setProperty(property, updateProperty);
    }

    /**
     * Attempts to update or create a Galasa Property based on the boolean parameter
     * @param property The GalasaProperty to be actioned
     * @param updateProperty Boolean flag indicating if the action to be performed is an update
     * @throws FrameworkException
     */
    public void setProperty(@NotNull GalasaProperty property, boolean updateProperty) throws FrameworkException, InternalServletException {
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

     /**
     * Returns an GalasaProperty from the request body that should be encoded in UTF-8 format
     * @param request
     * @return GalasaProperty 
     * @throws IOException
     * @throws InternalServletException
     */
    public GalasaProperty getPropertyFromRequestBody (HttpServletRequest request) throws IOException, InternalServletException{
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
    
}
