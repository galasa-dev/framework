/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.GalasaResourceType.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.GalasaResourceType;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.api.common.resources.ResourceNameValidator;
import dev.galasa.framework.api.resources.processors.GalasaPropertyProcessor;
import dev.galasa.framework.api.resources.processors.GalasaSecretProcessor;
import dev.galasa.framework.api.resources.processors.IGalasaResourceProcessor;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.utils.GalasaGson;

public class ResourcesRoute  extends BaseRoute{

    static final GalasaGson gson = new GalasaGson();

    static final ResourceNameValidator nameValidator = new ResourceNameValidator();

    protected static final String path = "\\/";

    private Map<GalasaResourceType, IGalasaResourceProcessor> resourceProcessors = new HashMap<>();
    
    protected List<String> errors = new ArrayList<String>();

    public ResourcesRoute(ResponseBuilder responseBuilder, CPSFacade cps, ICredentialsService credentialsService) {
        super(responseBuilder, path);

        resourceProcessors.put(GALASA_PROPERTY, new GalasaPropertyProcessor(cps));
        resourceProcessors.put(GALASA_SECRET, new GalasaSecretProcessor(credentialsService));
    }

    @Override
     public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters, 
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {  
        logger.info("ResourcesRoute - handlePostRequest() entered");

        JsonObject jsonBody = parseRequestBody(request, JsonObject.class);
        List<String> errorsList = processRequest(jsonBody);
        if (errorsList.size() >0){
            response = getResponseBuilder().buildResponse(request, response, "application/json", getErrorsAsJson(errorsList), HttpServletResponse.SC_BAD_REQUEST);
        } else {
            response = getResponseBuilder().buildResponse(request, response, "application/json", "", HttpServletResponse.SC_OK);
        }
        errors.clear();

        logger.info("ResourcesRoute - handlePostRequest() exiting");
        return response;

    }

    protected List<String> processRequest(JsonObject body) throws InternalServletException{
        String actionStr = body.get("action").getAsString().toLowerCase().trim();
        ResourceAction action = ResourceAction.getFromString(actionStr);
        if (action != null){
            JsonArray jsonArray = body.get("data").getAsJsonArray();
            processDataArray(jsonArray, action);
        } else {
            ServletError error = new ServletError(GAL5025_UNSUPPORTED_ACTION);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return errors;
    }


    /**
     * Convert the List of Error Strings into JSON Objects 
     * and add them to a JSON array to be sent in the response to the client
     * 
     * @param errorsList List of Errors to be converted to JSON objects
     * @return String containing the JSON Array of Errors
     */
    protected String getErrorsAsJson(List<String> errorsList){
        JsonArray json = new JsonArray();
        for (String error : errorsList){
            json.add( gson.fromJson(error, JsonObject.class));
        }
        return gson.toJson(json);
    }

    protected void processDataArray(JsonArray jsonArray, ResourceAction action) throws InternalServletException{
        for (JsonElement element: jsonArray) {
            try {
                checkJsonElementIsValidJSON(element);
                JsonObject resource = element.getAsJsonObject();
                String kindStr = resource.get("kind").getAsString();

                GalasaResourceType kind = GalasaResourceType.getFromString(kindStr);
                if (kind == null) {
                    ServletError error = new ServletError(GAL5026_UNSUPPORTED_RESOURCE_TYPE);
                    throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
                }

                errors.addAll(resourceProcessors.get(kind).processResource(resource, action));

            } catch (InternalServletException s) {
                errors.add(s.getMessage());
            }
        }
    }
}
