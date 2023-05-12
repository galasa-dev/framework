/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;
import static dev.galasa.framework.api.ras.internal.BaseServlet.*;

public class RunArtifactsListRoute extends RunsRoute {

   static final Gson gson = GalasaGsonBuilder.build();
   
   public RunArtifactsListRoute(IFileSystem fileSystem, IFramework framework) {
      super("\\/runs\\/([A-z0-9.\\-=]+)\\/artifacts\\/?", fileSystem, framework);
   }

   @Override
   public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      String outputString = retrieveResults(runId);
      return sendResponse(res, outputString, HttpServletResponse.SC_OK ); 
   }

   private String retrieveResults(String runId) throws InternalServletException {
      IRunResult run = null;
      JsonArray artifacts;
      try {
         run = getRunByRunId(runId);
      } catch (ResultArchiveStoreException e) {
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }

      try {
         artifacts = getArtifacts(run);
         JsonArray rootartifacts = addArtifactsFromDB(run, artifacts);
         artifacts.addAll(rootartifacts);
      } catch (ResultArchiveStoreException | IOException ex) {
         ServletError error = new ServletError(GAL5007_ERROR_RETRIEVING_ARTIFACTS_LIST,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      return gson.toJson(artifacts);
   }

   private JsonArray addArtifactsFromDB(IRunResult run, JsonArray artifacts) throws ResultArchiveStoreException, IOException{
      // Add the run log as a separate artifact as it does not exist on the file system
      String runLog = run.getLog();
      JsonArray artifactRecords = new JsonArray();
      if (runLog != null) {
         artifactRecords.add(getArtifactAsJsonObject("/run.log","text/plain",runLog.getBytes(StandardCharsets.UTF_8).length));
      }

      TestStructure testStructure = run.getTestStructure();
      if (testStructure != null) {
         String testStructureStr = gson.toJson(testStructure);
         artifactRecords.add(getArtifactAsJsonObject("/structure.json", "application/json", testStructureStr.getBytes(StandardCharsets.UTF_8).length));
      }
      artifactRecords.add(getArtifactAsJsonObject("/artifacts.properties", "text/plain", artifacts.toString().getBytes(StandardCharsets.UTF_8).length));
      return artifactRecords;
   }
}
