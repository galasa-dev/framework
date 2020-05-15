package dev.galasa.framework.api.runs.internal;

import java.io.IOException;
import java.io.InputStreamReader;

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

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/propertystore" }, name = "Galasa CPS")
public class PropertyStore extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    private final Gson gson = GalasaGsonBuilder.build();

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        JsonObject requestBody = gson.fromJson(new InputStreamReader(req.getInputStream()), JsonObject.class);
        String namespace = requestBody.get("namespace").getAsString();
        String prefix = requestBody.get("prefix").getAsString();
        String suffix = requestBody.get("suffix").getAsString();
        String[] infixes = new String[0];
        if(requestBody.get("infixes") != null) {
            JsonArray infixesArray = requestBody.get("infixes").getAsJsonArray();

            infixes = new String[infixesArray.size()];
            for(int i = 0; i < infixesArray.size(); i++) {
                infixes[i] = infixesArray.get(i).getAsString();
            }
        }

        try {
            IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService(namespace);
            String propValue = cps.getProperty(prefix, suffix, infixes);
            resp.setStatus(200);
            resp.setHeader("Content-Type", "Application/json");
            JsonObject respValue = new JsonObject();
            respValue.addProperty("value", propValue);
            resp.getWriter().write(gson.toJson(respValue));
        } catch (Exception e) {
            logger.error("Unable to get property", e);
            resp.setStatus(500);
        }
    }
    
}