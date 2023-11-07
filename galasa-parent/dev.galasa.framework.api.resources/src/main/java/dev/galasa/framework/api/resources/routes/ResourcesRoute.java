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
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.cps.internal.common.GalasaProperty;
import dev.galasa.framework.api.cps.internal.common.PropertyActions;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class ResourcesRoute  extends BaseRoute{

    static final Gson gson = GalasaGsonBuilder.build();

    private static final Set<String> validActions = Set.of("apply","create","update");
    private List<ServletError> errors = new ArrayList<ServletError>();

    private IFramework framework;
    PropertyActions propertyActions;

    public ResourcesRoute(ResponseBuilder responseBuilder,  IFramework framework ) {
        super(responseBuilder, "\\/?");
        this.framework = framework;
         this.propertyActions = new PropertyActions(framework);
    }

    protected IFramework getFramework() {
        return this.framework;
    }

    @Override
     public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters, 
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {  
        HttpServletResponse returnResponse;   
        checkRequestHasContent(request);
        String jsonBody = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
        List<ServletError> errorsList = processRequest(jsonBody);
        if (errorsList.size() >0){
            returnResponse = getResponseBuilder().buildResponse(response, "application/json", gson.toJson(errorsList), HttpServletResponse.SC_BAD_REQUEST);
        }else{
            returnResponse = getResponseBuilder().buildResponse(response, "application/json", "", HttpServletResponse.SC_OK);
        }
        return returnResponse;

    }

    public List<ServletError> processRequest(String jsonBody) throws InternalServletException{
        
        JsonObject body = gson.fromJson(jsonBody, JsonObject.class);
        String action = body.get("action").toString().toLowerCase().trim();
        if (validActions.contains(action)){
            JsonArray jsonArray = body.get("data").getAsJsonArray();
            processDataArray(jsonArray, action);
        }else{
            ServletError error = new ServletError(GAL5025_UNSUPPORTED_ACTION, action);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    return errors;
    }

    public void processDataArray(JsonArray jsonArray, String action) throws InternalServletException{
        try{
            for (JsonElement element: jsonArray){
                JsonObject resource = element.getAsJsonObject();
                switch (resource.get("kind").toString()){
                    case "GalasaProperty":
                        processGalasaProperty(resource,action);
                        break;
                    default:
                        ServletError error = new ServletError(GAL5026_UNSUPPORTED_RESOURCE_TYPE);
                        throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
                }
            }

        }catch(InternalServletException s){
            errors.add(s.getError());
        }catch(Exception e){
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    public void processGalasaProperty (JsonObject resource, String action) throws InternalServletException{
        String apiversion = resource.get("apiversion").toString();
        if (apiversion.equals(new GalasaProperty(null, null).getApiVersion())){
            try{
                GalasaProperty property = gson.fromJson(resource, GalasaProperty.class);
                if (propertyActions.isPropertyValid(property)){
                    propertyActions.setGalasaProperty(property, action);
                }
            }catch (FrameworkException f){
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR, f.getMessage());
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }else{
            ServletError error = new ServletError(GAL5027_UNSUPPORTED_API_VERSION);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
