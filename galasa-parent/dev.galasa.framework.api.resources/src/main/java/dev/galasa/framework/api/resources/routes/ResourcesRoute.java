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
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.GalasaProperty;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class ResourcesRoute  extends BaseRoute{

    static final Gson gson = GalasaGsonBuilder.build();

    private static final Set<String> validActions = Set.of("apply","create","update");
    private static final Set<String> updateActions = Set.of("apply","update");
    
    protected List<String> errors = new ArrayList<String>();

    private IFramework framework;

    public ResourcesRoute(ResponseBuilder responseBuilder,  IFramework framework ) {
        super(responseBuilder, "\\/?");
         this.framework = framework;
    }

    @Override
     public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters, 
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {  
        checkRequestHasContent(request);
        String jsonBody = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
        List<String> errorsList = processRequest(jsonBody);
        if (errorsList.size() >0){
            response = getResponseBuilder().buildResponse(response, "application/json", gson.toJson(errorsList), HttpServletResponse.SC_BAD_REQUEST);
        }else{
            response = getResponseBuilder().buildResponse(response, "application/json", "", HttpServletResponse.SC_OK);
        }
        return response;

    }

    public List<String> processRequest(String jsonBody) throws InternalServletException{
        
        JsonObject body = gson.fromJson(jsonBody, JsonObject.class);
        String action = body.get("action").getAsString().toLowerCase().trim();
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
        for (JsonElement element: jsonArray){
                try{
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
            }catch(InternalServletException s){
                errors.add(s.getMessage());
            }catch(Exception e){
                ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    public void processGalasaProperty (JsonObject resource, String action) throws InternalServletException{
        String apiversion = resource.get("apiVersion").getAsString();
        if (apiversion.equals(new GalasaProperty("",null, null).getApiVersion())){
            try{
                GalasaProperty galasaProperty = gson.fromJson(resource, GalasaProperty.class);           
                if (galasaProperty.isPropertyValid()){
                    CPSFacade cps = new CPSFacade(framework);
                    CPSNamespace namespace = cps.getNamespace(galasaProperty.getNamespace());
                    boolean updateProperty = false;
                    CPSProperty property = namespace.getPropertyFromStore(galasaProperty.getName());
                    if (updateActions.contains(action) && (property.existsInStore()) || action.equals("update")){
                        updateProperty = true;
                    }
                    property.setPropertyToStore(galasaProperty, updateProperty);
                }
            }catch (InternalServletException i){
                throw i;
            }catch (Exception e){
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR, e.getMessage());
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }else{
            ServletError error = new ServletError(GAL5027_UNSUPPORTED_API_VERSION, apiversion);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
