/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;
import static dev.galasa.framework.api.ras.internal.BaseServlet.*;

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
   public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      String outputString = retrieveResults(runId);
      return sendResponse(res, outputString, HttpServletResponse.SC_OK ); 
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
         List<Path> artifactPaths = getArtifactPaths(run.getArtifactsRoot(), new ArrayList<>());
         for (Path artifactPath : artifactPaths) {
            JsonObject artifactRecord = getArtifactJsonObject(artifactPath);
            artifactRecords.add(artifactRecord);
         }
         
         // Add the run log as a separate artifact as it does not exist on the file system
         String runLog = run.getLog();
         if (runLog != null) {
            JsonObject artifactRecord = new JsonObject();
   
            artifactRecord.addProperty("path", "/run.log");
            artifactRecord.addProperty("contentType", "text/plain");
            artifactRecord.addProperty("size", runLog.getBytes("UTF-8").length);
   
            artifactRecords.add(artifactRecord);
         }
   
      } catch( ResultArchiveStoreException | IOException ex ) {
         ServletError error = new ServletError(GAL5007_ERROR_RETRIEVING_ARTIFACTS,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

      return gson.toJson(artifactRecords);
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

   // Define a default filter to accept everything
   static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };

   /** 
    * Walks through an artifact directory recursively, collecting each artifact and filtering out all subdirectories
    * @param root - an artifact's root directory
    * @param accumulatedPaths - an intermediate list of accumulated artifact paths
    * @return a list of artifact paths
    */ 
   private List<Path> getArtifactPaths(Path directory, List<Path> accumulatedPaths) throws IOException {
      FileSystemProvider fsProvider = directory.getFileSystem().provider();
      
      try (DirectoryStream<Path> stream = fsProvider.newDirectoryStream(directory, defaultFilter)) {
         for (Path entry : stream) {
            if (fileSystem.isDirectory(entry)) {
               accumulatedPaths = getArtifactPaths(entry, accumulatedPaths);
            } else {
               accumulatedPaths.add(entry);
            }
         }
      }
      return accumulatedPaths;
   }

   private JsonObject getArtifactJsonObject(Path artifactPath) throws IOException {
      JsonObject artifactRecord = new JsonObject();
      artifactRecord.addProperty("path", artifactPath.toString());
      artifactRecord.addProperty("contentType", fileSystem.probeContentType(artifactPath));
      artifactRecord.addProperty("size", fileSystem.size(artifactPath));

      return artifactRecord;
   }
}