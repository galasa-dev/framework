/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;

import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
      "osgi.http.whiteboard.servlet.pattern=/ras/run/*", "osgi.http.whiteboard.context.path=/" }, name = "Galasa Run microservice")
public class RasRunServlet extends HttpServlet {
   
   private static final long serialVersionUID = 1L;
   
   @Reference
   public IFramework framework;

   protected IFileSystem fileSystem = new FileSystem();

   static final Gson gson = GalasaGsonBuilder.build();
   
   private final Map<String, Route> routes = new HashMap<>();
   
   RunResultRas runResultRas;
   RunLogRas runLogRas;

   public void init() {
      addRoute(new RunDetailsRoute(runResultRas));
      addRoute(new RunLogRoute(runLogRas));
      addRoute(new RunArtifactsListRoute(fileSystem, framework));
   }

   private void addRoute(Route route) {
      routes.put(route.getPath(), route);
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

      String url = req.getPathInfo();
      if (url != null) {
         for (Map.Entry<String, Route> entry : routes.entrySet()) {

            String routePattern = entry.getKey();
            Route route = entry.getValue();
            
            Matcher matcher = Pattern.compile(routePattern).matcher(url);

            if (matcher.matches()) {
               // Retrieve the first matching group (0 retrieves the entire pattern)
               String runId = matcher.group(1);

               route.handleRequest(req, res, runId);
               return;
            }
         }
      }
      ResponseUtility.sendError("Invalid path", 404, res);
   }
   
   @Activate
   public void activate() {
      this.runResultRas = new RunResultRas(this.framework);
      this.runLogRas = new RunLogRas(this.framework);
   }
}
