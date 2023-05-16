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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public abstract class RunsRoute extends BaseRoute {

    protected IFileSystem fileSystem;
    static final Gson gson = GalasaGsonBuilder.build();

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };

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
    protected JsonArray getArtifacts(IRunResult run) throws ResultArchiveStoreException, IOException {

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
     * @param root             - an artifact's root directory
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