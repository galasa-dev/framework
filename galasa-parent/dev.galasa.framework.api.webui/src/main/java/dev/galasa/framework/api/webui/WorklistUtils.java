package dev.galasa.framework.api.webui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkPropertyFileException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;

public class WorklistUtils {
	
	private IFramework framework;
	
	public WorklistUtils(IFramework framework) {
		this.framework = framework;
	}
	
	
	protected List<String> getRunIdsFromDss() throws DynamicStatusStoreException, ServletException {
	
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		String worklistProperty = dss.get("user.username.worklist");
		
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
	
	
	protected List<WorklistItem> getWorklistItems() throws ServletException {
		
		List<WorklistItem> runs = new ArrayList<WorklistItem>();
		
		if (WorklistQuery.runIdsInWorklist != null) {
			
			IRunResult run = null;
			for (String runId : WorklistQuery.runIdsInWorklist) {
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
					WorklistQuery.runIdsNotFound.add(runId);
					try {
						removeRunIdFromDss(runId);
					} catch (Exception e) {
						throw new ServletException("Error while updating the DSS", e);
					}
				}
				
			}
		}
		
		return runs;
	}
	
	protected IRunResult getRunById(String id) throws ResultArchiveStoreException {

		IRunResult run = null;
	       
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
           
           run = directoryService.getRunById(id);
           
           if(run != null) {
              break;
           }
           
        }
		
		return run;
	}
	
	protected void removeRunIdFromDss(String runId) throws DynamicStatusStoreException, FrameworkPropertyFileException {
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		String oldValue = dss.get("user.username.worklist");
		
		if (oldValue != null) {
			
			JsonObject jsonObject = new JsonParser().parse(oldValue).getAsJsonObject();
			JsonArray runIds = (JsonArray) jsonObject.get("runIds");
			
			JsonElement id = new JsonParser().parse(runId);
			if (runIds.contains(id)) {
				runIds.remove(id);
				jsonObject.remove("runIds");
				jsonObject.add("runIds", runIds);
			}	
			
			String newValue = jsonObject.toString();
			
			dss.performActions(
					new DssSwap("user.username.worklist", oldValue, newValue));
			
		}
			
	}
	
	protected void addRunIdToDss(String runId) throws DynamicStatusStoreException, FrameworkPropertyFileException {
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		String oldValue = dss.get("user.username.worklist");
		
		if (oldValue != null) {
			
			JsonObject jsonObject = new JsonParser().parse(oldValue).getAsJsonObject();
			JsonArray runIds = (JsonArray) jsonObject.get("runIds");
			
			JsonElement id = new JsonParser().parse(runId);
			if (!runIds.contains(id)) {
				runIds.add(id);
				jsonObject.remove("runIds");
				jsonObject.add("runIds", runIds);	
			}
			
			String newValue = jsonObject.toString();
			
			dss.performActions(
					new DssSwap("user.username.worklist", oldValue, newValue));
			
		} else {
			createWorklistPropertyInDss(runId);
		}
			
	}
	
	protected void createWorklistPropertyInDss(String runId) throws DynamicStatusStoreException {
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		
		JsonObject jsonObject = new JsonObject();
		JsonArray runIds = new JsonArray();
		runIds.add(runId);
		jsonObject.add("runIds", runIds);
		
		String value = jsonObject.toString();
		
		dss.performActions(
				new DssAdd("user.username.worklist", value));
		
	}
	
	protected Map<String, String> getParameterMap(HttpServletRequest request) {

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

}
