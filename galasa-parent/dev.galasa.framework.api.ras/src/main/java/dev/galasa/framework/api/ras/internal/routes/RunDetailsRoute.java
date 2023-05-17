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

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.api.ras.internal.commons.InternalServletException;
import dev.galasa.framework.api.ras.internal.commons.QueryParameters;
import dev.galasa.framework.api.ras.internal.commons.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.BaseServlet.*;
import static dev.galasa.framework.api.ras.internal.commons.ServletErrorMessage.*;

/*
 * Implementation to return details for a given run based on its runId.
 */
public class RunDetailsRoute extends RunsRoute {


   static final Gson gson = GalasaGsonBuilder.build();

   public RunDetailsRoute(IFramework framework) {
      //  Regex to match endpoint: /ras/runs/{runid}
      super("\\/runs\\/([A-z0-9.\\-=]+)\\/?");
      this.framework = framework;
   }

   @Override
   public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      try{
         RasRunResult run = getRunFromFramework(runId);
         String outputString = gson.toJson(run);
         return sendResponse(res, outputString, HttpServletResponse.SC_OK ); 
      
      }catch(ResultArchiveStoreException ex){
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }
   }
}