/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;

import dev.galasa.api.runs.ScheduleRequest;
import dev.galasa.api.runs.ScheduleStatus;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns.SharedEnvironmentPhase;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

/**
 * Schedule Tests API
 * 
 * Allows for a set of Tests to be scheduled and their state to be inquired
 * 
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/runs/*" }, name = "Galasa Run Test")
public class ScheduleTests extends HttpServlet {
    private static final long serialVersionUID        = 1L;

    private Log logger = LogFactory.getLog(getClass());
    
    private final Gson        gson = GalasaGsonBuilder.build();

    @Reference
    public IFramework         framework;                                 // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String groupName = getGroupName(req);
        List<IRun> runs = null;
        try {
            runs = framework.getFrameworkRuns().getAllGroupedRuns(groupName);
        } catch (FrameworkException fe) {
            logger.error("Unable to obtain framework runs for Run Group: " + groupName, fe);
            resp.setStatus(500);
            return;
        }
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

        resp.setStatus(200);
        resp.setHeader("Content-Type", "Application/json");

        try {
            resp.getWriter().write(gson.toJson(status));
        } catch (IOException ioe) {
            logger.fatal("Unable to respond to requester", ioe);
            resp.setStatus(500);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean submissionFailures = false;
        String groupName = getGroupName(req);
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

    private String getGroupName(HttpServletRequest req) {
        return req.getPathInfo().substring(1);
    }

    @Activate
    void activate(Map<String, Object> properties) {
        modified(properties);
        logger.info("Galasa Shedule Tests API activated");
    }

    @Modified
    void modified(Map<String, Object> properties) {
        // TODO set the JWT signing key etc
    }

    @Deactivate
    void deactivate() {
        // TODO Clear the properties to prevent JWT generation
    }

}