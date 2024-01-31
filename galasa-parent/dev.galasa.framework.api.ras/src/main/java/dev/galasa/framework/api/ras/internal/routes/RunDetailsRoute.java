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

import dev.galasa.framework.api.ras.internal.common.RunActionJson;
import dev.galasa.framework.api.ras.internal.common.RunActionStatus;
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
import dev.galasa.framework.spi.utils.GalasaGson;

/*
 * Implementation to return details for a given run based on its runId.
 */
public class RunDetailsRoute extends RunsRoute {

   private IFramework framework;

   static final GalasaGson gson = new GalasaGson();

   protected static final String path = "\\/runs\\/([A-z0-9.\\-=]+)\\/?";

   public RunDetailsRoute(ResponseBuilder responseBuilder, IFramework framework) {
      //  Regex to match endpoint: /ras/runs/{runid}
      super(responseBuilder, path, framework);
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
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, ex);
      }
   }

   @Override
   public HttpServletResponse handlePutRequest(String pathInfo, QueryParameters queryParams, HttpServletRequest request, HttpServletResponse response) throws DynamicStatusStoreException, FrameworkException, IOException {
      String runId = getRunIdFromPath(pathInfo);
      String runName = getRunNameFromRunId(runId);

      checkRequestHasContent(request);
      RunActionJson runAction = getUpdatedRunActionFromRequestBody(request);
      
      return getResponseBuilder().buildResponse(response, "text/plain", updateRunStatus(runName, runAction), HttpServletResponse.SC_OK);
   } 


   private String updateRunStatus(String runName, RunActionJson runAction) throws InternalServletException {
      String responseBody = "";
      RunActionStatus status = RunActionStatus.getfromString(runAction.getStatus());
      String result = runAction.getResult();
      
      if (status == null) {
         ServletError error = new ServletError(GAL5045_INVALID_STATUS_UPDATE_REQUEST, runAction.getStatus());
         throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
      } else if (status == RunActionStatus.QUEUED) {
         resetRun(runName);
         logger.info("Run reset by external source.");
         responseBody = String.format("Successfully reset run %s", runName);
      } else if (status == RunActionStatus.FINISHED) {
         cancelRun(runName, result);
         logger.info("Run cancelled by external source.");
         responseBody = String.format("Successfully cancelled run %s", runName);
      } 
      return responseBody;
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

   private RunActionJson getUpdatedRunActionFromRequestBody(HttpServletRequest request) throws IOException {
      ServletInputStream body = request.getInputStream();
      String jsonString = new String(body.readAllBytes(), StandardCharsets.UTF_8);
      body.close();
      RunActionJson runAction = gson.fromJson(jsonString, RunActionJson.class);
      return runAction;
   }

   private void resetRun(String runName) throws InternalServletException {
      boolean isReset = false;
      try {
      isReset = framework.getFrameworkRuns().reset(runName);
      } catch (FrameworkException e){
         ServletError error = new ServletError(GAL5047_UNABLE_TO_RESET_RUN, runName);
         throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
      }
      if (!isReset){
         ServletError error = new ServletError(GAL5049_UNABLE_TO_RESET_COMPLETED_RUN, runName);
         throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
      }
   }

   private void cancelRun(String runName, String result) throws InternalServletException {
      boolean isCanceled = false;
      if (!result.equalsIgnoreCase("cancelled")){
         ServletError error = new ServletError(GAL5046_UNABLE_TO_CANCEL_RUN_INVALID_RESULT, runName, result);
         throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
      }
      try {
         // Cancelling a run works by deleting all its entries in the DSS
         isCanceled = framework.getFrameworkRuns().delete(runName);
      } catch (FrameworkException e) {
         ServletError error = new ServletError(GAL5048_UNABLE_TO_CANCEL_RUN, runName);
         throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
      }
      if (!isCanceled) {
         ServletError error = new ServletError(GAL5050_UNABLE_TO_CANCEL_COMPLETED_RUN, runName);
         throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
      }
   }

}