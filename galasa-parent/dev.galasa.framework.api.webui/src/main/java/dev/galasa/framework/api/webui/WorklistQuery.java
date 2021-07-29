/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.framework.api.webui;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;
import dev.galasa.JsonError;
import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property =
{"osgi.http.whiteboard.servlet.pattern=/webui/worklist" }, name = "Galasa Worklist microservice")
public class WorklistQuery extends HttpServlet {

	private static final long serialVersionUID = 1L;
	  
	@Reference
	IFramework framework;
	
	final static Gson gson = GalasaGsonBuilder.build();
	

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doOptions(req, resp);
		setCORS(resp, req);
	}
	
	private void setCORS(HttpServletResponse resp, HttpServletRequest req) {
		
		String origin = req.getHeader("Origin");
	
		resp.setHeader("Access-Control-Allow-Origin", origin);
        resp.setHeader("Access-Control-Allow-Methods", "*");
        resp.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type");
        resp.setHeader("Allow", "GET, HEAD, POST, TRACE, OPTIONS, DELETE");
        resp.setContentType("application/json");
        resp.setHeader("Vary", "Origin");
	}
	
	
	@Override 
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String principalUsername = getPrincipalUsername(req);
		
		super.doOptions(req, resp);
		setCORS(resp, req);
		
		returnWorklist(principalUsername, resp, 200);

	}
	
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String principalUsername = getPrincipalUsername(req);
		
		Map<String, String> paramMap = getParameterMap(req);
	
		String runId = "";
		if (paramMap.get("runId") != null && !paramMap.get("runId").isEmpty()) {
			runId = paramMap.get("runId");
		} else {
			sendJsonError(resp);
			return;
		}
		
		boolean newResourceCreated = false;
		try {
			newResourceCreated = addRunIdToDss(principalUsername, runId);
		} catch (Exception e) {
			throw new ServletException("Error while updating the DSS", e);
		}
		
		int statusCode = 200;
		if (newResourceCreated == true) {
			statusCode = 201;
		}
		
		super.doOptions(req, resp);
		setCORS(resp, req);
		
		returnWorklist(principalUsername, resp, statusCode);
 
	}
	
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String principalUsername = getPrincipalUsername(req);
		
		Map<String, String> paramMap = getParameterMap(req);
		
		String runId = "";
		if (paramMap.get("runId") != null && !paramMap.get("runId").isEmpty()) {
			runId = paramMap.get("runId");
		} else {
			sendJsonError(resp);
			return;
		}

		try {
			removeRunIdFromDss(principalUsername, runId);
		} catch (Exception e) {
			throw new ServletException("Error while updating the DSS", e);
		}
		
		super.doOptions(req, resp);
		setCORS(resp, req);
		
		returnWorklist(principalUsername, resp, 200);
 
	}
	
	
	private void returnWorklist(String principalUsername, HttpServletResponse resp, int statusCode) throws ServletException {
		
		List<String> runIdsInWorklist = new ArrayList<String>();
		
		try {
			 runIdsInWorklist = getRunIdsFromDss(principalUsername);
		} catch (DynamicStatusStoreException e) {
			throw new ServletException("Error while retrieving Worklist property from the DSS", e);
		}
		

		List<WorklistItem> runs = getWorklistItems(principalUsername, runIdsInWorklist);
		
		
		JsonObject worklist = new JsonObject();
		JsonElement worklistItems = gson.toJsonTree(runs);
		worklist.add("worklistItems", worklistItems);
		
		
		String json = "";
		 
		try {
			json = gson.toJson(worklist);
		} catch (Exception e) {
			throw new ServletException("An error occurred whilst retrieving the Worklist", e);
		}
		
		try {	 		 
	         PrintWriter out = resp.getWriter();
	         resp.setStatus(statusCode);
	         out.print(json);
	         out.close();
	      } catch (Exception e) {
	         throw new ServletException("An error occurred whilst retrieving the Worklist", e);
	      }
		 
		
	}
	
	private String getPrincipalUsername(HttpServletRequest req) {
		
		Principal pU = req.getUserPrincipal();
		
		String principalUsername = "";
		
		if (pU == null) {
			principalUsername = "unknown";
		} else {
			principalUsername = pU.toString().toLowerCase();
		}
		
		return principalUsername;
		
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
	
	
	private List<String> getRunIdsFromDss(String principalUsername) throws DynamicStatusStoreException, ServletException {
		
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		String worklistProperty = dss.get("user." + principalUsername + ".worklist");
		
		if (worklistProperty != null) {
			
			List<String> runIdsInWorklist = new ArrayList<String>();	
			
			if (worklistProperty.length() != 0) {
				
				JsonObject jsonObject = new JsonParser().parse(worklistProperty).getAsJsonObject();
				
				JsonArray runIds = (JsonArray) jsonObject.get("runIds");		

				for (JsonElement el : runIds) {
					runIdsInWorklist.add(el.getAsString());
				}	
			}	
			
			return runIdsInWorklist;
			
		} else {
			return null;
		}
		
	}
	
	
	private List<WorklistItem> getWorklistItems(String principalUsername, List<String> runIdsInWorklist) throws ServletException {
		
		List<WorklistItem> runs = new ArrayList<WorklistItem>();
		
		List<String> runIdsNotFound = new ArrayList<String>();
		
		if (runIdsInWorklist != null) {
			
			IRunResult run = null;
			for (String runId : runIdsInWorklist) {
				try {
					run = getRunById(runId);
				} catch (ResultArchiveStoreException e) {
					throw new ServletException("Error while retrieving run for Run ID " + runId, e);
				}
				
				if (run != null) {
					
					try {
						
						String runName = run.getTestStructure().getRunName();
						String shortName = run.getTestStructure().getTestShortName();
						String testClass = run.getTestStructure().getTestName();
						String result = run.getTestStructure().getResult();
						
						WorklistItem worklistItem = new WorklistItem(runId, runName, shortName, testClass, result);
						
						runs.add(worklistItem);
						
					} catch (ResultArchiveStoreException e) {
						throw new ServletException("Error while retriving Worklist information for Run ID " + runId, e);
					}
					
				} else {
					runIdsNotFound.add(runId);
					try {
						removeRunIdFromDss(principalUsername, runId);
					} catch (Exception e) {
						throw new ServletException("Error while updating the DSS", e);
					}
				}
				
			}
			
			runIdsInWorklist.removeAll(runIdsNotFound);
		}
		
		return runs;
	}
	
	
	private IRunResult getRunById(String id) throws ResultArchiveStoreException {

		IRunResult run = null;
	       
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
           
           run = directoryService.getRunById(id);
           
           if(run != null) {
        	   return run;
           }
           
        }
		
		return null;
	}
	
	
	private boolean addRunIdToDss(String principalUsername, String runId) throws DynamicStatusStoreException, ServletException {
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		
		int count = 0;
		
		do {
			
			String oldValue = dss.get("user." + principalUsername + ".worklist");
			
			if (oldValue != null) {
				
				JsonObject jsonObject = new JsonParser().parse(oldValue).getAsJsonObject();
				JsonArray runIds = (JsonArray) jsonObject.get("runIds");
				
				for (JsonElement el : runIds) {
					if (el.getAsString().equals(runId)) {
						// If Run ID is already in Worklist, exit this method
						return false;
					}
				}
				
				runIds.add(runId);			
				
				String newValue = jsonObject.toString();
				
				
				try {
					dss.performActions(
							new DssSwap("user." + principalUsername + ".worklist", oldValue, newValue));
					return false;
				} catch (DynamicStatusStoreException e) {
					oldValue = dss.get("user." + principalUsername + ".worklist");
					count++;
					
					if (count > 1000) {
						throw new ServletException("Error while updating the DSS, maximum attempts reached", e);
					}
				}			
				
			} else {
				
				createWorklistPropertyInDss(principalUsername, runId);
				
				return true;
			}
			
		} while (true);
			
	}
	
	
	protected void removeRunIdFromDss(String principalUsername, String runId) throws DynamicStatusStoreException, ServletException {
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		
		int count = 0;
		
		do {
			
			String oldValue = dss.get("user." + principalUsername + ".worklist");
			
			if (oldValue != null) {
				
				JsonObject jsonObject = new JsonParser().parse(oldValue).getAsJsonObject();
				JsonArray runIds = (JsonArray) jsonObject.get("runIds");
				
				Iterator<JsonElement> it = runIds.iterator();
				
				boolean found = false;
				while (it.hasNext()) {
					if (it.next().getAsString().equals(runId)){
						it.remove();
						found = true;
						break;
					}
				}
				
				if (found == false) {
					return;
				}
			
				String newValue = jsonObject.toString();
				
				
				try {
					dss.performActions(
							new DssSwap("user." + principalUsername + ".worklist", oldValue, newValue));
					return;
				} catch (DynamicStatusStoreException e) {
					oldValue = dss.get("user." + principalUsername + ".worklist");
					count++;
					
					if (count > 1000) {
						throw new ServletException("Error while updating the DSS, maximum attempts reached", e);
					}
				}
					
			} 
		
		} while (true);
				
	}

	
	private void createWorklistPropertyInDss(String principalUsername, String runId) throws DynamicStatusStoreException, ServletException {
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		
		JsonObject jsonObject = new JsonObject();
		JsonArray runIds = new JsonArray();
		runIds.add(runId);
		jsonObject.add("runIds", runIds);
		
		String value = jsonObject.toString();
		
		try {
			dss.performActions(
					new DssAdd("user." + principalUsername + ".worklist", value));
		} catch (DynamicStatusStoreException e) {
			// If the property already exists at the time of adding, attempt DssSwap instead
			addRunIdToDss(principalUsername, runId);
			return;
		}
		
	}
	
	
	private void sendJsonError(HttpServletResponse resp) throws IOException {
		
		PrintWriter out = resp.getWriter();
		resp.setStatus(400);
		resp.setContentType("Application/json");
        resp.addHeader("Access-Control-Allow-Origin", "*");
        JsonError error = new JsonError("Run ID missing from query");
        String jsonError = gson.toJson(error);
        out.print(jsonError);
        out.close();
		
	}

}