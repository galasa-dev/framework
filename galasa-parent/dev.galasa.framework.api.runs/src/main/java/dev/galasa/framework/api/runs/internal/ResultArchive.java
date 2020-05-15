package dev.galasa.framework.api.runs.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            JsonObject responseJson = new JsonObject();
            JsonArray directoryArray = new JsonArray();
            for(IResultArchiveStoreDirectoryService x : framework.getResultArchiveStore().getDirectoryServices()) {
                JsonObject directoryService = new JsonObject();
                directoryService.addProperty("name", x.getName().substring(0,x.getName().indexOf("/")).trim());
                directoryService.add("structure", getRasStructure(new File(x.getName().substring(x.getName().indexOf("/")).trim())));
                directoryArray.add(directoryService);
            }
            responseJson.add("rasServices", directoryArray);
        
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
        String resultPath = requestBody.get("resultPath").getAsString();
        File file = isInRas(resultPath);
        if(file != null) {
            try {
                InputStream fi = new FileInputStream(file);
                OutputStream os = resp.getOutputStream();
                byte[] buffer = new byte[10240];
                if(file.getName().lastIndexOf(".") == -1 || !file.getName().substring(file.getName().lastIndexOf(".")).equals(".gz")) {
                    for (int length = 0; (length = fi.read(buffer)) > 0;) {
                        os.write(buffer, 0, length);
                    }
                } else {
                    GZIPInputStream in = new GZIPInputStream(fi);
                    for (int length = 0; (length = in.read(buffer)) > 0;) {
                        os.write(buffer, 0, length);
                    }
                    in.close();
                }
                resp.setStatus(200);
                fi.close();
            } catch (Exception e) {
                logger.fatal("Unable to respond to requester", e);
                resp.setStatus(500);
            }
        } else {
            logger.warn("Attempt to access file not in RAS: " + resultPath);
            resp.setStatus(400);
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
            String requiredPath = root.getPath().substring(0, root.getPath().indexOf("/ras/"));
            file.addProperty("resultPath", root.getPath().replace(requiredPath, ""));
            return file;
        }
    }

    private File isInRas(String path) {
        for(IResultArchiveStoreDirectoryService dirService : framework.getResultArchiveStore().getDirectoryServices()) {
            Path directoryPath = Paths.get(dirService.getName().substring(dirService.getName().indexOf("/")).trim());
            File file = directoryPath.resolve(path.replace("/ras/", "")).toFile();
            if(file.exists() && !file.isDirectory()) {
                return file;
            }
        }
        return null;
    }
}