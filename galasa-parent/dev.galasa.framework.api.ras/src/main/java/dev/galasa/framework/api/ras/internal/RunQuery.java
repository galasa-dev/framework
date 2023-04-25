/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.api.ras.RasRunResult;
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

import java.io.PrintWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/ras/run" }, name = "Galasa Runs microservice")
public class RunQuery extends BaseServlet {



	private static final long serialVersionUID = 1L;

	final static Gson gson = GalasaGsonBuilder.build();

	private Log  logger  =  LogFactory.getLog(this.getClass());

	

	public static final int DEFAULT_PAGE_NUMBER = 1;
	public static final int DEFAULT_NUMBER_RECORDS_PER_PAGE = 100;


	@Override
	protected String retrieveResults( 
		Map<String,String[]> rawParamMap
	) throws InternalServletException {

		Map<String,String> paramMap = getParameterMap(rawParamMap);
		
		int pageNum = extractSingleIntProperty(rawParamMap, "page", DEFAULT_PAGE_NUMBER);
		int pageSize = extractSingleIntProperty(rawParamMap, "size", DEFAULT_NUMBER_RECORDS_PER_PAGE);

		List<RasRunResult> runs = new ArrayList<>();

		/* Get list of Run Ids from the URL -
		If a Run ID parameter list is present in the URL then only return that run / those runs
		Do not filter as well */
		
		if (rawParamMap.get("runId") != null && (rawParamMap.get("runId").length >0) ){
			String[] runIds = rawParamMap.get("runId");
			
			IRunResult run = null;
			for (String runId : runIds) {
				try {
					run = getRunByRunId(runId.trim());
					
					if (run != null) {
						runs.add(RunResultUtility.toRunResult(run, true));
					}
				} catch (ResultArchiveStoreException e) {
					ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
					throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
				}
			}

		} else {
	
			List<IRasSearchCriteria> critList = getCriteria(rawParamMap);

			try {
				runs = getRuns(critList);
			} catch (Exception e) {
				ServletError error = new ServletError(GAL5003_ERROR_RETRIEVEING_RUNS);
				throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}

		runs = sortResults(runs, rawParamMap, extractSortValue(paramMap));

		String responseBodyJson = buildResponseBody(runs,pageNum,pageSize);

		return responseBodyJson;
	}

	private List<IRasSearchCriteria> getCriteria( Map<String,String[]> rawParamMap ) throws InternalServletException {

		String requestor = extractSingleStringProperty(rawParamMap,"requestor",null);
		String testName = extractSingleStringProperty(rawParamMap,"testname",null);
		String bundle = extractSingleStringProperty(rawParamMap,"bundle",null);
		String result = extractSingleStringProperty(rawParamMap, "result", null);
		String runName = extractSingleStringProperty(rawParamMap, "runname", null);

		Instant to = extractSingleDateTimeProperty(rawParamMap, "to", null);

		Instant fromDefault = Instant.now();
		fromDefault = fromDefault.minus(24, ChronoUnit.HOURS);
		Instant from = extractSingleDateTimeProperty(rawParamMap, "from", fromDefault);

		List<IRasSearchCriteria> criteria = getCriteria(requestor,testName,bundle,result,to, from, runName);
		return criteria ;
	}

	private String buildResponseBody(List<RasRunResult> runs, int pageNum, int pageSize) throws InternalServletException {

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
				ServletError error = new ServletError(GAL5004_ERROR_RETRIEVING_PAGE);
				throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}	
		}
		return json;
	}

	private List<IRasSearchCriteria> getCriteria(
		String requestor,
		String testName,
		String bundle,
		String result,
		Instant to, 
		@NotNull Instant from, 
		String runName
	) throws InternalServletException {

		List<IRasSearchCriteria> critList = new ArrayList<>();   

		// The default for 'from' is now-24 hours. So will never be null.
		RasSearchCriteriaQueuedFrom fromCriteria = new RasSearchCriteriaQueuedFrom(from); 
		critList.add(fromCriteria);    
		
		// Checking all parameters to apply to the search criteria		
		// The default for 'to' is null.
		if (to != null) {	
			RasSearchCriteriaQueuedTo toCriteria = new RasSearchCriteriaQueuedTo(to);
			critList.add(toCriteria);
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
		return critList;
	}

	private Instant extractSingleDateTimeProperty(Map<String, String[]> paramMap, String key, Instant defaultValue ) throws InternalServletException {
		String[] values = paramMap.get(key);
		Instant dateTime ;
		if (values== null || values.length == 0) {
			dateTime = defaultValue ;
		} else {
			if (values.length > 1){
				ServletError error = new ServletError(GAL5006_INVALID_QUERY_PARAM_DUPLICATES,key);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}
			String firstOccurrance = values[0];
			try {
				dateTime = Instant.parse(firstOccurrance);
			} catch (Exception e) {
				ServletError error = new ServletError(GAL5001_INVALID_DATE_TIME_FIELD,key,firstOccurrance);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		return dateTime;
	}

	private int extractSingleIntProperty( 
		Map<String, String[]> paramMap, 
		String key, 
		int defaultValue 
	) throws InternalServletException {
		
		int returnedValue = defaultValue ;
		String[] paramValuesStr = paramMap.get(key);
		if (paramValuesStr != null &&  paramValuesStr.length > 0){
			if (paramValuesStr.length > 1){
				ServletError error = new ServletError(GAL5006_INVALID_QUERY_PARAM_DUPLICATES,key);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}

			String firstOccurrance = paramValuesStr[0];
			String trimmedFirstOccurrance = firstOccurrance.trim();
			if (!trimmedFirstOccurrance.equals("")){
				try {
					returnedValue = Integer.parseInt(trimmedFirstOccurrance);
				} catch (NumberFormatException e) {
					ServletError error = new ServletError(GAL5005_INVALID_QUERY_PARAM_NOT_INTEGER,key,trimmedFirstOccurrance);
					throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
				}
			}

		}   
		return returnedValue;
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
			logger.error("Error trying to set output buffer. Ignoring.",e);
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

		IRasSearchCriteria[] criteria = new IRasSearchCriteria[critList.size()];

		critList.toArray(criteria);

		// Collect all the runs from all the RAS stores into a single list
		List<IRunResult> runs = new ArrayList<>();
		for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
			runs.addAll(directoryService.getRuns(criteria));
		}

		// Convert each result to the required format
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

	private Map<String, String> getParameterMap(Map<String, String[]> rawParameterMap) {

		Map<String, String> newParameterMap = new HashMap<>();
		for (String parameterName : rawParameterMap.keySet()) {

			String[] values = rawParameterMap.get(parameterName);

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
		
		Map<String,String[]> rawParamMap,
		String sortValue
	) {

		// shallow-clone the input list so we don't change it.
		List<RasRunResult> runs = new ArrayList<RasRunResult>();
		runs.addAll(unsortedRuns);
		
		Collections.sort(runs, Comparator.nullsLast(Comparator.nullsLast(new SortByEndTime())));

		// Checking ascending or descending for sorting
		return sortingData(runs, rawParamMap, sortValue);
		
	}

	public List<RasRunResult> sortingData(List<RasRunResult> runs, Map<String,String[]> query, @NotNull String sortValue){
		
		boolean isTestClassSortAscending = ExtractQuerySort.isAscending(query,"testclass");
		boolean isResultSortAscending = ExtractQuerySort.isAscending(query, "result");

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
		return runs;
	}

	public String extractSortValue (Map<String,String> paramMap){
		String sortValue = paramMap.get("sort");
		if (sortValue == null){
			sortValue = "to:desc";
		}
		return sortValue;
	}
}