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
"osgi.http.whiteboard.servlet.pattern=/ras/runs" }, name = "Galasa Runs microservice")
public class RunResultsRas extends HttpServlet {
	
	@Reference
	IFramework framework;
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		
		Map<String, String> paramMap = getParameterMap(req);
		
		JsonArray returnArray = new JsonArray();
		
		try {
			
		List<IRunResult> runs = getRuns(paramMap.get("requestor"), Instant.parse(paramMap.get("to")), 
				Instant.parse(paramMap.get("from")), paramMap.get("testname"));
		
		int pageSize = Integer.parseInt(paramMap.get("pageSize").trim()) | 100;
		
		int amountOfRuns = runs.size();
		int index = 0;
		int pageNum = 1;
		
		while(pageSize > amountOfRuns) {
			List<IRunResult> newList = runs.subList(index, runs.size());
			for(IRunResult run : newList) {
				JsonObject returnObj = new JsonObject();
				returnObj.addProperty("pageSize", String.valueOf(pageSize));
				returnObj.addProperty("pageNumber", String.valueOf(pageNum));
			}
		}
		
		
		}catch(Exception e) {
			
			e.printStackTrace();
		}
		
	}
	
	private List<IRunResult> getRuns(String requestor, Instant to, Instant from, String testName) throws ResultArchiveStoreException {
		
		List<IRunResult> runs = new ArrayList<>();
		
		for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
	    		
	    		runs.addAll(directoryService.getRuns(requestor, to, from, testName));
	    		
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
