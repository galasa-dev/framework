/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.ras.internal.common.RunResultUtility;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

/*
 * Implementation to return details for a given run based on its runId.
 */
public class RunDetailsRoute extends RunsRoute {

   private IFramework framework;

   static final Gson gson = GalasaGsonBuilder.build();

   public RunDetailsRoute(ResponseBuilder responseBuilder, IFramework framework) {
      //  Regex to match endpoint: /ras/runs/{runid}
      super(responseBuilder, "\\/runs\\/([A-z0-9.\\-=]+)\\/?", framework);
      this.framework = framework;
   }

   @Override
   public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
      String runId = getRunIdFromPath(pathInfo);
      try{
         RasRunResult run = getRunFromFramework(runId);
         String outputString = gson.toJson(run);
         return getResponseBuilder().buildResponse(res, "application/json", outputString, HttpServletResponse.SC_OK );
      }catch(ResultArchiveStoreException ex){
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }
   }

   @Override
   public HttpServletResponse handlePutRequest(String pathInfo, QueryParameters queryParams, HttpServletRequest request, HttpServletResponse response) throws DynamicStatusStoreException, FrameworkException, IOException {
      String runId = getRunIdFromPath(pathInfo);
      String runName = getRunNameFromRunId(runId);

      checkRequestHasContent(request);
      String status = getUpdatedRunStatusFromRequestBody(request);

      String responseBody = "";
      if (status.equals("reset")) {
         resetRun(runName);
         responseBody = String.format("Successfully reset run %s", runName);
      } else if (status.equals("delete")) {
         deleteRun(runName);
         responseBody = String.format("Successfully deleted run %s", runName);
      } else {
         // To do - Return error for unknown status 
      }
      // To do - Only return if either of the above have been done.
      return getResponseBuilder().buildResponse(response, "text/plain", responseBody, HttpServletResponse.SC_OK); 
   }

   public RasRunResult getRunFromFramework(String id) throws ResultArchiveStoreException {

      IRunResult run = getRunByRunId(id);

      if (run == null) {
         return null;
      }
      return RunResultUtility.toRunResult(run, false);
   }

   private String getRunIdFromPath(String pathInfo) throws InternalServletException {
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      return runId;
   }

   private String getRunNameFromRunId(String runId) throws ResultArchiveStoreException {
      IRunResult run = getRunByRunId(runId);
      String runName = run.getTestStructure().getRunName();
      return runName;
   }

   private String getUpdatedRunStatusFromRequestBody(HttpServletRequest request) throws IOException {
      ServletInputStream body = request.getInputStream();
      String jsonString = new String(body.readAllBytes(), StandardCharsets.UTF_8);
      body.close();
      String status = gson.fromJson(jsonString, JsonObject.class).get("status").getAsString();
      return status;
   }

   private void resetRun(String runName) throws DynamicStatusStoreException, FrameworkException {
      framework.getFrameworkRuns().reset(runName);
   }

   private void deleteRun(String runName) throws DynamicStatusStoreException, FrameworkException {
      framework.getFrameworkRuns().delete(runName);
   }

}