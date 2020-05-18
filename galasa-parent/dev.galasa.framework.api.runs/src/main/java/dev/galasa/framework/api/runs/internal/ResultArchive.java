package dev.galasa.framework.api.runs.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/resultarchive" }, name = "Galasa RAS")
public class ResultArchive extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    private final Gson gson = GalasaGsonBuilder.build();

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject requestBody = gson.fromJson(new InputStreamReader(req.getInputStream()), JsonObject.class);
        String runName = requestBody.get("runName").getAsString();
        try {
            JsonObject response = new JsonObject();
            for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
                if (directoryService.getRuns(runName).size() > 0) {
                    IRunResult result = directoryService.getRuns(runName).get(0);
                    response.addProperty("runlog", result.getLog());
                    response.add("testStructure", gson.toJsonTree(result.getTestStructure()));

                    JsonArray artifactFiles = new JsonArray();
                    Files.list(result.getArtifactsRoot()).forEach(new ConsumeDirectory(artifactFiles));
                    response.add("artifactFiles", artifactFiles);
                    break;
                }
            }

            resp.setStatus(200);
            resp.setHeader("Content-Type", "Application/json");
            resp.getWriter().write(gson.toJson(response));
        } catch (Exception e) {
            logger.fatal("Unable to respond to requester", e);
            resp.setStatus(500);
        }
    }

    private static class ConsumeDirectory implements Consumer<Path> {

        private final JsonArray folders;
        private final Gson gson = GalasaGsonBuilder.build();

        public ConsumeDirectory(JsonArray folders) {
            this.folders = folders;
        }

        @Override
        public void accept(Path path) {
            try {
                if(Files.isDirectory(path)) {
                    JsonObject directory = new JsonObject();
                    directory.addProperty("name", path.getFileName().toString());
                    JsonArray children = new JsonArray();
                    Files.list(path).forEach(new ConsumeDirectory(children));
                    directory.add("children", children);
                    folders.add(directory);
                } else {
                    JsonObject file = new JsonObject();
                    file.addProperty("name", path.getFileName().toString());

                    if(path.getFileName().toString().endsWith(".gz")) {
                        ByteArrayInputStream bis = new ByteArrayInputStream(Files.readAllBytes(path));
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        GZIPInputStream in = new GZIPInputStream(bis);
                        byte[] buffer = new byte[10240];
                        for (int length = 0; (length = in.read(buffer)) > 0;) {
                            bos.write(buffer, 0, length);
                        }
                        file.add("content", gson.fromJson(new String(bos.toByteArray()), JsonObject.class));
                    } else {
                        file.add("content", gson.toJsonTree(new String(Files.readAllBytes(path))));
                    }
                    folders.add(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}