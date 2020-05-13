package dev.galasa.framework.api.runs.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Path rasRoot = framework.getResultArchiveStore().getStoredArtifactsRoot();
        JsonObject responseJson = getRasStructure(rasRoot.toFile());

        try {
            resp.setStatus(200);
            resp.setHeader("Content-Type", "Application/json");
            resp.getWriter().write(gson.toJson(responseJson));
        } catch (Exception e) {
            logger.fatal("Unable to respond to requester", e);
            resp.setStatus(500);
        }

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject requestBody = gson.fromJson(new InputStreamReader(req.getInputStream()), JsonObject.class);

        try {
            File file = new File(requestBody.get("resultPath").getAsString());
            InputStream fi = new FileInputStream(file);
            OutputStream os = resp.getOutputStream();
            byte[] buffer = new byte[10240];

            for (int length = 0; (length = fi.read(buffer)) > 0;) {
                os.write(buffer, 0, length);
            }
            resp.setStatus(200);
            fi.close();
        } catch (Exception e) {
            logger.fatal("Unable to respond to requester", e);
            resp.setStatus(500);
        }
    }

    private JsonObject getRasStructure(File root) {
        if(root.isDirectory()) {
            JsonObject directory = new JsonObject();
            directory.addProperty("name", root.getName());
            directory.addProperty("directory", true);
            JsonArray children = new JsonArray();
            for(File child : root.listFiles()) {
                children.add(getRasStructure(child));
            }
            directory.add("children", children);
            return directory;
        } else {
            JsonObject file = new JsonObject();
            file.addProperty("name", root.getName());
            file.addProperty("directory", false);
            file.addProperty("resultPath", root.getPath());
            return file;
        }
    }
}