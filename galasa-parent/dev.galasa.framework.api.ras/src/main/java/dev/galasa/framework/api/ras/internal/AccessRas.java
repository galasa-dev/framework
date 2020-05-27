/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.ras.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    private final static Double FROM = 2 * Math.pow(10, 8);

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            Pattern pattern1 = Pattern.compile("/runname/([A-z0-9.\\-_']+)/?");
            Matcher matcher1 = pattern1.matcher(req.getPathInfo());
            Pattern pattern2 = Pattern.compile("/run/([A-z0-9.\\-_']+)/?");
            Matcher matcher2 = pattern2.matcher(req.getPathInfo());
            Pattern pattern3 = Pattern.compile("/run/([A-z0-9.\\-_']+)/artifact/([A-z0-9.\\-_']+)/?");
            Matcher matcher3 = pattern3.matcher(req.getPathInfo());
            Pattern pattern4 = Pattern.compile("/run/?");
            Matcher matcher4 = pattern4.matcher(req.getPathInfo());
            Pattern pattern5 = Pattern.compile("/run/([A-z0-9.\\-_']+)/runlog/?");
            Matcher matcher5 = pattern5.matcher(req.getPathInfo());

            if (matcher1.matches()) {
                getRunsWithRunname(resp, matcher1.group(1));
            } else if (matcher2.matches()) {
                getRunStructure(resp, matcher2.group(1));
            } else if (matcher3.matches()) {
                getArtifactData(resp, matcher3.group(1), matcher3.group(2));
            } else if (matcher4.matches()) {
                getRunsByQuery(resp, req.getQueryString());
            } else if (matcher5.matches()) {
                getRunLog(resp, matcher5.group(1));
            } else {
                sendError(resp, "Invalid GET URL - " + req.getPathInfo());
            }
        } catch(IOException | ResultArchiveStoreException | DateTimeParseException e) {
            sendError(resp, e.getStackTrace());
        }
    }

    private void getRunsWithRunname(HttpServletResponse resp, String runName) throws IOException, ResultArchiveStoreException {
        JsonArray respJson = new JsonArray();
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
            for (String requestor : directoryService.getRequestors()) {
                Instant from = Instant.now().minusSeconds(FROM.longValue());
                for (IRunResult result : directoryService.getRuns(requestor, from, Instant.now())) {
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

    private void getRunsByQuery(HttpServletResponse resp, String queryString) throws IOException, ResultArchiveStoreException, DateTimeParseException {
        if (queryString == null) {
            sendError(resp, "Empty RAS Query");
            return;
        }
        Map<String, String> query = new HashMap<>();
        for (String pair : queryString.split("&")) {
            String[] keyValue = pair.split("=");
            if (!RASQUERY.contains(keyValue[0])) {
                sendError(resp, "Invalid RAS Query field: " + keyValue[0]);
                return;
            }
            if (keyValue.length == 2)
                query.put(keyValue[0], keyValue[1]);
        }
        if (!query.containsKey("to") || !query.containsKey("from") || (query.containsKey("page") && !query.containsKey("size"))) {
            sendError(resp, "Invalid RAS Query fields");
            return;
        }

        JsonArray respArray = new JsonArray();
        JsonArray respJson = new JsonArray();
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
            if(query.containsKey("requestor")) {
                for (IRunResult result : directoryService.getRuns(query.get("requestor"), Instant.parse(query.get("from")), Instant.parse(query.get("to")))) {
                    if(query.containsKey("testclass")) {
                        if (query.get("testclass").equals(result.getTestStructure().getTestName())) {
                            respArray.add(result.getTestStructure().getRunName());
                        }
                    } else {
                        respArray.add(result.getTestStructure().getRunName());
                    }
                }
            } else {
                for (String requestor : directoryService.getRequestors()) {
                    for (IRunResult result : directoryService.getRuns(requestor, Instant.parse(query.get("from")), Instant.parse(query.get("to")))) {
                        if(query.containsKey("testclass")) {
                            if (query.get("testclass").equals(result.getTestStructure().getTestName())) {
                                respArray.add(result.getTestStructure().getRunName());
                            }
                        } else {
                            respArray.add(result.getTestStructure().getRunName());
                        }
                    }
                }
            }
        }

        if(query.containsKey("page")) {
            int size = Integer.parseInt(query.get("size"));
            int page = Integer.parseInt(query.get("page"));
            for(int i = size * (page - 1); i < size * page && i < respArray.size(); i++) {
                respJson.add(respArray.get(i));
            }
        } else if(query.containsKey("size")) {
            int size = Integer.parseInt(query.get("size"));
            for(int i = 0; i < size && i < respArray.size(); i++) {
                respJson.add(respArray.get(i));
            }
        } else {
            for(int i = 0; i < respArray.size(); i++) {
                respJson.add(respArray.get(i));
            }
        }
        
        resp.getWriter().write(gson.toJson(respJson));
        resp.setStatus(200);
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

    public void sendError(HttpServletResponse resp, StackTraceElement[] trace) {
        StringBuilder message = new StringBuilder();
        for(StackTraceElement element : trace) {
            message.append(element.toString());
        }
        sendError(resp, message.toString());
    }

    public void sendError(HttpServletResponse resp, String trace) {
        resp.setStatus(500);
        JsonObject json = new JsonObject();
        json.addProperty("error", trace);
        try {
            resp.getWriter().write(gson.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
