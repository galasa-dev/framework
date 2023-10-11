/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.ras.internal.common.RasQueryParameters;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class RequestorRoute extends RunsRoute {

    public RequestorRoute(ResponseBuilder responseBuilder, String path, IFramework framework) {
        /* Regex to match endpoints: 
		*  -> /ras/requestors
		*  -> /ras/requestors?
		*/
        super(responseBuilder, "\\/requestors\\/?", framework);
    }

    final static Gson gson = GalasaGsonBuilder.build();
    private RasQueryParameters sortQueryParameterChecker;

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,HttpServletRequest req, HttpServletResponse response)
    throws ServletException, IOException, FrameworkException {
        this.sortQueryParameterChecker = new RasQueryParameters(queryParams);
        String outputString = retrieveRequestors(queryParams);
        return getResponseBuilder().buildResponse(response, "application/json", outputString, HttpServletResponse.SC_OK); 
    }
    
    private String retrieveRequestors(QueryParameters params) throws InternalServletException, ResultArchiveStoreException{
        List<String> requestorsList = getRequestors();

        //sorts list
			Collections.sort(requestorsList);
            if (params.getSingleString("sort",null) !=null){
			    if(!sortQueryParameterChecker.isAscending("requestor")) {
				    Collections.reverse(requestorsList);
			    }
            }
            
        //create json object
			JsonElement json = new Gson().toJsonTree(requestorsList);
            JsonObject requestors = new JsonObject(); 
			requestors.add("requestors", json);
            return requestors.toString();
    }
}