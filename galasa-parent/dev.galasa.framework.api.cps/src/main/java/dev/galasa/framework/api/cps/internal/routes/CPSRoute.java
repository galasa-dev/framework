/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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
import dev.galasa.framework.api.common.resources.GalasaPropertyName;
import dev.galasa.framework.api.cps.internal.common.PropertyUtilities;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
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

    protected PropertyUtilities propertyUtility;



    public CPSRoute(ResponseBuilder responseBuilder, String path , IFramework framework) {
        super(responseBuilder, path);
        this.framework = framework;
        this.propertyUtility = new PropertyUtilities(framework);
    }

    protected IFramework getFramework() {
        return this.framework;
    }

    

    protected  boolean checkNamespaceExists(String namespaceName) throws ConfigurationPropertyStoreException, InternalServletException {
        boolean valid = false;
        CPSFacade cps = new CPSFacade(framework);
        CPSNamespace namespace = cps.getNamespace(namespaceName);
        try {
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
            propertyArray.add(property.toJSON());
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
            propertyArray.add(property.toJSON());
        }
        return gson.toJson(propertyArray);
    }
}