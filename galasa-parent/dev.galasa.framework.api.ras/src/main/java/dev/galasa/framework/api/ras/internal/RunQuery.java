package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/ras/run" }, name = "Galasa Runs microservice")
public class RunQuery extends HttpServlet {
	
	@Reference
	IFramework framework;
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		
		Map<String, String> paramMap = getParameterMap(req);
		
		try {
			
		List<IRunResult> runs = getRuns(paramMap.get("requestor"), Instant.parse(paramMap.get("to")), 
				Instant.parse(paramMap.get("from")), paramMap.get("testname"));
		
		int pageSize = Integer.parseInt(paramMap.get("pageSize").trim());
		
		int amountOfRuns = runs.size();
		int startIndex = 0;
		int endIndex = 0;
		int pageNum = 1;
		
		int numPages = (int)Math.ceil(amountOfRuns / pageSize);
		
		while(pageSize > amountOfRuns) {
			
			List<IRunResult> newList = runs.subList(startIndex, (endIndex+pageSize)-1);
		
			JsonObject returnObj = new JsonObject();
			returnObj.addProperty("pageSize", String.valueOf(pageSize));
			returnObj.addProperty("pageNumber", String.valueOf(pageNum));
			returnObj.addProperty("numPages", String.valueOf(numPages));
			
			JsonArray returnArray = new JsonArray();
			
			for(IRunResult run : newList) {
				
				JsonObject runObj = new JsonObject();

				runObj.addProperty("runName", run.getTestStructure().getRunName());
				runObj.addProperty("testName", run.getTestStructure().getTestName());
				runObj.addProperty("testShortName", run.getTestStructure().getTestShortName());
				runObj.addProperty("bundle", run.getTestStructure().getBundle());
				runObj.addProperty("requestor", run.getTestStructure().getRequestor());
				runObj.addProperty("result", run.getTestStructure().getResult());
				runObj.addProperty("status", run.getTestStructure().getStatus());
				runObj.addProperty("queued", run.getTestStructure().getQueued().toString());
				runObj.addProperty("start", run.getTestStructure().getStartTime().toString());
				runObj.addProperty("end", run.getTestStructure().getEndTime().toString());
				
				returnArray.add(runObj);
				
			}
			
			pageNum++;
			amountOfRuns -= pageSize;
			startIndex += pageSize;
			
		}
		
		
		}catch(Exception e) {
			
			e.printStackTrace();
		}
		
	}
	
	private List<IRunResult> getRuns(String requestor, Instant to, Instant from, String testName) throws ResultArchiveStoreException {
		
		List<IRunResult> runs = new ArrayList<>();
		
		RasSearchCriteriaRequestor requestorCrit = new RasSearchCriteriaRequestor(requestor);
		RasSearchCriteriaQueuedTo toCrit = new RasSearchCriteriaQueuedTo(to);
		RasSearchCriteriaQueuedFrom fromCrit = new RasSearchCriteriaQueuedFrom(to);
		RasSearchCriteriaTestName testNameCrit = new RasSearchCriteriaTestName(testName);
		
		IRasSearchCriteria[] criteria = {requestorCrit, toCrit, fromCrit, testNameCrit};
		
		for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
	    		
	    		runs.addAll(directoryService.getRuns(criteria));
	    		
	    	}
		
		return runs;
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
	
	
}
