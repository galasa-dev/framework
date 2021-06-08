package dev.galasa.framework.api.webui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;
import dev.galasa.framework.spi.DynamicStatusStoreException;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property =
{"osgi.http.whiteboard.servlet.pattern=/webui/worklist" }, name = "Galasa Worklist microservice")
public class WorklistQuery extends HttpServlet {

	private static final long serialVersionUID = 1L;
	  
	@Reference
	IFramework framework;
	
	Gson gson = GalasaGsonBuilder.build();
	   
	static List<String> runIdsInWorklist = new ArrayList<String>();
	   
    static List<String> runIdsNotFound = new ArrayList<String>();
    
    private WorklistUtils worklistUtils;
	  

	
	@Override 
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		returnWorklist(resp);

	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		Map<String, String> paramMap = worklistUtils.getParameterMap(req);
	
		String runId = "";
		if (paramMap.get("runId") != null && !paramMap.get("runId").equals("")) {
			runId = paramMap.get("runId");
		}
		

		try {
			worklistUtils.addRunIdToDss(runId);
		} catch (Exception e) {
			throw new ServletException("Error while updating the DSS", e);
		}
		
		returnWorklist(resp);
 
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		Map<String, String> paramMap = worklistUtils.getParameterMap(req);
		
		String runId = "";
		if (paramMap.get("runId") != null && !paramMap.get("runId").equals("")) {
			runId = paramMap.get("runId");
		}
		

		try {
			worklistUtils.removeRunIdFromDss(runId);
		} catch (Exception e) {
			throw new ServletException("Error while updating the DSS", e);
		}
		
		returnWorklist(resp);
 
	}
	
	protected void returnWorklist(HttpServletResponse resp) throws ServletException {
		
		try {
			 runIdsInWorklist = worklistUtils.getRunIdsFromDss();
		} catch (DynamicStatusStoreException e) {
			throw new ServletException("Error while retrieving Worklist property from the DSS", e);
		}
		
		
		List<WorklistItem> runs = worklistUtils.getWorklistItems();
		
		if (runIdsInWorklist != null) {
			runIdsInWorklist.removeAll(runIdsNotFound);
		}
		
		
		JsonObject worklist = new JsonObject();
		JsonElement worklistItems = gson.toJsonTree(runs);
		worklist.add("worklistItems", worklistItems);
		
		
		String json = "";
		 
		if (!worklist.isJsonNull()) {
			try {
				json = gson.toJson(worklist);
			} catch (Exception e) {
				throw new ServletException("An error occurred whilst retrieving the Worklist", e);
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
	
	@Activate
	public void activate() {
	   this.worklistUtils = new WorklistUtils(this.framework);
	}

}