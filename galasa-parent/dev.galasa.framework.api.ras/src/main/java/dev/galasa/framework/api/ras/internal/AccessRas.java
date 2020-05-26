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

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] reqParams = req.getPathInfo().substring(1).split("/");
        if (reqParams.length == 2 && reqParams[0].equals("runname")) {
            JsonArray respJson = new JsonArray();
            try {
                for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore()
                        .getDirectoryServices()) {
                    for (IRunResult result : directoryService.getRuns(reqParams[1])) {
                        respJson.add(result.getTestStructure().getRunName());
                    }
                }
            } catch (Exception e) {
                logger.error("Error accessing RAS", e);
                resp.setStatus(500);
                return;
            }
            resp.getWriter().write(gson.toJson(respJson));
            resp.setStatus(200);
            return;

        } else if (reqParams.length == 2 && reqParams[0].equals("run")) {
            JsonObject respJson = new JsonObject();
            try {
                for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore()
                        .getDirectoryServices()) {
                    for (IRunResult result : directoryService.getRuns(reqParams[1])) {
                        respJson.add("teststructure", gson.toJsonTree(result.getTestStructure()));
                        JsonArray artifactFiles = new JsonArray();
                        Files.list(result.getArtifactsRoot()).forEach(new ConsumeDirectory(artifactFiles));
                        respJson.add("artifactFiles", artifactFiles);
                    }
                }
            } catch (Exception e) {
                logger.error("Error accessing RAS", e);
                resp.setStatus(500);
                return;
            }
            resp.getWriter().write(gson.toJson(respJson));
            resp.setStatus(200);
            return;

        } else if (reqParams.length == 4 && reqParams[0].equals("run") && reqParams[2].equals("artifact")) {
            try {
                for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore()
                        .getDirectoryServices()) {
                    for (IRunResult result : directoryService.getRuns(reqParams[1])) {
                        StringBuilder content = new StringBuilder();
                        Files.list(result.getArtifactsRoot()).forEach(new ConsumeArtifact(content, reqParams[3]));
                        resp.getWriter().write(content.toString());
                        resp.setStatus(200);
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("Error accessing RAS", e);
                resp.setStatus(500);
                return;
            }

        } else if (reqParams.length == 1 && reqParams[0].equals("run")) {
            String reqQuery = req.getQueryString();
            if (reqQuery == null) {
                logger.error("Empty RAS Query");
                resp.setStatus(500);
                return;
            }
            Map<String, String> query = new HashMap<>();
            for (String pair : reqQuery.split("&")) {
                String[] keyValue = pair.split("=");
                if (!RASQUERY.contains(keyValue[0])) {
                    logger.error("Invalid RAS Query field: " + keyValue[0]);
                    resp.setStatus(500);
                    return;
                }
                if (keyValue.length == 2)
                    query.put(keyValue[0], keyValue[1]);
            }
            if (!query.containsKey("to") || !query.containsKey("from") || (query.containsKey("page") && !query.containsKey("size"))) {
                logger.error("Invalid RAS Query fields");
                resp.setStatus(500);
                return;
            }

            JsonArray respArray = new JsonArray();
            JsonArray respJson = new JsonArray();
            try {
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
            } catch (DateTimeParseException e) {
                logger.error("Error Parsing DateTime", e);
                resp.setStatus(500);
                return;
            } catch (ResultArchiveStoreException e) {
                logger.error("Error accessing RAS", e);
                resp.setStatus(500);
                return;
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
            return;

        } else if(reqParams.length == 3 && reqParams[0].equals("id") && reqParams[2].equals("runlog")) {
            String runlog = "";
            try {
                for(IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
                    for(IRunResult result : directoryService.getRuns(reqParams[1])) {
                        runlog = result.getLog();
                    }
                }
            } catch (Exception e) {
                logger.error("Error accessing RAS", e);
                resp.setStatus(500);
                return;
            }
            resp.getWriter().write(runlog);
            resp.setStatus(200);
            return;
        }
        
        logger.error("Invalid RAS URL");
        resp.setStatus(500);
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

}
