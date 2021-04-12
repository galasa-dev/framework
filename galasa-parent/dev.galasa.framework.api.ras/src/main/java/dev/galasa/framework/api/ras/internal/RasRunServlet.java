package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;

import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
      "osgi.http.whiteboard.servlet.pattern=/ras/run/*", "osgi.http.whiteboard.context.path=/" }, name = "Galasa Run microservice")
public class RasRunServlet extends HttpServlet {
   
   private static final long serialVersionUID = 1L;
   
   @Reference
   public IFramework framework;
   
   private static final Gson gson = GalasaGsonBuilder.build();
   
   private static final Pattern pattern = Pattern.compile("\\/([A-z0-9.\\-=]+)");
   private static final Pattern pattern2 = Pattern.compile("\\/([A-z0-9.\\-=]+)\\/runlog");
   
   private RunResultRas runResultRas;
   private RunLogRas runLogRas;
   
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
      
      
      String url = req.getPathInfo();
     
      Matcher matcher1 = pattern.matcher(url);
  
      String response = "";
      
      if(matcher1.matches()) {
         
         try {
            
            RasRunResult run = runResultRas.getRun(url);
            
            if(run != null) {
               response = gson.toJson(run);
               ResponseUtility.sendJsonResponse(response, 200, res);
               return;
            }else {
               ResponseUtility.sendError("Could not receive run", 404, res);
               return;
            }
            
            
         }catch(Exception e) {
            throw new ServletException("Error occured trying to retrieve run from framework", e);
         }
      
      }
      
      Matcher matcher2 = pattern2.matcher(url);
     
      if(matcher2.matches()) {
          
         try {
            
            String runLog = runLogRas.getRunlog(url);
            
            if(!runLog.isEmpty()) {
               response = runLog;
               ResponseUtility.sendTextResponse(response, 200, res);
               return;
            }else {
               ResponseUtility.sendError("Could not receive run log", 404, res);
               return;
            }
            

            
         }catch(Exception e) {
            throw new ServletException("Error occured trying to retrieve run from framework", e);
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
