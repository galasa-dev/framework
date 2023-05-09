/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

public class RunLogRoute extends BaseRoute {

   private final RunLogRas runLogRas;

   private final static Gson gson = GalasaGsonBuilder.build();

   public RunLogRoute(RunLogRas runLogRas) {
      super("\\/run\\/([A-z0-9.\\-=]+)\\/runlog");
      this.runLogRas = runLogRas;
   }

   @Override
   public String handleRequest(String pathInfo, QueryParameters queryParams) throws ServletException, IOException, FrameworkException {
      
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      String runLog = runLogRas.getRunlog(runId);
      if (runLog != null) {
         JsonObject jsonLog = new JsonObject();
         jsonLog.addProperty("runId", runId);
         jsonLog.addProperty("log", runLog);
         return gson.toJson(jsonLog);
      } else {
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID, runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }  
   }
}