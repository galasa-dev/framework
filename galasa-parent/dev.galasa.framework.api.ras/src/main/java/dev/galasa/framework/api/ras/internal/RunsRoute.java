/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;
import static dev.galasa.framework.api.ras.internal.BaseServlet.*;

public abstract class RunsRoute extends BaseRoute {
    
    private IFileSystem fileSystem;
    static final Gson gson = GalasaGsonBuilder.build();
 
    private IFramework framework;

    public RunsRoute(String path, IFileSystem fileSystem, IFramework framework) {
        super(path);
        this.framework = framework;
        this.fileSystem = fileSystem;
    }

    protected IRunResult getRunByRunId(String id) throws ResultArchiveStoreException {

        IRunResult run = null;

        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {

            run = directoryService.getRunById(id);

            if (run != null) {
                return run;
            }
        }
        return null;
    }
    
    protected JsonArray retrieveArtifacts(IRunResult run) throws InternalServletException, ResultArchiveStoreException, IOException {

        JsonArray artifactRecords = new JsonArray();
        List<Path> artifactPaths = getArtifactPaths(run.getArtifactsRoot(), new ArrayList<>());
        for (Path artifactPath : artifactPaths) {
           JsonObject artifactRecord = getArtifactAsJsonObject(
              artifactPath.toString(),
              fileSystem.probeContentType(artifactPath),
              fileSystem.size(artifactPath)
           );
           artifactRecords.add(artifactRecord);
        }
        return artifactRecords;
     }

   // Define a default filter to accept everything
   static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };

   /** 
    * Walks through an artifact directory recursively, collecting each artifact and filtering out all subdirectories
    * @param root - an artifact's root directory
    * @param accumulatedPaths - an intermediate list of accumulated artifact paths
    * @return a list of artifact paths
    */ 
   protected List<Path> getArtifactPaths(Path directory, List<Path> accumulatedPaths) throws IOException {
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

   protected JsonObject getArtifactAsJsonObject(String artifactPath, String contentType, long fileSize) throws IOException {
      JsonObject artifactRecord = new JsonObject();
      artifactRecord.addProperty("path", artifactPath);
      artifactRecord.addProperty("contentType", contentType);
      artifactRecord.addProperty("size", fileSize);
      return artifactRecord;
   }

}