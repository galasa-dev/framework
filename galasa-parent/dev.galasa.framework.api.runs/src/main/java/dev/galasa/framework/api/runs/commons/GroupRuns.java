/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.commons;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.google.gson.Gson;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns.SharedEnvironmentPhase;
import dev.galasa.api.runs.ScheduleRequest;
import dev.galasa.api.runs.ScheduleStatus;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class GroupRuns extends BaseRoute {

    protected IFramework framework;
    private final Gson gson = GalasaGsonBuilder.build();
    

    public GroupRuns(ResponseBuilder responseBuilder, String path, IFramework framework) {
        super(responseBuilder, path);
        this.framework = framework;
    }

    protected List<IRun> getRuns(String groupName) throws InternalServletException {
         List<IRun> runs = null;
        try {
            runs = this.framework.getFrameworkRuns().getAllGroupedRuns(groupName);
        } catch (FrameworkException fe) {
            ServletError error = new ServletError(GAL5019_UNABLE_TO_RETRIEVE_RUNS, groupName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return runs;
    }

    protected ScheduleStatus serializeRuns(@NotNull List<IRun> runs) {
       
        ScheduleStatus status = new ScheduleStatus();
        boolean complete = true;
        for (IRun run : runs) {
            if (!"FINISHED".equalsIgnoreCase(run.getStatus()) &&
                    !"UP".equalsIgnoreCase(run.getStatus()) &&
                    !"DISCARDED".equalsIgnoreCase(run.getStatus())) {
                complete = false;
            }

            status.getRuns().add(run.getSerializedRun());
        }

        status.setComplete(complete);
        return status;
    }

    protected boolean checkRequestHasContent(HttpServletRequest request) throws InternalServletException{
        if (request.getContentLength() < 1){
            ServletError error = new ServletError(GAL5411_NO_REQUEST_BODY, request.getPathInfo());
            throw new InternalServletException(error, HttpServletResponse.SC_LENGTH_REQUIRED);
        }
        return true;
    }

    protected ScheduleRequest getScheduleRequestfromRequest(HttpServletRequest request) throws InternalServletException{
        ScheduleRequest scheduleRequest;
        try{
            String payload = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
            scheduleRequest = (ScheduleRequest) gson.fromJson(payload,ScheduleRequest.class);
        } catch(Exception e) {
            ServletError error = new ServletError(GAL5020_UNABLE_TO_CONVERT_TO_SCHEDULE_REQUEST);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return scheduleRequest;
    }

    public ScheduleStatus scheduleRun(ScheduleRequest request, String groupName, String jwtRequestor) throws ServletException, IOException, InternalServletException {
            
        ScheduleStatus status = new ScheduleStatus();
        status.setComplete(false);
            
        for (String className : request.getClassNames()) {
            String bundle = className.split("/")[0];
            String testClass = className.split("/")[1];

            SharedEnvironmentPhase senvPhase = null;
            if (request.getSharedEnvironmentPhase() != null) {
                try {
                    senvPhase = SharedEnvironmentPhase.valueOf(request.getSharedEnvironmentPhase());
                } catch (Throwable t) {
                    ServletError error = new ServletError(GAL5022_UNABLE_TO_PARSE_SHARED_ENVIRONMENT_PHASE);
                    throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }

            if(jwtRequestor == null){
                jwtRequestor = request.getRequestor(); 
            }
            try {
                IRun newRun = framework.getFrameworkRuns().submitRun(request.getRequestorType(), jwtRequestor, bundle, testClass,
                        groupName, request.getMavenRepository(), request.getObr(), request.getTestStream(), false,
                        request.isTrace(), request.getOverrides(), 
                        senvPhase, 
                        request.getSharedEnvironmentRunName(),
                        "java");
                
                status.getRuns().add(newRun.getSerializedRun());
            } catch (FrameworkException fe) {
                ServletError error = new ServletError(GAL5021_UNABLE_TO_SUMBIT_RUNS, className);  
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        return status;
    }
}
