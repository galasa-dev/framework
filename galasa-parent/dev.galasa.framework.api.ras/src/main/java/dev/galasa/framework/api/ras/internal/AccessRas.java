/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.ras.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

/**
 * CPS API
 * 
 * Allows for CPS properties to be retrieved and added
 * 
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/ras/*" }, name = "Galasa RAS")
public class AccessRas extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    private final Gson gson = GalasaGsonBuilder.build();

    private final List<String> RASQUERY = Arrays.asList("requestor", "from", "to", "testclass", "page", "size");

    private final static Double FROM = 2 * Math.pow(10, 8); //Number of seconds in approx 6.3 years

    private final static Pattern pattern1 = Pattern.compile("/runname/([A-z0-9.\\-_']+)/?");
    private final static Pattern pattern2 = Pattern.compile("/run/([A-z0-9.\\-_']+)/?");
    private final static Pattern pattern3 = Pattern.compile("/run/([A-z0-9.\\-_']+)/artifact/([A-z0-9.\\-_']+)/?");
    private final static Pattern pattern5 = Pattern.compile("/run/([A-z0-9.\\-_']+)/runlog/?");

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            resp.setHeader("Content-Type", "Application/json");
            Matcher matcher1 = pattern1.matcher(req.getPathInfo());
            if (matcher1.matches()) {
                getRunsWithRunname(resp, matcher1.group(1));
                return;
            }
            Matcher matcher2 = pattern2.matcher(req.getPathInfo());
            if (matcher2.matches()) {
                getRunStructure(resp, matcher2.group(1));
                return;
            }
            Matcher matcher3 = pattern3.matcher(req.getPathInfo());
            if (matcher3.matches()) {
                getArtifactData(resp, matcher3.group(1), matcher3.group(2));
                return;
            }
            Matcher matcher5 = pattern5.matcher(req.getPathInfo());
            if (matcher5.matches()) {
                getRunLog(resp, matcher5.group(1));
                return;
            }
            sendError(resp, "Invalid GET URL - " + req.getPathInfo());
        } catch(IOException | DateTimeParseException | ResultArchiveStoreException e) {
            sendError(resp, e);
        }
    }

    private void getRunsWithRunname(HttpServletResponse resp, String runName) throws IOException, ResultArchiveStoreException {
        JsonArray respJson = new JsonArray();
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
            for (String requestor : directoryService.getRequestors()) {
                Instant from = Instant.now().minusSeconds(FROM.longValue());
                for (IRunResult result : directoryService.getRuns(requestor, from, Instant.now(), null)) {
                    if (runName.equals(result.getTestStructure().getTestName())) {
                        respJson.add(result.getTestStructure().getRunName());
                    }
                }
            }
        }
        resp.getWriter().write(gson.toJson(respJson));
        resp.setStatus(200);
    }

    private void getRunStructure(HttpServletResponse resp, String runId) throws ResultArchiveStoreException, IOException {
        JsonObject respJson = new JsonObject();
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore()
                .getDirectoryServices()) {
            for (IRunResult result : directoryService.getRuns(runId)) {
                respJson.add("teststructure", gson.toJsonTree(result.getTestStructure()));
                JsonArray artifactFiles = new JsonArray();
                Files.list(result.getArtifactsRoot()).forEach(new ConsumeDirectory(artifactFiles));
                respJson.add("artifactFiles", artifactFiles);
            }
        }
        resp.getWriter().write(gson.toJson(respJson));
        resp.setStatus(200);
    }

    private void getArtifactData(HttpServletResponse resp, String runId, String artifactId) throws ResultArchiveStoreException, IOException {
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore()
                .getDirectoryServices()) {
            for (IRunResult result : directoryService.getRuns(runId)) {
                StringBuilder content = new StringBuilder();
                Files.list(result.getArtifactsRoot()).forEach(new ConsumeArtifact(content, artifactId));
                resp.getWriter().write(content.toString());
                resp.setStatus(200);
                return;
            }
        }
    }

    
    private void getRunLog(HttpServletResponse resp, String runId) throws IOException, ResultArchiveStoreException {
        String runlog = "";
        for(IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
            for(IRunResult result : directoryService.getRuns(runId)) {
                runlog = result.getLog();
            }
        }
        resp.getWriter().write(runlog);
        resp.setStatus(200);
        return;
    }

    @Activate
    void activate(Map<String, Object> properties) {
        modified(properties);
        logger.info("Galasa RAS API activated");
    }

    @Modified
    void modified(Map<String, Object> properties) {
        // TODO set the JWT signing key etc
    }

    @Deactivate
    void deactivate() {
        // TODO Clear the properties to prevent JWT generation
    }

    private static class ConsumeDirectory implements Consumer<Path> {

        private final JsonArray folders;

        public ConsumeDirectory(JsonArray folders) {
            this.folders = folders;
        }

        @Override
        public void accept(Path path) {
            try {
                if(Files.isDirectory(path)) {
                    JsonObject directory = new JsonObject();
                    directory.addProperty("artifactId", path.getFileName().toString());
                    JsonArray children = new JsonArray();
                    Files.list(path).forEach(new ConsumeDirectory(children));
                    directory.add("children", children);
                    folders.add(directory);
                } else {
                    JsonObject file = new JsonObject();
                    file.addProperty("artifactId", path.getFileName().toString());
                    folders.add(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class ConsumeArtifact implements Consumer<Path> {

        private StringBuilder content;
        private final String artifactId;

        public ConsumeArtifact(StringBuilder content, String artifactId) {
            this.content = content;
            this.artifactId = artifactId;
        }

        @Override
        public void accept(Path path) {
            try {
                if(Files.isDirectory(path)) {
                    Files.list(path).forEach(new ConsumeArtifact(content, artifactId));
                } else if(path.getFileName().toString().equals(artifactId)) {
                    if(path.getFileName().toString().endsWith(".gz")) {
                        ByteArrayInputStream bis = new ByteArrayInputStream(Files.readAllBytes(path));
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        GZIPInputStream in = new GZIPInputStream(bis);
                        byte[] buffer = new byte[10240];
                        for (int length = 0; (length = in.read(buffer)) > 0;) {
                            bos.write(buffer, 0, length);
                        }
                        content.append(new String(bos.toByteArray()));
                    } else {
                        content.append(new String(Files.readAllBytes(path)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendError(HttpServletResponse resp, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        sendError(resp, sw.toString());
    }

    public void sendError(HttpServletResponse resp, String errorMessage) {
        resp.setStatus(500);
        JsonObject json = new JsonObject();
        json.addProperty("error", errorMessage);
        try {
            resp.getWriter().write(gson.toJson(json));
        } catch (IOException e) {
            logger.fatal("Unable to respond", e);
        }
    }

}
