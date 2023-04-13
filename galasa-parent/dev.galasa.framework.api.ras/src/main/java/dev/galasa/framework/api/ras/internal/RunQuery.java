/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaBundle;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaResult;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRunName;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	final static Gson gson = GalasaGsonBuilder.build();

	private Log  logger  =  LogFactory.getLog(this.getClass());

	Map<String,String> paramMap ;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		int pageNum = 1;
		int pageSize = 100;

		paramMap= getParameterMap(req);

		pageNum = extractPageProperty(resp, "page", pageNum);
		pageSize = extractPageProperty(resp, "size", pageSize);

		List<RasRunResult> runs = new ArrayList<>();

		/* Get list of Run Ids from the URL -
		If a Run ID parameter list is present in the URL then only return that run / those runs
		Do not filter as well */

		//private List<RasRunResult> (){}

		String runIdsParam = "";
		if (paramMap.get("runId") != null && !paramMap.get("runId").isEmpty()) {
			runIdsParam = paramMap.get("runId");
			
			String [] runIds = runIdsParam.split(",");
			IRunResult run = null;
			for (String runId : runIds) {
				try {
					run = getRunByRunId(runId.trim());
					
					if (run != null) {
						runs.add(RunResultUtility.toRunResult(run, true));
					}
				} catch (ResultArchiveStoreException e) {
					// @@@@@@@@@@
					sendResponse(resp, new ServletError(GAL5002_INVALID_RUN_ID,runId).toString(), 500);
					logger.error(new ServletError(GAL5002_INVALID_RUN_ID,runId).toString(),e);
				}
			}

		} else {

			List<IRasSearchCriteria> critList = new ArrayList<>();       

			String requestor = paramMap.get("requestor");
			String testName = paramMap.get("testname");
			String bundle = paramMap.get("bundle");
			String result = paramMap.get("result");
			String to = paramMap.get("to");
			String from = paramMap.get("from");
			String runName = paramMap.get("runname");
			
			// Checking all parameters to apply to the search criteria

			try {
				if (to != null && !to.isEmpty()) {
					Instant toCrit = Instant.parse(to);
					RasSearchCriteriaQueuedTo toCriteria = new RasSearchCriteriaQueuedTo(toCrit);
					critList.add(toCriteria);
				}
				} catch (Exception e) {
				sendResponse(resp, new ServletError(GAL5001_INVALID_DATE_TIME_FIELD,"to"+to ).toString(), 500);
				logger.error(new ServletError(GAL5001_INVALID_DATE_TIME_FIELD,"to"+to).toString(),e);
			}
			try{
				Instant fromCrit = null;
				if (from != null && !from.isEmpty()) {
					fromCrit = Instant.parse(from);
				} else {
					fromCrit = Instant.now();
					fromCrit = fromCrit.minus(24, ChronoUnit.HOURS);
				}
				RasSearchCriteriaQueuedFrom fromCriteria = new RasSearchCriteriaQueuedFrom(fromCrit); 
				critList.add(fromCriteria);
 
			} catch (Exception e) {
				sendResponse(resp, new ServletError(GAL5001_INVALID_DATE_TIME_FIELD,"from"+from ).toString(), 500);
				logger.error(new ServletError(GAL5001_INVALID_DATE_TIME_FIELD, "from"+from).toString(),e);
			}

			if (requestor != null && !requestor.isEmpty()) {
				RasSearchCriteriaRequestor requestorCriteria = new RasSearchCriteriaRequestor(requestor);
				critList.add(requestorCriteria);
			}
			if (testName != null && !testName.isEmpty()) {
				RasSearchCriteriaTestName testNameCriteria = new RasSearchCriteriaTestName(testName);
				critList.add(testNameCriteria);
			}
			if (bundle != null && !bundle.isEmpty()) {
				RasSearchCriteriaBundle bundleCriteria = new RasSearchCriteriaBundle(bundle);
				critList.add(bundleCriteria);
			}
			if (result != null && !result.isEmpty()) {
				RasSearchCriteriaResult resultCriteria = new RasSearchCriteriaResult(result);
				critList.add(resultCriteria);
			}
			if (runName != null && !runName.isEmpty()) {
				RasSearchCriteriaRunName runNameCriteria = new RasSearchCriteriaRunName(runName);
				critList.add(runNameCriteria);
			}

			try {
				runs = getRuns(critList);
			} catch (Exception e) {
				sendResponse(resp, new ServletError(GAL5003_ERROR_RETRIEVEING_RUNS).toString(), 500);
				logger.error(new ServletError(GAL5003_ERROR_RETRIEVEING_RUNS).toString(),e);
			}
		}


		runs = sortResults(runs, paramMap , req.getParameterMap());


		List<JsonObject> returnArray = new ArrayList<>();

		//Splits up the pages based on the page size
		List<List<RasRunResult>> paginatedResults = ListUtils.partition(runs, pageSize);

		int numPages = paginatedResults.size();

		int pageIndex = 1;

		//Building the object to be returned by the API and splitting

		if (!paginatedResults.isEmpty()) {
			for(List<RasRunResult> thisPageResults : paginatedResults) {
				JsonObject obj = pageToJson(thisPageResults,runs.size(),pageIndex,pageSize,numPages);
				returnArray.add(obj);
				pageIndex+=1;
			}
		}else{
			// No results at all, so return one page saying that.
			JsonObject obj = pageToJson(runs,runs.size(),pageIndex,pageSize,1);
			returnArray.add(obj);
		}
	
	 

		String json = ""; 

		if (returnArray.isEmpty()) {
			// No items to return, so json list will be empty.
			json = "[]";
		} else {
			try {
				json = gson.toJson(returnArray.get(pageNum-1));
			} catch (Exception e) {
				sendResponse(resp, new ServletError(GAL5004_ERROR_RETRIEVING_PAGE).toString(), 500);
				logger.error(new ServletError(GAL5004_ERROR_RETRIEVING_PAGE).toString(),e);
			}	
		}

		sendResponse(resp, json,200);
	}

	private int extractPageProperty(HttpServletResponse resp, String pageKey, int pageValue){
		if (paramMap.get(pageKey) != null && !paramMap.get(pageKey).equals("")) {
			try {
				pageValue = Integer.parseInt(paramMap.get(pageKey));
				return pageValue;
			} catch (Exception e) {
				sendResponse(resp, new ServletError(GAL5004_ERROR_RETRIEVING_PAGE).toString(), 500);
				logger.error(new ServletError(GAL5004_ERROR_RETRIEVING_PAGE).toString(),e);
			}
		}
		return pageValue;
		
	}	

	public void sendResponse(HttpServletResponse resp , String json , int status){
		//Set headers for HTTP Response
		resp.setStatus(status);
		resp.setContentType( "Application/json");
		resp.addHeader("Access-Control-Allow-Origin", "*");
		try{
			PrintWriter out = resp.getWriter();
			out.print(json);
			out.close();
		}catch(Exception e){
			logger.error("Error trying to set output buffer",e);
		}

	}
	private JsonObject pageToJson(List<RasRunResult> resultsInPage, int totalRuns, int pageIndex, int pageSize, int numPages) {
		JsonObject obj = new JsonObject();

		obj.addProperty("pageNum", pageIndex);
		obj.addProperty("pageSize", pageSize);
		obj.addProperty("numPages", numPages);
		obj.addProperty("amountOfRuns", totalRuns);

		JsonElement tree = gson.toJsonTree(resultsInPage);

		obj.add("runs", tree);
		return obj;
	}
	

	private List<RasRunResult> getRuns(List<IRasSearchCriteria> critList) throws ResultArchiveStoreException {

		List<IRunResult> runs = new ArrayList<>();

		IRasSearchCriteria[] criteria = new IRasSearchCriteria[critList.size()];

		critList.toArray(criteria);

		for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {

			runs.addAll(directoryService.getRuns(criteria));

		}

		List<RasRunResult> runResults = new ArrayList<>();

		for(IRunResult run : runs) {
			runResults.add(RunResultUtility.toRunResult(run, true));
		}

		return runResults;
	}

	private IRunResult getRunByRunId(String id) throws ResultArchiveStoreException {

		IRunResult run = null;

		for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {

			run = directoryService.getRunById(id);

			if(run != null) {
				return run;
			}
		}
		return null;
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


	class SortByEndTime implements Comparator<RasRunResult> {

		@Override
		public int compare(RasRunResult a, RasRunResult b) {
			Instant aEndTime = a.getTestStructure().getEndTime();
			Instant bEndTime = b.getTestStructure().getEndTime();

			if (aEndTime == null) {
				if (bEndTime == null) {
					return 0;
				}
				return -1;
			}
			if (bEndTime == null) {
				return 1;
			}
			return aEndTime.compareTo(bEndTime);
		}
	}

	class SortByTestClass implements Comparator<RasRunResult>{

		@Override
		public int compare(RasRunResult a, RasRunResult b) {
			String aTestClass = a.getTestStructure().getTestShortName();
			String bTestClass = b.getTestStructure().getTestShortName();

			if (aTestClass == null) {
				if (bTestClass == null) {
					return 0;
				}
				return -1;
			}
			if (bTestClass == null) {
				return 1;
			}
			return aTestClass.compareTo(bTestClass);
		}
	}

	class SortByResult implements Comparator<RasRunResult>{

		@Override
		public int compare(RasRunResult a, RasRunResult b) {
			String aResult = a.getTestStructure().getResult();
			String bResult = b.getTestStructure().getResult();

			if (aResult == null) {
				if (bResult == null) {
					return 0;
				}
				return -1;
			}
			if (bResult == null) {
				return 1;
			}
			return aResult.compareTo(bResult);
		}
	}


	public List<RasRunResult> sortResults(
		List<RasRunResult> unsortedRuns,
		Map<String,String> paramMap,
		Map<String,String[]> query
	) {

		// shallow-clone the input list so we don't change it.
		List<RasRunResult> runs = new ArrayList<RasRunResult>();
		runs.addAll(unsortedRuns);
		
		Collections.sort(runs, Comparator.nullsLast(Comparator.nullsLast(new SortByEndTime())));

		// Checking ascending or descending for sorting

		boolean isTestClassSortAscending = ExtractQuerySort.isAscending(query,"testclass");
		boolean isResultSortAscending = ExtractQuerySort.isAscending(query, "result");

		String sortValue = paramMap.get("sort");
		//if (sortValue != null) {
			if (!ExtractQuerySort.isAscending(query, "to")) {
				Collections.reverse(runs);
			} else if (sortValue.equals("testclass:asc") && isTestClassSortAscending) {
				Collections.sort(runs, new SortByTestClass());
			} else if (!isTestClassSortAscending) {
				Collections.sort(runs, new SortByTestClass());
				Collections.reverse(runs);   
			} else if (sortValue.equals("result:asc") && isResultSortAscending) {
				Collections.sort(runs, new SortByResult());
			} else if (!isResultSortAscending) {
				Collections.sort(runs, new SortByResult());
				Collections.reverse(runs);
			}
		//}
		return runs;
	}
}