/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.cps.internal.verycommon.InternalServletException;
import dev.galasa.framework.api.cps.internal.verycommon.QueryParameters;
import dev.galasa.framework.api.cps.internal.verycommon.ResponseBuilder;
import dev.galasa.framework.api.cps.internal.verycommon.ServletError;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import static dev.galasa.framework.api.cps.internal.verycommon.ServletErrorMessage.*;

public class PropertyQueryRoute extends CPSRoute{

    private static final String path = "/cps/([a-zA-z0-9.]*)([?]?|[^/])+$";

    public PropertyQueryRoute(ResponseBuilder responseBuilder, IFramework framework) {
        super(responseBuilder, path , framework);
    }

    private String getNamespaceFromURL(String pathInfo){
        String[] namespace = pathInfo.split("/");
        return namespace[2];
    }
    
    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        String properties;
        String namespace = getNamespaceFromURL(pathInfo);
        try {
            nameValidator.assertNamespaceCharPatternIsValid(namespace);
        }catch (FrameworkException f){
            ServletError error = new ServletError(GAL5017_INVALID_NAMESPACE_ERROR,namespace);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        properties = getNamespaceProperties(namespace);
        return getResponseBuilder().buildResponse(response, "application/json", properties, HttpServletResponse.SC_OK); 
    }
    
    private String getNamespaceProperties(String namespace)
            throws IOException, ConfigurationPropertyStoreException, InternalServletException {

        if (hiddenNameSpaces.contains(namespace)) {
            ServletError error = new ServletError(GAL5016_CPS_HIDDEN_NAMESPACE_ERROR, namespace);
			throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }

        JsonArray propertyArray = new JsonArray();
        Map<String, String> properties = framework.getConfigurationPropertyService(namespace).getAllProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            JsonObject cpsProp = new JsonObject();
            cpsProp.addProperty("name", entry.getKey());
            cpsProp.addProperty("value", getProtectedValue(entry.getValue(),namespace));
            propertyArray.add(cpsProp);
        }
        return gson.toJson(propertyArray);
    }
}
