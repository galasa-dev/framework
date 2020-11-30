package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;

import dev.galasa.JsonError;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.RunResult;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    private final Gson gson = GalasaGsonBuilder.build();

    private static final long serialVersionUID = 1L;
    private static final Pattern pattern = Pattern.compile("(?!.*\\/).+");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
       
       
        String runId = "";
        String url = req.getRequestURI().toString();

        Matcher matcher = pattern.matcher(url);
        
        String json = "";
       
         try {
            
            //Check if there is an id
            if (matcher.find()) {
               runId = matcher.group();
               
               RunResult run = getRun(runId);
               
               //Check to see if a run came back with that id
               if(run != null) {
                 json = gson.toJson(getRun(runId));
               }else {
                  PrintWriter out = res.getWriter();
                  res.setStatus(404);
                  res.setContentType("Application/json");
                  res.addHeader("Access-Control-Allow-Origin", "*");
                  JsonError error = new JsonError("Could not find requested run");
                  String jsonError = gson.toJson(error);
                  out.print(jsonError);
                  out.close();
                  return;
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
           if(run != null) {
              break;
           }
           
        }
        
        if(run == null) {
           return null;
        }
        
       return RunResultUtility.toRunResult(run, false);
    }

}
    
