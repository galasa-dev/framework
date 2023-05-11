/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

public class RunArtifactsDownloadRoute extends RunsRoute {

    static final Gson gson = GalasaGsonBuilder.build();
   
   public RunArtifactsDownloadRoute(IFileSystem fileSystem, IFramework framework) {
      super("\\/runs\\/([A-z0-9.\\-=]+)\\/files\\/([A-z0-9.\\-=\\/]+)", fileSystem, framework);
   }

    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
        matcher.matches();
        String runId = matcher.group(1);
        String artifactPath = matcher.group(2);
        return getArtifact(runId, artifactPath, response);
    }

    private HttpServletResponse getArtifact(String runId, String artifactPath, HttpServletResponse res) throws InternalServletException, IOException {
        IRunResult run = null;
        String runName = "";
        String artifactsPrefix = "artifacts";

        // Get run details in order to find artifacts
        try {
            run = getRunByRunId(runId);
            runName = run.getTestStructure().getRunName();
        } catch (ResultArchiveStoreException e) {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        
        // Get artifact that matches artifactId from URL or a path that starts with artifacts/
        try {
            if (artifactPath.equals("run.log")) {
                String runLog = run.getLog();
                if (runLog != null) {
                    res = setDownloadResponse(res, runLog.getBytes(StandardCharsets.UTF_8), "text/plain", "run.log");
                }
            } else if (artifactPath.equals("structure.json")) {
                TestStructure testStructure = run.getTestStructure();
                if (testStructure != null) {
                    String testStructureStr = gson.toJson(testStructure);
                    res = setDownloadResponse(res, testStructureStr.getBytes(StandardCharsets.UTF_8), "application/json", "structure.json");
                }
            } else if (artifactPath.equals("artifacts.properties")) {
                JsonArray artifacts = retrieveArtifacts(run); 
                if (artifacts != null) {
                    res = setDownloadResponse(res, artifacts.toString().getBytes(StandardCharsets.UTF_8), "text/plain", "artifacts.properties");
                }
            } else if (artifactPath.startsWith(artifactsPrefix)) {
                res = retrieveArtifact(res, run, artifactPath.substring(artifactsPrefix.length()));
            } else {
                ServletError error = new ServletError(GAL5008_ERROR_LOCATING_ARTIFACT, artifactPath, runName);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (ResultArchiveStoreException | IOException ex) {
            ServletError error = new ServletError(GAL5009_ERROR_RETRIEVING_ARTIFACT, artifactPath, runName);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }
    
    private HttpServletResponse retrieveArtifact(HttpServletResponse res, IRunResult run, String artifactPath) throws ResultArchiveStoreException, InternalServletException {
        byte[] dataStream;
        try {
            // Get Artifact (path is already given in URL)
            Path artifactLocation = run.getArtifactsRoot().getFileSystem().getPath(artifactPath);

            //Read in file from filesystem
            InputStream fileInputStream = fileSystem.newInputStream(artifactLocation);// broken
            dataStream = fileInputStream.readAllBytes();
            fileInputStream.close();

            setDownloadResponse(res, dataStream, fileSystem.probeContentType(artifactLocation), artifactLocation.getFileName().toString());
                
        } catch (Exception ex) {
            ServletError error = new ServletError(GAL5008_ERROR_LOCATING_ARTIFACT, artifactPath, run.getTestStructure().getRunName());
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    private HttpServletResponse setDownloadResponse(HttpServletResponse res, byte[] content, String contentType, String fileName) throws IOException {
        OutputStream outStream = res.getOutputStream();
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType(contentType);
        res.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        outStream.write(content);
        outStream.close(); 
        return res;
    }
}
