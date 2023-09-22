/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.ResourceNameValidator;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
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
    private static final Set<String> writeOnlyNamespaces = new HashSet<>();
    static {
        writeOnlyNamespaces.add("secure");
    }

    public CPSRoute(ResponseBuilder responseBuilder, String path , IFramework framework ) {
    super(responseBuilder, path);
    this.framework = framework;
    }

    protected boolean isHiddenNamespace(String namespace){
        return hiddenNamespaces.contains(namespace);
    }

    protected String getProtectedValue(String actualValue , String namespace) {
        String protectedValue ;
        if (writeOnlyNamespaces.contains(namespace)) {
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

    protected String getNamespaceFromURL(String pathInfo) throws InternalServletException{
        /*
         * This expects a pathInfo from the cps endpoints, i.e.
         * /cps/<namespace>
         * /cps/<namespace>/properties
         * /cps/<namespace>/properties?prefix=<prefix>&suffix=<suffix>
         * /cps/<namespace>/properties/<propertyName>
         * This means that the minimum length is going to be 3 as without the url matches above
         * meaning we should not have an IndexOutOfBoundsException 
         */
        try {
            String[] namespace = pathInfo.split("/");
            return namespace[2];
        } catch (Exception e){
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected String buildResponseBody(String namespace, Map<String, String> properties){
        /*
         * Builds a json array object from a Map of properties
         */
        JsonArray propertyArray = new JsonArray();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            JsonObject cpsProp = new JsonObject();
            cpsProp.addProperty("name", entry.getKey());
            cpsProp.addProperty("value", getProtectedValue(entry.getValue(),namespace));
            propertyArray.add(cpsProp);
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

}