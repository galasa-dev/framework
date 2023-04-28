/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.api.ras.RasRunResult;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

public class RunDetailsRoute extends BaseRoute {

   private final RunResultRas runResultRas;

   public RunDetailsRoute(RunResultRas runResultRas) {
      super("\\/([A-z0-9.\\-=]+)");
      this.runResultRas = runResultRas;
   }

   @Override
   public void handleRequest(HttpServletRequest req, HttpServletResponse res, String runId) throws ServletException, IOException {
      try {
         RasRunResult run = runResultRas.getRun(req.getPathInfo());
         
         if(run == null) {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
         }

         String response = RasRunServlet.gson.toJson(run);
         sendResponse(res, response, 200);

      } catch (Exception e) {
         String responseBody = new ServletError(GAL5002_INVALID_RUN_ID, runId).toString();
         int httpFailureCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
         sendResponse(res, responseBody, httpFailureCode);
      }
   }
}