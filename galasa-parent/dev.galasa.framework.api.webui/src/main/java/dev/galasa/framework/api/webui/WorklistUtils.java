package dev.galasa.framework.api.webui;


import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreException;
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
	
	protected boolean newResourceCreated = false;
	
	
	protected String getPrincipalUsername(HttpServletRequest req) {
		
		Principal pU = req.getUserPrincipal();
		
		String principalUsername = "";
		
		if (pU == null) {
			principalUsername = "unknown";
		} else {
			principalUsername = pU.toString().toLowerCase();
		}
		
		return principalUsername;
		
	}
	
	
	protected List<String> getRunIdsFromDss(String principalUsername) throws DynamicStatusStoreException, ServletException {
		
	
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
	
	
	protected List<WorklistItem> getWorklistItems(String principalUsername, List<String> runIdsInWorklist) throws ServletException {
		
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
	
	protected IRunResult getRunById(String id) throws ResultArchiveStoreException {

		IRunResult run = null;
	       
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
           
           run = directoryService.getRunById(id);
           
           if(run != null) {
        	   return run;
           }
           
        }
		
		return null;
	}
	
	protected void removeRunIdFromDss(String principalUsername, String runId) throws DynamicStatusStoreException, ServletException {
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		String oldValue = dss.get("user." + principalUsername + ".worklist");
		
		if (oldValue != null) {
			
			JsonObject jsonObject = new JsonParser().parse(oldValue).getAsJsonObject();
			JsonArray runIds = (JsonArray) jsonObject.get("runIds");
			
			JsonArray runIdsToRemove = new JsonArray();
			for (JsonElement el : runIds) {
				if (el.getAsString().equals(runId)) {
					runIdsToRemove.add(el);
				}
			}
			
			if (runIdsToRemove.size()>0) {
				for (JsonElement el : runIdsToRemove) {
					runIds.remove(el);
				}
			}
		
			jsonObject.remove("runIds");
			jsonObject.add("runIds", runIds);

			
			String newValue = jsonObject.toString();
			
			boolean exceptionThrown = false;
			int count = 0;
			
			do {

				try {
					dss.performActions(
							new DssSwap("user." + principalUsername + ".worklist", oldValue, newValue));
				} catch (DynamicStatusStoreException e) {
					exceptionThrown = true;
					oldValue = dss.get("user." + principalUsername + ".worklist");
					count++;
					
					if (count > 1000) {
						throw new ServletException("Error while updating the DSS, maximum attempts reached");
					}
				}
				
				exceptionThrown = false;
				
			} while (exceptionThrown);
			
		}
			
	}

	
	protected void addRunIdToDss(String principalUsername, String runId) throws DynamicStatusStoreException, ServletException {
		
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("webui");
		String oldValue = dss.get("user." + principalUsername + ".worklist");
		
		if (oldValue != null) {
			
			JsonObject jsonObject = new JsonParser().parse(oldValue).getAsJsonObject();
			JsonArray runIds = (JsonArray) jsonObject.get("runIds");
			
			boolean runIdInArray = false;
			for (JsonElement el : runIds) {
				if (el.getAsString().equals(runId)) {
					runIdInArray = true;
					break;
				}
			}
			
			if (runIdInArray == false) {
				runIds.add(runId);
			}
			
			jsonObject.remove("runIds");
			jsonObject.add("runIds", runIds);	
			
			
			String newValue = jsonObject.toString();
			
			boolean exceptionThrown = false;
			int count = 0;
			
			do {

				try {
					dss.performActions(
							new DssSwap("user." + principalUsername + ".worklist", oldValue, newValue));
				} catch (DynamicStatusStoreException e) {
					exceptionThrown = true;
					oldValue = dss.get("user." + principalUsername + ".worklist");
					count++;
					
					if (count > 1000) {
						throw new ServletException("Error while updating the DSS, maximum attempts reached");
					}
				}
				
				exceptionThrown = false;
				
			} while (exceptionThrown);
		
			
		} else {
			createWorklistPropertyInDss(principalUsername, runId);
		}
			
	}
	
	protected void createWorklistPropertyInDss(String principalUsername, String runId) throws DynamicStatusStoreException, ServletException {
		
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

		newResourceCreated = true;
		
	}

}
