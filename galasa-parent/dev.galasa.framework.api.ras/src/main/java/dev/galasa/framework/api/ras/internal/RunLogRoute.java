/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

public class RunLogRoute extends BaseRoute {

   private final RunLogRas runLogRas;

   public RunLogRoute(RunLogRas runLogRas) {
      super("\\/([A-z0-9.\\-=]+)\\/runlog");
      this.runLogRas = runLogRas;
   }

   @Override
   public void handleRequest(HttpServletRequest req, HttpServletResponse res, String runId)
         throws ServletException, IOException {
      try {      
         String runLog = runLogRas.getRunlog(req.getPathInfo());
         
         if (runLog.isEmpty()) {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
         }

         sendResponse(res, runLog, 200);

      } catch(Exception e) {
         String responseBody = new ServletError(GAL5002_INVALID_RUN_ID, runId).toString();
         int httpFailureCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
         sendResponse(res, responseBody, httpFailureCode);
      }
   }
}