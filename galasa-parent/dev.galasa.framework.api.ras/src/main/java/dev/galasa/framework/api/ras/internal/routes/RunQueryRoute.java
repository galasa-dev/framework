/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.routes;

import org.apache.commons.collections4.ListUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.api.ras.internal.verycommon.InternalServletException;
import dev.galasa.framework.api.ras.internal.verycommon.QueryParameters;
import dev.galasa.framework.api.ras.internal.verycommon.ResponseBuilder;
import dev.galasa.framework.api.ras.internal.verycommon.ServletError;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.api.ras.internal.common.RasQueryParameters;
import dev.galasa.framework.api.ras.internal.common.RunResultUtility;
import dev.galasa.framework.spi.FrameworkException;
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
import dev.galasa.framework.spi.ras.RasSearchCriteriaStatus;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.verycommon.ServletErrorMessage.*;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
/*
 * Implementation to query the ecosystem for a set of runs that match the default or supplied criteria
 */
public class RunQueryRoute extends RunsRoute {

	public RunQueryRoute(ResponseBuilder responseBuilder, IFramework framework) {
		/* Regex to match endpoints: 
		*  -> /ras/runs
		*  -> /ras/runs/
		*  -> /ras/runs?{querystring} 
		*/
		super(responseBuilder, "\\/runs\\/?", framework);

	}

	final static Gson gson = GalasaGsonBuilder.build();

	@Override
	public HttpServletResponse handleRequest(String pathInfo, QueryParameters generalQueryParams, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
		RasQueryParameters queryParams = new RasQueryParameters(generalQueryParams);
		String outputString = retrieveResults(queryParams);
		return getResponseBuilder().buildResponse(res, "application/json", outputString, HttpServletResponse.SC_OK); 
	}

	protected String retrieveResults( 
		RasQueryParameters queryParams
	) throws InternalServletException {

		int pageNum = queryParams.getPageNumber();
		int pageSize = queryParams.getPageSize();

		List<RasRunResult> runs = new ArrayList<>();

		/* Get list of Run Ids from the URL -
		If a Run ID parameter list is present in the URL then only return that run / those runs
		Do not filter as well */
		
		List<String> runIds = queryParams.getRunIds();

		if (runIds != null && runIds.size() > 0) {
			
			
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
	
			List<IRasSearchCriteria> critList = getCriteria(queryParams);

			try {
				runs = getRuns(critList);
			} catch (Exception e) {
				ServletError error = new ServletError(GAL5003_ERROR_RETRIEVING_RUNS);
				throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}

		runs = sortResults(runs, queryParams, queryParams.getSortValue("to:desc"));

		String responseBodyJson = buildResponseBody(runs,pageNum,pageSize);

		return responseBodyJson;
	}

	private List<IRasSearchCriteria> getCriteria(RasQueryParameters queryParams) throws InternalServletException {

		String requestor = queryParams.getRequestor();
		String testName = queryParams.getTestName();
		String bundle = queryParams.getBundle();
		List<String> result = queryParams.getResultsFromParameters(getResultNames());
		List<TestRunLifecycleStatus> status = queryParams.getStatusesFromParameters();
		String runName = queryParams.getRunName();

		Instant to = queryParams.getToTime();

		Instant noQueryFrom = Instant.now().minus(24,ChronoUnit.HOURS);
		// from will error if no runname is specified as it is a mandatory field
		Instant from = getWorkingFromValue(queryParams, noQueryFrom);

		List<IRasSearchCriteria> criteria = getCriteria(requestor,testName,bundle,result,status,to, from, runName);
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
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}	
		}
		return json;
	}

	private List<IRasSearchCriteria> getCriteria(
		String requestor,
		String testName,
		String bundle,
		List<String> result,
		List<TestRunLifecycleStatus> passedInStatuses,
		Instant to, 
		Instant from, 
		String runName
	) throws InternalServletException {

		List<IRasSearchCriteria> critList = new ArrayList<>();   

		if (from != null) {	
			RasSearchCriteriaQueuedFrom fromCriteria = new RasSearchCriteriaQueuedFrom(from); 
			critList.add(fromCriteria);
		}    
		
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
			RasSearchCriteriaResult resultCriteria = new RasSearchCriteriaResult(result.toArray(new String[0]));
			critList.add(resultCriteria);
		}
		if (passedInStatuses != null && !passedInStatuses.isEmpty()){
			RasSearchCriteriaStatus statusCriteria = new RasSearchCriteriaStatus(passedInStatuses);
			critList.add(statusCriteria);
		}
		if (runName != null && !runName.isEmpty()) {
			RasSearchCriteriaRunName runNameCriteria = new RasSearchCriteriaRunName(runName);
			critList.add(runNameCriteria);
		}
		return critList;
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
		for (IResultArchiveStoreDirectoryService directoryService : getFramework().getResultArchiveStore().getDirectoryServices()) {
			runs.addAll(directoryService.getRuns(criteria));
		}

		// Convert each result to the required format
		List<RasRunResult> runResults = new ArrayList<>();
		for(IRunResult run : runs) {
			runResults.add(RunResultUtility.toRunResult(run, true));
		}

		return runResults;
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
		RasQueryParameters queryParams,
		String sortValue
	) throws InternalServletException {

		// shallow-clone the input list so we don't change it.
		List<RasRunResult> runs = new ArrayList<RasRunResult>();
		runs.addAll(unsortedRuns);
		
		Collections.sort(runs, Comparator.nullsLast(Comparator.nullsLast(new SortByEndTime())));

		// Checking ascending or descending for sorting
		return sortData(runs, queryParams, sortValue);
		
	}

	public List<RasRunResult> sortData(List<RasRunResult> runs, RasQueryParameters queryParams, @NotNull String sortValue) throws InternalServletException {

		if (queryParams.validateSortValue() || !queryParams.validateSortValue()){
			if (sortValue.toLowerCase().startsWith("to") ) {
				boolean isAscending = queryParams.isAscending("to");
				if (isAscending) {
					Collections.reverse(runs);
				}
			} else if (sortValue.toLowerCase().startsWith("testclass")) {
				boolean isAscending = queryParams.isAscending("testclass");
				if (isAscending) {
					Collections.sort(runs, new SortByTestClass());
				} else {
					Collections.sort(runs, new SortByTestClass());
					Collections.reverse(runs);   
				}

			} else if (sortValue.toLowerCase().startsWith("result")) {
				boolean isAscending = queryParams.isAscending("result");
				if (isAscending) {
					Collections.sort(runs, new SortByResult());
				} else {
					Collections.sort(runs, new SortByResult());
					Collections.reverse(runs);
				}
			}
		}
		return runs;
	}


	public Instant getWorkingFromValue (RasQueryParameters params , Instant defaultFromTimestamp) throws InternalServletException{
		int querysize = params.getSize();
		Instant from = null ;
		if (querysize > 0){
			if (!params.checkFromTimeOrRunNamePresent()){
				//  RULE: Throw exception because a query exists but no from date has been supplied
				// EXCEPT: When a runname is present in the query
					ServletError error = new ServletError(GAL5010_FROM_DATE_IS_REQUIRED);
					throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			from = params.getFromTime();
		}else {
			// The default for 'from' is now-24 hours. If no query parameters are specified
			from = defaultFromTimestamp;
		}
		return from;
	}
}