package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;

import dev.galasa.api.run.RunResult;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/ras/run/*" }, name = "Galasa Run Result microservice")
public class RunResultRas extends HttpServlet {

    @Reference
    IFramework framework;

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
       
       
        Gson gson = GalasaGsonBuilder.build();
       
        String runId = "";
        String url = req.getRequestURI().toString();
        String[] parts = url.split("/");
        
        
        String json = "";
       
         try {
            
            //Check if there is an id
            if (parts[3] != null) {
               runId = parts[3];
               
               RunResult run = getRun(runId);
               
               //Check to see if a run came back with that id
               if(run != null) {
                 json = gson.toJson(getRun(runId));
               }
               
           }    
            
         } catch (Exception e) {
            throw new ServletException("Error occured retrieving run", e);
         }

        try {
           PrintWriter out = res.getWriter();
           res.setContentType( "Application/json");
           res.addHeader("Access-Control-Allow-Origin", "*");
           out.print(json);
           out.close();
      } catch (Exception e) {
    
         throw new ServletException("An error has occured", e);
      }
    }

    private RunResult getRun(String id) throws ResultArchiveStoreException {
       
       IRunResult run = null;
       
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
             
           run = directoryService.getRunById(id);
           
        }
        
        if(run == null) {
           return null;
        }
        
       return RunResultUtility.toRunResult(run);
    }

}
    
