/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.cps.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

/**
 * CPS API
 * 
 * Allows for CPS properties to be retrieved and added
 * 
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
    "osgi.http.whiteboard.servlet.pattern=/cps/*" }, name = "Galasa CPS")
public class AccessCps extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    private final Gson gson = GalasaGsonBuilder.build();

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] reqParams = req.getPathInfo().substring(1).split("/");
        if(reqParams.length == 1 && reqParams[0].equals("namespace")) {
            JsonArray namespaceArray = new JsonArray();
            try {
                List<String> namespaces = framework.getConfigurationPropertyService("framework").getCPSNamespaces();
                for(String name : namespaces) {
                    namespaceArray.add(name);
                }
            } catch(ConfigurationPropertyStoreException e) {
                logger.error("Unable to access CPS", e);
                resp.setStatus(500);
                return;
            }
            resp.getWriter().write(gson.toJson(namespaceArray));
            resp.setStatus(200);
            return;
        } else if(reqParams.length == 2 && reqParams[0].equals("namespace")) {
            JsonArray propertyArray = new JsonArray();
            try {
                Map<String,String> properties = framework.getConfigurationPropertyService(reqParams[1]).getAllProperties();
                for(String prop : properties.keySet()) {
                    JsonObject cpsProp = new JsonObject();
                    cpsProp.addProperty("name", prop);
                    cpsProp.addProperty("value", properties.get(prop));
                    propertyArray.add(cpsProp);
                }
            } catch(ConfigurationPropertyStoreException e) {
                logger.error("Unable to access CPS", e);
                resp.setStatus(500);
                return;
            }
            resp.getWriter().write(gson.toJson(propertyArray));
            resp.setStatus(200);
            return;
        } else if(reqParams.length == 6 && reqParams[0].equals("namespace") && reqParams[2].equals("prefix") && reqParams[4].equals("suffix")) {
            String query = req.getQueryString();
            String[] infixArray = null;
            if(query == null) {
                infixArray = new String[0];
            } else {
                String[] queries = query.split("&");
                List<String> infixes = new ArrayList<>();
                for(String pair : queries) {
                    String[] keyValue = pair.split("=");
                    if(!keyValue[0].equals("infix")) {
                        logger.error("Invalid Infix in URL");
                        resp.setStatus(500);
                        return;
                    }
                    infixes.add(keyValue[1]);
                }
                infixArray = infixes.toArray(new String[0]);
            }
            JsonObject respJson = new JsonObject();

            try {
                String propValue = framework.getConfigurationPropertyService(reqParams[1]).getProperty(reqParams[3], reqParams[5], infixArray);
                Map<String, String> pairs = framework.getConfigurationPropertyService(reqParams[1]).getAllProperties();
                for(String key : pairs.keySet()) {
                    if(pairs.get(key).equals(propValue) && key.startsWith(reqParams[1] + "." + reqParams[3]) && key.endsWith(reqParams[5])) {
                        respJson.addProperty("name", key);
                        respJson.addProperty("value", pairs.get(key));
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to Access CPS");
                resp.setStatus(500);
                return;
            }
            resp.getWriter().write(gson.toJson(respJson));
            resp.setStatus(200);
            return;
        }
        logger.error("Invalid Request to CPS");
        resp.setStatus(500);
        return;
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] reqParams = req.getPathInfo().substring(1).split("/");
        if(reqParams.length != 4 || !reqParams[0].equals("namespace") || !reqParams[2].equals("property")) {
            logger.error("Url incorrect: " + req.getPathInfo());
            resp.setStatus(500);
            return;
        }
        String urlName = reqParams[3];

        JsonObject reqJson = gson.fromJson(new InputStreamReader(req.getInputStream()),JsonObject.class);
        if(!urlName.equals(reqJson.get("name").getAsString())) {
            logger.error("Different CPS property name in url and request: " + urlName + ", " + reqJson.get("name"));
            resp.setStatus(500);
            return;
        }
        try {
            IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService(reqParams[1]);
            cps.setProperty(reqJson.get("name").getAsString(), reqJson.get("value").getAsString());
            resp.setStatus(200);
        } catch(ConfigurationPropertyStoreException e) {
            logger.error("Unable to access CPS for namesapce: " + reqParams[1], e);
            resp.setStatus(500);
            return;
        }
    }

    @Activate
    void activate(Map<String, Object> properties) {
        modified(properties);
        logger.info("Galasa CPS API activated");
    }

    @Modified
    void modified(Map<String, Object> properties) {
        // TODO set the JWT signing key etc
    }

    @Deactivate
    void deactivate() {
        // TODO Clear the properties to prevent JWT generation
    }

}
