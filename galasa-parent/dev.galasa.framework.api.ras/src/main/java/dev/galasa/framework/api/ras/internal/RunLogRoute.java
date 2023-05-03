/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.spi.FrameworkException;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

public class RunLogRoute extends BaseRoute {

   private final RunLogRas runLogRas;

   public RunLogRoute(RunLogRas runLogRas) {
      super("\\/run\\/([A-z0-9.\\-=]+)\\/runlog");
      this.runLogRas = runLogRas;
   }

   @Override
   public String handleRequest(String pathInfo, QueryParameters queryParams) throws ServletException, IOException, FrameworkException {
      String runLog = runLogRas.getRunlog(pathInfo);
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      
      if (runLog.isEmpty()) {
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID, runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }

      return runLog;
   }
}