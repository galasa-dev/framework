/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import dev.galasa.framework.StatusNames.statuses;
import dev.galasa.framework.api.ras.internal.verycommon.InternalServletException;
import dev.galasa.framework.api.ras.internal.verycommon.QueryParameters;
import dev.galasa.framework.api.ras.internal.verycommon.ServletError;
import static dev.galasa.framework.api.ras.internal.verycommon.ServletErrorMessage.*;


public class RasQueryParameters {

	public static final int DEFAULT_PAGE_NUMBER = 1;
	public static final int DEFAULT_NUMBER_RECORDS_PER_PAGE = 100;

    private QueryParameters generalQueryParams ;

    public RasQueryParameters(QueryParameters generalQueryParams) {
        this.generalQueryParams = generalQueryParams;
    }


    // make func to validate status values
    public List<String> getStatusesFromParameters () throws InternalServletException{
		// status values received from the query
		List<String> queryStatuses = generalQueryParams.getMultipleString("status", null);

		List<String> validStatuses = statuses.getAll();
		
		if (queryStatuses != null){
			List<String> returnStatuses = new ArrayList<String>();
			for (String status : queryStatuses){
				String statusLowercase = status.toLowerCase();
				if (validStatuses.contains(statusLowercase)) {
					returnStatuses.add(statusLowercase);
				} else {
					ServletError error = new ServletError(GAL5014_STATUS_NAME_NOT_RECOGNIZED, status, validStatuses.toString());
					throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
				}
				
			}
			return returnStatuses;
		}

		return null;
	}



    public int getPageNumber() throws InternalServletException {
        int pageNumber = generalQueryParams.getSingleInt("page", DEFAULT_PAGE_NUMBER);
        return pageNumber;
    }

    public int getPageSize() throws InternalServletException {
        return generalQueryParams.getSingleInt("size", DEFAULT_NUMBER_RECORDS_PER_PAGE);
    }

    public Instant getToTime() throws InternalServletException {
        return generalQueryParams.getSingleInstant("to", null);
    }

    public Instant getFromTime() throws InternalServletException {
        return generalQueryParams.getSingleInstant("from", null);
    }

    public String getRequestor() throws InternalServletException {
        return generalQueryParams.getSingleString("requestor", null);
    }

    public String getTestName() throws InternalServletException {
        return generalQueryParams.getSingleString("testname", null);
    } 

    public String getBundle() throws InternalServletException {
        return generalQueryParams.getSingleString("bundle", null);
    } 

    public String getRunName() throws InternalServletException {
        return generalQueryParams.getSingleString("runname", null);
    } 

    public String getSortValue() throws InternalServletException {
        return generalQueryParams.getSingleString("sort","to:desc");
    } 

    public List<String> getRunIds( ) {
        return generalQueryParams.getMultipleString("runId", null);
    }

    public boolean checkFromTimeOrRunNamePresent() throws InternalServletException {
        return generalQueryParams.checkAtLeastOneQueryParameterPresent("from", "runname");
    }

    public int getSize(){
        return generalQueryParams.getSize();
    }


    public List<String> getResultsFromParameters (@NotNull List<String> rasResults) throws InternalServletException{
		// Create map for the lowercase values of all results to ensure we can compare accurately
		Map<String,String> resultNames = new HashMap<String,String>();
		for (String result :rasResults){
			resultNames.put(result.toLowerCase(), result);
		}
		// Return the Results from the URL Query
		List<String> queryResults = generalQueryParams.getMultipleString("result", null);
		// Check the results against the map
		if (queryResults != null){
			List<String> returnResults = new ArrayList<String>();
			for (String result: queryResults){
				String matched = resultNames.get(result.toLowerCase());
				if (matched != null) {
					returnResults.add(matched);
				} else {
					ServletError error = new ServletError(GAL5013_RESULT_NAME_NOT_RECOGNIZED, result, rasResults.toString());
					throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
				}
			}
			return returnResults;
		}
		return null;
	}

    public QueryParameters getGeneralQueryParameters() {
        return this.generalQueryParams;
    }

}