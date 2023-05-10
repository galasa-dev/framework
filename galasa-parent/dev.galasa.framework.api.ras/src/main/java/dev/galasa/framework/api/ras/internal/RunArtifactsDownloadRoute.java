/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

public class RunArtifactsDownloadRoute extends BaseRoute {

   private IFileSystem fileSystem;
   static final Gson gson = GalasaGsonBuilder.build();

   private IFramework framework;
   
   public RunArtifactsDownloadRoute(IFileSystem fileSystem, IFramework framework) {
      super("\\/run\\/([A-z0-9.\\-=]+)\\/artifacts\\/([A-z0-9.\\-=]+)");
      this.fileSystem = fileSystem;
      this.framework = framework;
   }

   @Override
   public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
      Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
      matcher.matches();
      String runId = matcher.group(1);
      String artifactId = matcher.group(2);
      return retrieveArtifacts(runId, artifactId, response);
   }

   private HttpServletResponse retrieveArtifacts(String runId, String artifactId, HttpServletResponse res) throws InternalServletException, IOException {
      IRunResult run = null;
      OutputStream outStream = res.getOutputStream();
      try {
         run = getRunByRunId(runId);
      } catch (ResultArchiveStoreException e) {
         ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
      }
      
      try {
        if (artifactId.equals("run.log")) {
            String runLog = run.getLog();
            if (runLog != null) {
               res.setStatus(HttpServletResponse.SC_OK);
		         res.setContentType( "text/plain");
               String contentDisposition = String.format("attachment; filename=\"%s-run.log\"", run.getTestStructure().getRunName());
               res.setHeader( "Content-Disposition", contentDisposition);
               outStream.write(runLog.getBytes("UTF-8"));
               outStream.close();
            } else {
                //getArtifactPath(artifactId, new ArrayList<>());
            }
        }
      } catch( ResultArchiveStoreException | IOException ex ) {
         ServletError error = new ServletError(GAL5007_ERROR_RETRIEVING_ARTIFACTS,runId);
         throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      return res;
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
   private List<Path> getArtifactPath(Path directory, List<Path> accumulatedPaths) throws IOException {
      FileSystemProvider fsProvider = directory.getFileSystem().provider();
      
      try (DirectoryStream<Path> stream = fsProvider.newDirectoryStream(directory, defaultFilter)) {
         for (Path entry : stream) {
            if (fileSystem.isDirectory(entry)) {
               accumulatedPaths = getArtifactPath(entry, accumulatedPaths);
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