/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.routes;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.*;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

/**
 * Implementation to retrieve the run log for a given run based on its runId.
 */
public class RunLogRoute extends RunsRoute {

    public RunLogRoute(ResponseBuilder responseBuilder, IFramework framework) {
        //  Regex to match endpoint: /ras/runs/{runid}/runlog
        super(responseBuilder, "\\/runs\\/([A-z0-9.\\-=]+)\\/runlog\\/?");
        this.framework = framework;
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
        
        Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
        matcher.matches();
        String runId = matcher.group(1);
        String runLog = getRunlog(runId);
        if (runLog != null) {
            return getResponseBuilder().sendResponse(res, "text/plain", runLog, HttpServletResponse.SC_OK); 
        } else {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID, runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }
}