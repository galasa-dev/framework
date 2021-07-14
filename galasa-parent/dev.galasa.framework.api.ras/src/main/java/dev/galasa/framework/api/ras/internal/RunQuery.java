package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import org.apache.commons.collections4.ListUtils;

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


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		int pageNum = 1;
		int pageSize = 100;

		Map<String,String> paramMap = getParameterMap(req);

		if (paramMap.get("page") != null && !paramMap.get("page").equals("")) {
			try {
				pageNum = Integer.parseInt(paramMap.get("page"));
			} catch (Exception e) {
				throw new ServletException("Error parsing integer, ",e);
			}
		}

		if (paramMap.get("size") != null && !paramMap.get("size").equals("")) {
			try {
				pageSize = Integer.parseInt(paramMap.get("size"));
			} catch (Exception e) {
				throw new ServletException("Error parsing integer, ", e);
			}
		}

		List<RasRunResult> runs = new ArrayList<>();

		/* Get list of Run Ids from the URL -
		If a Run ID parameterlist is present in the URL then only return that run / those runs
		Do not filter as well */

		String runIdsParam = "";
		if (paramMap.get("runId") != null && !paramMap.get("runId").isEmpty()) {
			runIdsParam = paramMap.get("runId");
			
			String [] runIds = runIdsParam.split("[,]");
			for (String runId : runIds) {
				try {
					IRunResult run = getRunByRunId(runId);
					runs.add(RunResultUtility.toRunResult(run, true));
				} catch (ResultArchiveStoreException e) {
					throw new ServletException("Error retrieving run " + runId, e);
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
			
			// Checking all parameters to apply to the search criteria

			try {
				if (to != null && !to.isEmpty()) {
					Instant toCrit = Instant.parse(to);
					RasSearchCriteriaQueuedTo toCriteria = new RasSearchCriteriaQueuedTo(toCrit);
					critList.add(toCriteria);
				}

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
				throw new ServletException("Error parsing Instant, ", e);
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


			try {
				runs = getRuns(critList);
			} catch (Exception e) {
				throw new ServletException("Error retrieving runs, ", e);
			}
		}


		Collections.sort(runs, Comparator.nullsLast(Comparator.nullsLast(new SortByEndTime())));

		Map<String, String[]> query = req.getParameterMap();

		// Checking ascending or descending for sorting

		boolean testClassSort = ExtractQuerySort.isAscending(query,"testclass");
		boolean resultSort = ExtractQuerySort.isAscending(query, "result");

		if (!query.isEmpty()) {
			if (!ExtractQuerySort.isAscending(query, "to")) {
				Collections.reverse(runs);
			} else if (paramMap.get("sort").equals("testclass:asc") && testClassSort) {
				Collections.sort(runs, new SortByTestClass());
			} else if (!testClassSort) {
				Collections.sort(runs, new SortByTestClass());
				Collections.reverse(runs);   
			} else if (paramMap.get("sort").equals("result:asc") && resultSort) {
				Collections.sort(runs, new SortByResult());
			} else if (!resultSort) {
				Collections.sort(runs, new SortByResult());
				Collections.reverse(runs);
			}
		}


		List<JsonObject> returnArray = new ArrayList<>();

		//Splits up the pages based on the page size

		List<List<RasRunResult>> runList = ListUtils.partition(runs, pageSize);

		int numPages = runList.size();

		int pageIndex = 1;

		//Building the object to be returned by the API and splitting

		if (runList != null) {
			for(List<RasRunResult> list : runList) {

				JsonObject obj = new JsonObject();

				obj.addProperty("pageNum", pageIndex);
				obj.addProperty("pageSize", pageSize);
				obj.addProperty("numPages", numPages);
				obj.addProperty("amountOfRuns", runs.size());

				JsonElement tree = gson.toJsonTree(list);

				obj.add("runs", tree);

				returnArray.add(obj);

				pageIndex+=1;
			}
		}

		String json = "";

		if (returnArray.size() != 0) {
			try {
				json = gson.toJson(returnArray.get(pageNum-1));
			} catch (Exception e) {
				throw new ServletException("Error retrieving page, ", e);
			}	
		}

		try {
			PrintWriter out = resp.getWriter();
			resp.setContentType( "Application/json");
			resp.addHeader("Access-Control-Allow-Origin", "*");
			out.print(json);
			out.close();
		} catch (Exception e) {
			throw new ServletException("An error occurred whilst retrieving runs", e);
		}
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
}