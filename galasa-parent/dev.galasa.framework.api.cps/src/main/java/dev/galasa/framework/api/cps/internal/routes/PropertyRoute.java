/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static dev.galasa.framework.api.cps.internal.verycommon.ServletErrorMessage.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.cps.internal.verycommon.InternalServletException;
import dev.galasa.framework.api.cps.internal.verycommon.QueryParameters;
import dev.galasa.framework.api.cps.internal.verycommon.ResponseBuilder;
import dev.galasa.framework.api.cps.internal.verycommon.ServletError;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;


/**
 * An abstract route used by all the Property-related routes.
 */
public class PropertyRoute extends CPSRoute {

    static final Gson gson = GalasaGsonBuilder.build();

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };


    public PropertyRoute(ResponseBuilder responseBuilder, IFramework framework ) {
		/* Regex to match endpoints: 
		*  -> /cps/
		*/
		super(responseBuilder, "/cps\\/?", framework);
	}

    protected IFramework getFramework() {
        return super.framework;
    }

    protected String getPropertyNameFromURL(String pathInfo){
        String[] namespace = pathInfo.split("/");
        return namespace[4];
    }
    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        String namespace = getNamespaceFromURL(pathInfo);
        String property = getPropertyNameFromURL(pathInfo);
        String namespaces = getProperty(namespace,property);
		return getResponseBuilder().buildResponse(response, "application/json", namespaces, HttpServletResponse.SC_OK); 
    }

    private String getProperty(String namespace, String propertyName) throws ConfigurationPropertyStoreException {
        Map<String, String> properties = getAllProperties(namespace);
        JsonArray propertyArray = new JsonArray();
        
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().toString() == propertyName){
                JsonObject cpsProp = new JsonObject();
                cpsProp.addProperty("name", entry.getKey());
                cpsProp.addProperty("value", getProtectedValue(entry.getValue(),namespace));
                propertyArray.add(cpsProp);
            }
        }
        return gson.toJson(propertyArray);
    }

}