package dev.galasa.framework.api.ras.internal.routes;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.ras.internal.common.RasQueryParameters;
import dev.galasa.framework.api.ras.internal.verycommon.*;
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
        super(responseBuilder, "\\/requestors?", framework);
    }

    final static Gson gson = GalasaGsonBuilder.build();

    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response)
    throws ServletException, IOException, FrameworkException {
        String outputString = retrieveRequestors(new RasQueryParameters(queryParams));
        return getResponseBuilder().buildResponse(response, "application/json", outputString, HttpServletResponse.SC_OK); 
    }
    
    private String retrieveRequestors(RasQueryParameters params) throws InternalServletException, ResultArchiveStoreException{
        List<String> list = getRequestors();

        //sorts list
			Collections.sort(list);
            if (params.getSortValue() !=null){
			    if(!params.isAscending("requestor")) {
				    Collections.reverse(list);
			    }
            }
            
        //create json object
			JsonElement json = new Gson().toJsonTree(list);
            JsonObject requestors = new JsonObject(); 
			requestors.add("requestors", json);
            return requestors.toString();
    }
}