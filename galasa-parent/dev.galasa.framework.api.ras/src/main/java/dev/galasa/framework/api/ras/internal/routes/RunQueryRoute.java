/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.api.ras.internal.common.RasQueryParameters;
import dev.galasa.framework.api.ras.internal.common.RunResultUtility;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasRunResultPage;
import dev.galasa.framework.spi.ras.RasSearchCriteriaBundle;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaResult;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRunName;
import dev.galasa.framework.spi.ras.RasSearchCriteriaStatus;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;
import dev.galasa.framework.spi.ras.RasSortField;
import dev.galasa.framework.spi.utils.GalasaGson;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*
 * Implementation to query the ecosystem for a set of runs that match the default or supplied criteria
 */
public class RunQueryRoute extends RunsRoute {

	protected static final String path = "\\/runs\\/?";

    // A mapping of sort keys to corresponding test structure fields
    private final Map<String, String> sortKeyMap = Map.of(
        "from", "queued",
        "to", "endTime",
        "testclass", "testName"
    );

	public RunQueryRoute(ResponseBuilder responseBuilder, IFramework framework) {
		/* Regex to match endpoints:
		*  -> /ras/runs
		*  -> /ras/runs/
		*  -> /ras/runs?{querystring}
		*/
		super(responseBuilder, path, framework);

	}

	static final GalasaGson gson = new GalasaGson();

