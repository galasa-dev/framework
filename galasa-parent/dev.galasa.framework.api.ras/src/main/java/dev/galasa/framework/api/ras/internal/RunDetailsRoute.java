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

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;
import static dev.galasa.framework.api.ras.internal.BaseServlet.*;

public class RunDetailsRoute extends BaseRoute {

   private final RunResultRas runResultRas;
   static final Gson gson = GalasaGsonBuilder.build();

   public RunDetailsRoute(RunResultRas runResultRas) {
      super("\\/runs\\/([A-z0-9.\\-=]+)\\/?");
      this.runResultRas = runResultRas;
   }

   @Override
   public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
      RasRunResult run = runResultRas.getRun(pathInfo);
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      
      if(run == null) {
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }
      
      String outputString = gson.toJson(run);
      return sendResponse(res, outputString, HttpServletResponse.SC_OK ); 
   }
}