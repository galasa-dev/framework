/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

import dev.galasa.api.runs.ScheduleRequest;
import dev.galasa.api.runs.ScheduleStatus;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.runs.commons.GroupRuns;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns.SharedEnvironmentPhase;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class GroupRunsRoute extends GroupRuns{

    private Log logger = LogFactory.getLog(getClass());
    private final Gson gson = GalasaGsonBuilder.build();
    private IFramework framework;


    public GroupRunsRoute(ResponseBuilder responseBuilder, IFramework framework) {
        super(responseBuilder, "[a-zA-Z0-9_]*", framework);
    }

    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, FrameworkException{
        String groupName = pathInfo;
        List<IRun> runs = getRuns(groupName);
        ScheduleStatus serializedRuns = serializeRuns(runs);
        return getResponseBuilder().buildResponse(response, "application/json", gson.toJson(serializedRuns), HttpServletResponse.SC_OK);
    }
    
    public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters, HttpServletRequest request , HttpServletResponse response)
        throws ServletException, IOException, FrameworkException {
            return null;
        }
        
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean submissionFailures = false;
        String groupName = req.getPathInfo();
        ScheduleRequest request;
        try {
            request = (ScheduleRequest) gson.fromJson(new InputStreamReader(req.getInputStream()),
                    ScheduleRequest.class);
        } catch (Exception e) {
            throw new ServletException("Problem translating the json payload", e);
        }
        
        
        ScheduleStatus status = new ScheduleStatus();
        status.setComplete(false);
        
        for (String className : request.getClassNames()) {
            String bundle = className.split("/")[0];
            String testClass = className.split("/")[1];

            SharedEnvironmentPhase senvPhase = null;
            if (request.getSharedEnvironmentPhase() != null) {
                try {
                    senvPhase = SharedEnvironmentPhase.valueOf(request.getSharedEnvironmentPhase());
                } catch(Throwable t) {
                    throw new ServletException("Unable to parse shared environment phase", t);
                }
            }

            try {
                IRun newRun = framework.getFrameworkRuns().submitRun(request.getRequestorType(), request.getRequestor(), bundle, testClass,
                        groupName, request.getMavenRepository(), request.getObr(), request.getTestStream(), false,
                        request.isTrace(), request.getOverrides(), 
                        senvPhase, 
                        request.getSharedEnvironmentRunName(),
                        "java");
                
                status.getRuns().add(newRun.getSerializedRun());
            } catch (FrameworkException fe) {
                logger.error(
                        "Failure when submitting run: " + className, fe);
                submissionFailures = true;
                continue;
            }

        }
        resp.setHeader("Content-Type", "Application/json");
        if (!submissionFailures) {
            resp.setStatus(200);
        } else {
            resp.setStatus(500);
        }
        
        try {
            resp.getWriter().write(gson.toJson(status));
        } catch (IOException ioe) {
            throw new ServletException("Unable to respond to requester", ioe);
        }
    }

}