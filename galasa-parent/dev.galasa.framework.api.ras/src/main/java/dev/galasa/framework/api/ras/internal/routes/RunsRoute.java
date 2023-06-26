/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.routes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.ResultNames;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.ras.internal.common.RunResultUtility;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;


import static dev.galasa.framework.api.common.ServletErrorMessage.*;
public abstract class RunsRoute extends BaseRoute {

    protected IFileSystem fileSystem;
    static final Gson gson = GalasaGsonBuilder.build();

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };

    protected IFramework framework;

    public RunsRoute(String path) {
        super(path);
    }

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

    public String getRunlog(String runId) throws ResultArchiveStoreException, InternalServletException {
      
        IRunResult run = getRunByRunId(runId);
        String runLog = null;
              
        if(run != null) {
           runLog = run.getLog();
        }
        
        return runLog;
     }
  
     public String getRunNamebyRunId(String id) throws ResultArchiveStoreException{
        IRunResult run = getRunByRunId(id);
        return run.getTestStructure().getRunName();
     }
  
     public RasRunResult getRunFromFramework(String id) throws ResultArchiveStoreException {
         
        IRunResult run = getRunByRunId(id);
  
        if(run == null) {
           return null;
        }
        return RunResultUtility.toRunResult(run, false);
     }

    /**
     * Gets a JsonArray of artifacts for a given test run. The format of the
     * returned JSON array is as follows:
     * 
     * <pre>
     * <code>
     * [
     *   {
     *     "path": "/framework/cps_record.properties",
     *     "contentType": "text/plain",
     *     "size": 2306
     *   },
     *   {
     *     "path": "/run.log",
     *     "contentType": "text/plain",
     *     "size": 71478
     *   },
     *   {
     *     "path": "/structure.json",
     *     "contentType": "application/json",
     *     "size": 1301
     *   },
     *   {
     *     "path": "/artifacts.properties",
     *     "contentType": "text/plain",
     *     "size": 378
     *   }
     * ]
     * </code>
     * </pre>
     * 
     * @param run
     * @return
     * @throws ResultArchiveStoreException
     * @throws IOException
     */
    public JsonArray getArtifacts(IRunResult run) throws ResultArchiveStoreException, IOException {

        JsonArray artifactRecords = new JsonArray();
        List<Path> artifactPaths = getArtifactPaths(run.getArtifactsRoot(), new ArrayList<>());
        for (Path artifactPath : artifactPaths) {
            JsonObject artifactRecord = getArtifactAsJsonObject("/artifacts" + artifactPath.toString(), fileSystem.probeContentType(artifactPath), fileSystem.size(artifactPath));
            artifactRecords.add(artifactRecord);
        }
        return artifactRecords;
    }

    /**
     * Walks through an artifact directory recursively, collecting each artifact and
     * filtering out all subdirectories
     * 
     * @param directory        - the path to an artifact directory
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

    public List<String> getResultNames () throws InternalServletException{
		List<String> resultsList = new ArrayList<>();
    
		try {
			for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
                List<String> results = directoryService.getResultNames();
                if (results != null){
				resultsList.addAll(directoryService.getResultNames());
                }
			}
            for (String defaultResultName : ResultNames.getDefaultResultNames()){
				if (!resultsList.contains(defaultResultName)){
					resultsList.add(defaultResultName);
				}
		}
		}
        catch(ResultArchiveStoreException r){
            ServletError error = new ServletError(GAL5004_ERROR_RETRIEVING_PAGE );
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

		Collections.sort(resultsList);

		return resultsList;
	}
}