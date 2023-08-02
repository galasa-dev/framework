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

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.api.ras.internal.verycommon.InternalServletException;
import dev.galasa.framework.api.ras.internal.verycommon.QueryParameters;
import dev.galasa.framework.api.ras.internal.verycommon.ServletError;
import static dev.galasa.framework.api.ras.internal.verycommon.ServletErrorMessage.*;


public class RasQueryParameters {

	public static final int DEFAULT_PAGE_NUMBER = 1;
	public static final int DEFAULT_NUMBER_RECORDS_PER_PAGE = 100;
    private final Map<String, Boolean> sortDirectionMap = new HashMap<String,Boolean>(){{
        put("asc",true);
        put("ascending",true);
        put("desc",false);
        put("descending",false);
    }};

    private QueryParameters generalQueryParams ;

    public RasQueryParameters(QueryParameters generalQueryParams) {
        this.generalQueryParams = generalQueryParams;
    }


    // make func to validate status values
    public List<TestRunLifecycleStatus> getStatusesFromParameters () throws InternalServletException{
		// status values received from the query
		List<String> queryStatuses = generalQueryParams.getMultipleString("status", null);
		
		if (queryStatuses != null){
			List<TestRunLifecycleStatus> returnStatuses = new ArrayList<TestRunLifecycleStatus>();
			for (String status : queryStatuses){
				String statusUppercase = status.toUpperCase();
				if (TestRunLifecycleStatus.isStatusValid(statusUppercase)) {
					returnStatuses.add(TestRunLifecycleStatus.valueOf(statusUppercase));
				} else {
					ServletError error = new ServletError(GAL5014_STATUS_NAME_NOT_RECOGNIZED, status, TestRunLifecycleStatus.getAll().toString());
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

    public boolean isAscending(String fieldToSortBy) throws InternalServletException{
        String sortValue = generalQueryParams.getSingleString("sort", null);
		boolean isAscending = false;
        if (sortValue != null){
            try{
                if(sortIsValidFormat(sortValue)) {
                    isAscending = getSortDirection(fieldToSortBy,sortValue);
                }else{
                throw new Exception();
                }
            }catch (Exception e) {
                ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED,sortValue);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
		return isAscending;
	}

    // note asc = true, desc = false
    private boolean getSortDirection(String fieldToSortBy, String sortValue) {
        boolean isAscending = false;
        String[] split = sortValue.split(":");
            isAscending = sortDirectionMap.get(split[1].toLowerCase());
        return isAscending;
    }

    // check if sort value has right formatting
    private boolean sortIsValidFormat(String sortValue){
        boolean isValid = false;
        if(sortValue.contains(":")) {
            if(sortValue.split(":").length == 2){
                isValid = true;
            }
        }
        return isValid;
    }

    public boolean validateSortValue() throws InternalServletException{
		return isAscending(null);
	}

}