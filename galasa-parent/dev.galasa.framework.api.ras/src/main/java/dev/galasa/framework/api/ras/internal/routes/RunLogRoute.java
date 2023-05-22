/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.routes;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.ras.internal.common.InternalServletException;
import dev.galasa.framework.api.ras.internal.common.QueryParameters;
import dev.galasa.framework.api.ras.internal.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.BaseServlet.*;
import static dev.galasa.framework.api.ras.internal.common.ServletErrorMessage.*;

/**
 * Implementation to retrieve the run log for a given run based on its runId.
 */
public class RunLogRoute extends RunsRoute {

   private final static Gson gson = GalasaGsonBuilder.build();

   public RunLogRoute(IFramework framework) {
      //  Regex to match endpoint: /ras/runs/{runid}/runlog
      super("\\/runs\\/([A-z0-9.\\-=]+)\\/runlog\\/?");
      this.framework = framework;

   }

   @Override
   public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
      
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      String runLog = getRunlog(runId);
      if (runLog != null) {
         String runname = getRunNamebyRunId(runId);
         JsonObject jsonLog = new JsonObject();
         jsonLog.addProperty("name", runname);
         jsonLog.addProperty("log", runLog);
         String outputString = gson.toJson(jsonLog);

         return sendResponse(res, outputString, HttpServletResponse.SC_OK ); 
      } else {
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID, runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }  
   }
}