/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.spi.FrameworkException;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

public class RunDetailsRoute extends BaseRoute {

   private final RunResultRas runResultRas;

   public RunDetailsRoute(RunResultRas runResultRas) {
      super("\\/run\\/([A-z0-9.\\-=]+)");
      this.runResultRas = runResultRas;
   }

   @Override
   public String handleRequest(String pathInfo, QueryParameters queryParams) throws ServletException, IOException, FrameworkException {
      RasRunResult run = runResultRas.getRun(pathInfo);
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      
      if(run == null) {
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }

      return BaseServlet.gson.toJson(run);
   }
}