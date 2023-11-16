/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.google.gson.Gson;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.GalasaProperty;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class PropertyUtilities {

    private static final Set<String> updateActions = Set.of("apply","update");

    static final Gson gson = GalasaGsonBuilder.build();
    
    IFramework framework;
    CPSFacade cps;

    public PropertyUtilities(IFramework framework){
        this.framework = framework;
    }

    private IFramework getFramework(){
        return this.framework;
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
     * Returns a boolean value of whether the property has been located in the given namespace.
     * Hidden namespaces will return a false value as they should not be accessed via the API endpoints
     * @param property
     * @return boolean
     * @throws FrameworkException
     */
    protected boolean checkGalasaPropertyExists (CPSProperty property) throws InternalServletException{
        return checkPropertyExists(property.getNamespace(), property.getName());
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

    /**
     * Attempts to update or create a Galasa Property based on the boolean parameter
     * @param property The GalasaProperty to be actioned
     * @param updateProperty Boolean flag indicating if the action to be performed is an update
     * @throws FrameworkException
     */
    public void setProperty(@NotNull CPSProperty property, boolean updateProperty) throws FrameworkException, InternalServletException {
        boolean propExists = checkGalasaPropertyExists(property);
        /*
         * Logic Table to Determine actions
         * Create Property - The property must not already Exist i.e. propExists == false, updateProperty == false
         * Update Property - The property must exist i.e. propExists == true, updateProperty == true
         * Therefore setting updateProperty to false will force a create property path,
         * whilst setting updateProperty to true will force an update property path
         */
        if (propExists == updateProperty){
            getFramework().getConfigurationPropertyService(property.getNamespace()).setProperty(property.getNamespace()+"."+property.getName(), property.getValue());
        }else if (propExists){
            ServletError error = new ServletError(GAL5018_PROPERTY_ALREADY_EXISTS_ERROR, property.getName(), property.getNamespace());  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }else{
            ServletError error = new ServletError(GAL5017_PROPERTY_DOES_NOT_EXIST_ERROR, property.getName());  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public void setGalasaProperty (CPSProperty property, String action) throws FrameworkException{
        boolean updateProperty = false;
        if (property.isPropertyValid() && updateActions.contains(action)){
            if ((checkGalasaPropertyExists(property) || action.equals("update"))){
                updateProperty = true;
            }
        }
        setProperty(property, updateProperty);
    }

    public boolean checkPropertyNamespaceMatchesURLNamespace(@NotNull CPSProperty property , @NotNull String namespace){
        return namespace.toLowerCase().trim().equals(property.getNamespace().toLowerCase().trim());

    }
     /**
     * Returns an GalasaProperty from the request body that should be encoded in UTF-8 format
     * @param request
     * @return GalasaProperty 
     * @throws IOException
     * @throws InternalServletException
     */
    public CPSProperty getPropertyFromRequestBody (HttpServletRequest request) throws IOException, InternalServletException{
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
    public CPSProperty getGalasaPropertyfromJsonString (String jsonString) throws InternalServletException{
        CPSProperty property = null;
        boolean valid = false;
        try {
            GalasaProperty jsonproperty = gson.fromJson(jsonString, GalasaProperty.class);
            valid = jsonproperty.isPropertyValid();
            property = new CPSProperty(jsonproperty.getNamespace()+"."+jsonproperty.getName(),jsonproperty.getValue());
        }catch (Exception e){}
        
        if(!valid){
            ServletError error = new ServletError(GAL5023_UNABLE_TO_CAST_TO_GALASAPROPERTY, jsonString);  
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return property;
    }
    
}
