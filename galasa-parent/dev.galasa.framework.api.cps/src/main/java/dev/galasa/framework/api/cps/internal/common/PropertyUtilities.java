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

    public void setGalasaProperty (CPSProperty property, String action) throws FrameworkException{
        boolean updateProperty = false;
        if (property.isPropertyValid() && updateActions.contains(action)){
            if ((checkGalasaPropertyExists(property) || action.equals("update"))){
                updateProperty = true;
            }
        }
       // setProperty(property, updateProperty);
    }

    public boolean checkPropertyNamespaceMatchesURLNamespace(@NotNull CPSProperty property , @NotNull String namespace){
        return namespace.toLowerCase().trim().equals(property.getNamespace().toLowerCase().trim());

    }
}
