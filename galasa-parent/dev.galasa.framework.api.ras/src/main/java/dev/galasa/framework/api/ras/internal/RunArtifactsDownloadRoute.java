/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

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
        super("\\/run\\/([A-z0-9.\\-=]+)\\/artifacts\\/([A-z0-9.\\-=\\/]+)");
        this.fileSystem = fileSystem;
        this.framework = framework;
    }

    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
        matcher.matches();
        String runId = matcher.group(1);
        String artifactPath = matcher.group(2);
        return retrieveArtifacts(runId, artifactPath, response);
    }

    private HttpServletResponse retrieveArtifacts(String runId, String artifactPath, HttpServletResponse res) throws InternalServletException, IOException {
        IRunResult run = null;
        OutputStream outStream = res.getOutputStream();
        String runName = "";

        // Get run details in order to find artifacts
        try {
            run = getRunByRunId(runId);
            runName = run.getTestStructure().getRunName();
        } catch (ResultArchiveStoreException e) {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        
        // Get artifact that matches artifactId from URL
        try {
            // Check if run.log is used as ID and return run.log file
            if (artifactPath.equals("run.log")) {
                String runLog = run.getLog();
                if (runLog != null) {
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.setContentType( "text/plain");
                    String contentDisposition = String.format("attachment; filename=\"%s-run.log\"", runName);
                    res.setHeader( "Content-Disposition", contentDisposition);
                    outStream.write(runLog.getBytes(StandardCharsets.UTF_8));
                    outStream.close();
                }
            } else {
                // Get Artifact (path is already given in URL)
                Path artifactLocation = run.getArtifactsRoot().getFileSystem().getPath(artifactPath);

                //Read in file from filesystem
                InputStream fileInputStream = new FileInputStream(artifactLocation.toString());
                byte[] dataStream = fileInputStream.readAllBytes();
                
                for (byte b : dataStream) {
                    outStream.write(b);
                }

                outStream.close();
                fileInputStream.close();
            }
        } catch (ResultArchiveStoreException | IOException ex) {
            ServletError error = new ServletError(GAL5007_ERROR_RETRIEVING_ARTIFACTS,runName);
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
}