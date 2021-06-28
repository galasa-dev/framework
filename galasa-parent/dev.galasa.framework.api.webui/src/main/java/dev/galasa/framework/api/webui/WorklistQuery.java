package dev.galasa.framework.api.webui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	final static Gson gson = GalasaGsonBuilder.build();
    
    private WorklistUtils worklistUtils;

	
	@Override 
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String principalUsername = worklistUtils.getPrincipalUsername(req);
		
		returnWorklist(principalUsername, resp, 200);

	}
	
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String principalUsername = worklistUtils.getPrincipalUsername(req);
		
		Map<String, String> paramMap = getParameterMap(req);
	
		String runId = "";
		if (paramMap.get("runId") != null && !paramMap.get("runId").equals("")) {
			runId = paramMap.get("runId");
		}
		

		try {
			worklistUtils.addRunIdToDss(principalUsername, runId);
		} catch (Exception e) {
			throw new ServletException("Error while updating the DSS", e);
		}
		
		int statusCode = 200;
		if (worklistUtils.newResourceCreated == true) {
			statusCode = 201;
		}
		
		returnWorklist(principalUsername, resp, statusCode);
 
	}
	
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String principalUsername = worklistUtils.getPrincipalUsername(req);
		
		Map<String, String> paramMap = getParameterMap(req);
		
		String runId = "";
		if (paramMap.get("runId") != null && !paramMap.get("runId").equals("")) {
			runId = paramMap.get("runId");
		}
		

		try {
			worklistUtils.removeRunIdFromDss(principalUsername, runId);
		} catch (Exception e) {
			throw new ServletException("Error while updating the DSS", e);
		}
		
		returnWorklist(principalUsername, resp, 200);
 
	}
	
	
	protected void returnWorklist(String principalUsername, HttpServletResponse resp, int statusCode) throws ServletException {
		
		List<String> runIdsInWorklist = new ArrayList<String>();
		
		try {
			 runIdsInWorklist = worklistUtils.getRunIdsFromDss(principalUsername);
		} catch (DynamicStatusStoreException e) {
			throw new ServletException("Error while retrieving Worklist property from the DSS", e);
		}
		

		List<WorklistItem> runs = worklistUtils.getWorklistItems(principalUsername, runIdsInWorklist);
		
		
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
	         resp.setStatus(statusCode);
	         out.print(json);
	         out.close();
	      } catch (Exception e) {
	         throw new ServletException("An error occurred whilst retrieving the Worklist", e);
	      }
		 
		
	}

	
	private Map<String, String> getParameterMap(HttpServletRequest request) {

	      Map<String, String[]> ParameterMap = request.getParameterMap();
	      Map<String, String> newParameterMap = new HashMap<>();
	      for (String parameterName : ParameterMap.keySet()) {
	         String[] values = ParameterMap.get(parameterName);
	         if (values != null && values.length > 0) {
	            newParameterMap.put(parameterName, values[0]);
	         } else {
	            newParameterMap.put(parameterName, null);
	         }
	      }
	   
	      return newParameterMap;
	}
	
	
	@Activate
	public void activate() {
	   this.worklistUtils = new WorklistUtils(this.framework);
	}

}