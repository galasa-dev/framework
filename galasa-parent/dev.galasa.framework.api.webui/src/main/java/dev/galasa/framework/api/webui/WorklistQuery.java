package dev.galasa.framework.api.webui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;
import dev.galasa.JsonError;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property =
{"osgi.http.whiteboard.servlet.pattern=/webui/worklist" }, name = "Galasa Worklist microservice")
public class WorklistQuery extends HttpServlet {
	
	   private static final long serialVersionUID = 1L;
	   
	   @Reference
	   IFramework framework;
	
	   Gson gson = GalasaGsonBuilder.build();

	@Override 
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {	

		
		String worklistProperty = "";
		try {
			worklistProperty = getWorklistPropertyFromDss();
		} catch (DynamicStatusStoreException e) {
			throw new ServletException("Error while retrieving Worklist property", e);
		}
		
		
		List<WorklistItem> runs = new ArrayList<WorklistItem>();
		
		if (worklistProperty.length() != 0) {
			
			JsonObject jsonObject = new JsonParser().parse(worklistProperty).getAsJsonObject();
			JsonArray runIds = (JsonArray) jsonObject.get("runIds");
			
			
			String id = null;
			for (JsonElement el : runIds) {

				id = el.toString().replaceAll("[\"]", "");

				
				IRunResult run = null;
				try {
					run = getRunById(id);
				} catch (ResultArchiveStoreException e) {
					throw new ServletException("Error while retrieving run", e);
				}
				
				
				if (run != null) {
					
					try {
						
						String runName = run.getTestStructure().getRunName();
						String shortName = run.getTestStructure().getTestShortName();
						String testClass = run.getTestStructure().getTestName();
						String result = run.getTestStructure().getResult();
						
						WorklistItem worklistItem = new WorklistItem(id, runName, shortName, testClass, result);
						
						runs.add(worklistItem);
						
					} catch (ResultArchiveStoreException e) {
						throw new ServletException("Error while retriving run information", e);
					}
					
				} else {
					JsonError error = new JsonError("No run found by ID: " + id);
					PrintWriter out = resp.getWriter();
					resp.setContentType( "Application/json");
			        resp.addHeader("Access-Control-Allow-Origin", "*");
			        resp.setStatus(401);
			        out.print(gson.toJson(error));
			        out.close();
			         
			        return;
				}
				
			}
			
		}
		
		
		JsonObject worklist = new JsonObject();
		JsonElement worklistItems = gson.toJsonTree(runs);
		worklist.add("worklistItems", worklistItems);
		
		
		String json = "";
		 
		 if (worklist.size() != 0) {
			 try {
				 json = gson.toJson(worklist);
			 } catch (Exception e) {
				 throw new ServletException("Error retrieving page, ", e);
			 }
		 }
		
		 try {
	         PrintWriter out = resp.getWriter();
	         resp.setContentType( "Application/json");
	         resp.addHeader("Access-Control-Allow-Origin", "*");
	         out.print(json);
	         out.close();
	      } catch (Exception e) {
	         throw new ServletException("An error occurred whilst retrieving the Worklist", e);
	      }
		
	}
	
	private IRunResult getRunById(String id) throws ResultArchiveStoreException{

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
		
		return run;
	}
	
	private String getWorklistPropertyFromDss() throws DynamicStatusStoreException, ServletException {
	
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		
		try {
			DynamicStatusStoreSingleton.setDss(dss);
		} catch (Exception e) {
			throw new ServletException("Error while setting DSS singleton", e);
		}
		
		String worklistProperty = dss.get("user.username.worklist");
		
		return worklistProperty;
		
	}
	
}