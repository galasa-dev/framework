/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.GalasaProperty;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.ResourceNameValidator;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGson;

public class ResourcesRoute  extends BaseRoute{

    static final GalasaGson gson = new GalasaGson();

    static final ResourceNameValidator nameValidator = new ResourceNameValidator();

    protected static final String path = "\\/";
    private static final Set<String> validActions = Collections.unmodifiableSet(Set.of("apply","create","update", "delete"));
    private static final Set<String> updateActions = Collections.unmodifiableSet(Set.of("apply","update"));
    
    protected List<String> errors = new ArrayList<String>();

    private CPSFacade cps;

    public ResourcesRoute(ResponseBuilder responseBuilder, IFramework framework) throws ConfigurationPropertyStoreException {
        super(responseBuilder, path);
        this.cps = new CPSFacade(framework);
    }

    @Override
     public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters, 
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {  
        logger.info("ResourcesRoute - handlePostRequest() entered");
        checkRequestHasContent(request);

        ServletInputStream body = request.getInputStream();
        String jsonBody = new String (body.readAllBytes(),StandardCharsets.UTF_8);
        body.close();

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

    protected List<String> processRequest(String jsonBody) throws InternalServletException{
        JsonObject body = gson.fromJson(jsonBody, JsonObject.class);
        String action = body.get("action").getAsString().toLowerCase().trim();
        if (validActions.contains(action)){
            JsonArray jsonArray = body.get("data").getAsJsonArray();
            processDataArray(jsonArray, action);
        } else {
            ServletError error = new ServletError(GAL5025_UNSUPPORTED_ACTION, action);
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

    protected void processDataArray(JsonArray jsonArray, String action) throws InternalServletException{
        for (JsonElement element: jsonArray){
            try {
                checkJsonElementIsValidJSON(element);
                JsonObject resource = element.getAsJsonObject();
                String kind = resource.get("kind").getAsString();
                switch (kind){
                    case "GalasaProperty":
                        processGalasaProperty(resource,action);
                        break;
                    default:
                        ServletError error = new ServletError(GAL5026_UNSUPPORTED_RESOURCE_TYPE,kind);
                        throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
                }
            } catch(InternalServletException s){
                errors.add(s.getMessage());
            }
        }
    }
    
    private boolean checkGalasaPropertyJsonStructure(JsonObject propertyJson) throws InternalServletException{
        List<String> validationErrors = new ArrayList<String>();
        if (propertyJson.has("apiVersion")&& propertyJson.has("metadata")&&propertyJson.has("data")){
            //Check metadata is not null and contains name and namespace fields in the correct format
            JsonObject metadata = propertyJson.get("metadata").getAsJsonObject();
            if (metadata.size() > 0){
                JsonElement name = metadata.get("name");
                JsonElement namespace = metadata.get("namespace"); 
                    // Use the ResourceNameValidator to check that the name is correctly formatted and not null
                    try {
                        nameValidator.assertPropertyNameCharPatternIsValid(name.getAsString());
                    } catch (InternalServletException e){
                        // All ResourceNameValidator error should be added to the list of reasons why the property action has failed
                        validationErrors.add(e.getMessage());
                    }
                    // Use the ResourceNameValidator to check that the namesapce is correctly formatted and not null
                    try {
                        nameValidator.assertNamespaceCharPatternIsValid(namespace.getAsString());
                    } catch (InternalServletException e){
                        // All ResourceNameValidator error should be added to the list of reasons why the property action has failed
                        validationErrors.add(e.getMessage());
                    }

            } else {
                String message = "The 'metadata' field cannot be empty. The fields 'name' and 'namespace' are mandatory for the type GalasaProperty.";
                ServletError error = new ServletError(GAL5024_INVALID_GALASAPROPERTY, message);
                validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
            }
            
            //Check that data is not null and contains the value field
            JsonObject data = propertyJson.get("data").getAsJsonObject();
            if (data.size() > 0){
                if (data.has("value")){
                    String value = data.get("value").getAsString();
                    if (value == null || value.isBlank()) {
                        String message = "The 'value' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.";
                        ServletError error = new ServletError(GAL5024_INVALID_GALASAPROPERTY, message);
                        validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
                    }
                }
            } else {
                String message = "The 'data' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.";
                ServletError error = new ServletError(GAL5024_INVALID_GALASAPROPERTY, message);
                validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
            }

        } else {
            // Caused by bad Key Names in the JSON object i.e. apiversion instead of apiVersion
            ServletError error = new ServletError(GAL5400_BAD_REQUEST,propertyJson.toString());
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
        errors.addAll(validationErrors);
        return validationErrors.size() ==0;
    }

    protected void processGalasaProperty(JsonObject resource, String action) throws InternalServletException{
        try {
            if (checkGalasaPropertyJsonStructure(resource)){
                String apiversion = resource.get("apiVersion").getAsString();
                String expectedApiVersion = GalasaProperty.DEFAULTAPIVERSION;
                if (apiversion.equals(expectedApiVersion)) {
                    GalasaProperty galasaProperty = gson.fromJson(resource, GalasaProperty.class);           
                    CPSNamespace namespace = cps.getNamespace(galasaProperty.getNamespace());

                    //getPropertyFromStore() will only return null if the property is in a hidden namespace
                    CPSProperty property = namespace.getPropertyFromStore(galasaProperty.getName());

                    if (action.equals("delete")) {
                        property.deletePropertyFromStore();
                    } else {
                        /*
                        * The logic below is used to determine if the exclusive Not Or condition in property.setPropertyToStore 
                        * (i.e. "the property exists" must equal to "is this an update action") will action the request or error
                        *
                        * Logic Table to Determine actions
                        * If the action is equal to "update" (force update) the updateProperty is set to true (update property,
                        * will error if the property does not exist in CPS)
                        * If the action is either "update" or "apply" and the property exists in CPS the updateProperty is set to true (update property)
                        * If the action is equal to "apply" and the property does not exist in CPS the updateProperty is set to false (create property)
                        * If the action is equal to "create" (force create) the updateProperty is set to false (create property, will error if the property exists in CPS)
                        */
                        boolean updateProperty = false;
                        if ((updateActions.contains(action) && property.existsInStore()) || action.equals("update")){
                            updateProperty = true;
                        }
                        property.setPropertyToStore(galasaProperty, updateProperty);
                    }
                } else {
                    ServletError error = new ServletError(GAL5027_UNSUPPORTED_API_VERSION, apiversion, expectedApiVersion);
                    throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        } catch (ConfigurationPropertyStoreException e){
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR, e.getMessage());
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}
