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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

public class RunArtifactsListRoute extends BaseRoute {

   private IFileSystem fileSystem;
   static final Gson gson = GalasaGsonBuilder.build();

   private IFramework framework;
   
   public RunArtifactsListRoute(IFileSystem fileSystem, IFramework framework) {
      super("\\/run\\/([A-z0-9.\\-=]+)\\/artifacts");
      this.fileSystem = fileSystem;
      this.framework = framework;
   }

   @Override
   public String handleRequest(String pathInfo, QueryParameters queryParams) throws ServletException, IOException, FrameworkException {
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);

      return retrieveResults(runId);
   }

   private String retrieveResults(String runId) throws InternalServletException {
      IRunResult run = null;
      try {
         run = getRunByRunId(runId);
      } catch (ResultArchiveStoreException e) {
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }

      JsonArray artifactRecords = new JsonArray();
      try {
         // Walk through the artifact root directory, collecting each artifact and
         // filtering out all subdirectories
         fileSystem.walk(run.getArtifactsRoot())
            .filter(fileSystem::isRegularFile)
            .forEach(artifactPath -> {
               JsonObject artifactRecord = new JsonObject();

               artifactRecord.add("runId", new JsonPrimitive(runId));
               artifactRecord.add("path", new JsonPrimitive(artifactPath.toString()));
               artifactRecord.add("url", new JsonPrimitive(artifactPath.toString()));
   
               artifactRecords.add(artifactRecord);
            });
   
      } catch( ResultArchiveStoreException | IOException ex ) {
         ServletError error = new ServletError(GAL5007_ERROR_RETRIEVING_ARTIFACTS,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

      String returnedJsonString = gson.toJson(artifactRecords);
      return returnedJsonString;
   }

   private IRunResult getRunByRunId(String id) throws ResultArchiveStoreException {

      IRunResult run = null;

      for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {

         run = directoryService.getRunById(id);

         if(run != null) {
            return run;
         }
      }
      return null;
   }
}