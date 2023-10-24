/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.framework.api.authentication.JwtWrapper;
import dev.galasa.api.runs.ScheduleRequest;
import dev.galasa.api.runs.ScheduleStatus;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.runs.common.GroupRuns;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
public class GroupRunsRoute extends GroupRuns{

    private final Gson gson = GalasaGsonBuilder.build();


    public GroupRunsRoute(ResponseBuilder responseBuilder, IFramework framework) {
        /* Regex to match endpoints: 
		*  -> /runs/{GroupID}
		*/
        super(responseBuilder, "[a-zA-Z0-9_\\-]*", framework);
    }

    public HttpServletResponse handleGetRequest(String groupName, QueryParameters queryParams, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, FrameworkException{
        List<IRun> runs = getRuns(groupName);
        if (runs != null){
            ScheduleStatus serializedRuns = serializeRuns(runs);
            return getResponseBuilder().buildResponse(response, "application/json", gson.toJson(serializedRuns), HttpServletResponse.SC_OK);
        }else{
            ServletError error = new ServletError(GAL5019_UNABLE_TO_RETRIEVE_RUNS, groupName);  
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    public HttpServletResponse handlePostRequest(String groupName, QueryParameters queryParameters, HttpServletRequest request , HttpServletResponse response)
    throws ServletException, IOException, FrameworkException {
        String requestor;
        checkRequestHasContent(request);
        ScheduleRequest scheduleRequest = getScheduleRequestfromRequest(request);
        try{
            requestor = new JwtWrapper(request).getUserName();
        }catch(Exception e){
            /* If no JWT is present the try block will through an exception
             * Currently this process should work without a jwt however when authentication 
             * is enforced this catch should throw an exception
             */
            requestor = null;
        }
        ScheduleStatus scheduleStatus = scheduleRun(scheduleRequest, groupName, requestor);
        return getResponseBuilder().buildResponse(response, "application/json", gson.toJson(scheduleStatus), HttpServletResponse.SC_CREATED);
    }

}