	@Override
	public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters generalQueryParams, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
		RasQueryParameters queryParams = new RasQueryParameters(generalQueryParams);
		String outputString = retrieveResults(queryParams);
		return getResponseBuilder().buildResponse(req, res, "application/json", outputString, HttpServletResponse.SC_OK);
	}

	private String retrieveResults(RasQueryParameters queryParams) throws InternalServletException {

		int pageSize = queryParams.getPageSize();

        String pageCursor = queryParams.getPageCursor();

		List<RasRunResult> runs = new ArrayList<>();

		/* Get list of Run Ids from the URL -
		If a Run ID parameter list is present in the URL then only return that run / those runs
		Do not filter as well */
		List<String> runIds = queryParams.getRunIds();

        // Default to sorting in descending order based on the "queued time" of runs
        RasSortField sortValue = queryParams.getSortValue("from:desc");

        RasRunResultPage runsPage = null;
        String responseJson = null;
        try {
            if (runIds != null && runIds.size() > 0) {
                runs = sortResults(getRunsByIds(runIds), queryParams, sortValue);
                responseJson = buildResponseBody(runs, pageSize, null);
            } else {
                runsPage = getRunsPage(pageCursor, pageSize, formatSortField(sortValue), getCriteria(queryParams));
                responseJson = buildResponseBody(runsPage, pageSize);
            }
        } catch (ResultArchiveStoreException e) {
            ServletError error = new ServletError(GAL5003_ERROR_RETRIEVING_RUNS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);   
        }
        return responseJson;
	}

    private RasSortField formatSortField(RasSortField sortValue) {
        RasSortField sortField = null;
        if (sortValue != null) {
            String sortFieldName = sortValue.getFieldName();
            sortField = new RasSortField(sortFieldName, sortValue.getSortDirection());

            // Some sort keys map to different test structure fields (e.g. "from" maps to "queued"),
            // so make sure we are sorting by the correct test structure field
            String testStructureFieldName = sortKeyMap.get(sortFieldName);
            if (testStructureFieldName != null) {
                sortField.setFieldName(testStructureFieldName);
            }
        }
        return sortField;
    }

    private List<RasRunResult> getRunsByIds(List<String> runIds) throws InternalServletException {
        List<RasRunResult> runs = new ArrayList<>();

        for (String runId : runIds) {
            try {
                IRunResult run = getRunByRunId(runId.trim());

                if (run != null) {
                    runs.add(RunResultUtility.toRunResult(run, true));
                }
            } catch (ResultArchiveStoreException e) {
                ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
            }
        }
        return runs;
    }

	private List<IRasSearchCriteria> getCriteria(RasQueryParameters queryParams) throws InternalServletException {

		String requestor = queryParams.getRequestor();
		String testName = queryParams.getTestName();
		String bundle = queryParams.getBundle();
		List<String> result = queryParams.getResultsFromParameters(getResultNames());
		List<TestRunLifecycleStatus> status = queryParams.getStatusesFromParameters();
		String runName = queryParams.getRunName();

		Instant to = queryParams.getToTime();

		Instant defaultFromTime = Instant.now().minus(24,ChronoUnit.HOURS);
		// from will error if no runname is specified as it is a mandatory field
		Instant from = getQueriedFromTime(queryParams, defaultFromTime);

		List<IRasSearchCriteria> criteria = getCriteria(requestor,testName,bundle,result,status,to, from, runName);
		return criteria ;
	}

	private String buildResponseBody(List<RasRunResult> runs, int pageSize, String nextPageCursor) {
        JsonObject pageJson = new JsonObject();

        JsonElement tree = gson.toJsonTree(runs);
        pageJson.addProperty("pageSize", pageSize);
        pageJson.addProperty("amountOfRuns", runs.size());
        pageJson.addProperty("nextCursor", nextPageCursor);
        pageJson.add("runs", tree);

        return gson.toJson(pageJson);
	}

	private String buildResponseBody(RasRunResultPage runsPage, int pageSize) throws ResultArchiveStoreException {
        List<RasRunResult> runs = convertRunsToRunResults(runsPage.getRuns());
        return buildResponseBody(runs, pageSize, runsPage.getNextCursor());
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

	private RasRunResultPage getRunsPage(String pageCursor, int maxResults, RasSortField primarySort, List<IRasSearchCriteria> critList) throws ResultArchiveStoreException {

		IRasSearchCriteria[] criteria = new IRasSearchCriteria[critList.size()];

		critList.toArray(criteria);
        
		// Collect all the runs from all the RAS stores into a single list
		List<IRunResult> runs = new ArrayList<>();
        String nextCursor = null;
		for (IResultArchiveStoreDirectoryService directoryService : getFramework().getResultArchiveStore().getDirectoryServices()) {
            RasRunResultPage runsPage = directoryService.getRunsPage(maxResults, primarySort, pageCursor, criteria);
			runs.addAll(runsPage.getRuns());
            
            String nextRunsToken = runsPage.getNextCursor();
            if (nextRunsToken != null) {
                nextCursor = nextRunsToken;
            }
		}

		return new RasRunResultPage(runs, nextCursor);
	}

    private List<RasRunResult> convertRunsToRunResults(List<IRunResult> runs) throws ResultArchiveStoreException {
        // Convert each result to the required format
        List<RasRunResult> runResults = new ArrayList<>();
        for (IRunResult run : runs) {
            runResults.add(RunResultUtility.toRunResult(run, true));
        }
        return runResults;
    }

	class SortByQueuedTime implements Comparator<RasRunResult> {

		@Override
		public int compare(RasRunResult a, RasRunResult b) {
			Instant aQueuedTime = a.getTestStructure().getQueued();
			Instant bQueuedTime = b.getTestStructure().getQueued();

			if (aQueuedTime == null) {
				if (bQueuedTime == null) {
					return 0;
				}
				return -1;
			}
			if (bQueuedTime == null) {
				return 1;
			}
			return aQueuedTime.compareTo(bQueuedTime);
		}
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

	private List<RasRunResult> sortResults(
		List<RasRunResult> unsortedRuns,
		RasQueryParameters queryParams,
		RasSortField sortValue
	) throws InternalServletException {

		// shallow-clone the input list so we don't change it.
		List<RasRunResult> runs = new ArrayList<RasRunResult>();
		runs.addAll(unsortedRuns);

        Comparator<RasRunResult> runsComparator = buildRunsComparator(queryParams, sortValue);

        Collections.sort(runs, runsComparator);
        return runs;
	}

    private Comparator<RasRunResult> buildRunsComparator(RasQueryParameters queryParams, RasSortField sortField) throws InternalServletException {
        Comparator<RasRunResult> runsComparator = null;

        String sortFieldName = sortField.getFieldName();
        if (sortFieldName.equals("from")) {
            runsComparator = new SortByQueuedTime();
        } else if (sortFieldName.equals("to")) {
            runsComparator = new SortByEndTime();
        } else if (sortFieldName.equals("testclass")) {
            runsComparator = new SortByTestClass();
        } else if (sortFieldName.equals("result")) {
            runsComparator = new SortByResult();
        } else {
            ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED, sortFieldName);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        // Reverse the comparator if the direction is "desc"
        if (!queryParams.isAscending(sortField)) {
            runsComparator = runsComparator.reversed();
        }

        // Ensure null values appear last
        runsComparator = Comparator.nullsLast(runsComparator);

        return runsComparator;
    }

	Instant getQueriedFromTime(RasQueryParameters params, Instant defaultFromTimestamp) throws InternalServletException {
		int querysize = params.getSize();
		Instant from = defaultFromTimestamp;
		if (querysize > 0) {
			if (!params.isFromTimeOrRunNamePresent()) {
				//  RULE: Throw exception because a query exists but no from date has been supplied
				// EXCEPT: When a runname is present in the query
				ServletError error = new ServletError(GAL5010_FROM_DATE_IS_REQUIRED);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}
			from = params.getFromTime();
		}
		return from;
	}
}