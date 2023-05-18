/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal.commons;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import javax.servlet.http.HttpServletResponse;

import static dev.galasa.framework.api.ras.internal.commons.ServletErrorMessage.*;
import static javax.servlet.http.HttpServletResponse.*;


/**
 * A wrapper for a map of query parameters, with methods for extracting
 * and verifying the parameter contents.
 */
public class QueryParameters {
    
    Map<String,String[]> params ;

	/**
	 * @param paramMap The query parameters, probably coming from the request.
	 */
    public QueryParameters( Map<String,String[]> paramMap ) {
        this.params = paramMap;
    }
	
	/**
	 * 
	 * @param queryParameterName The query parameter
	 * @param defaultValue
	 * @return
	 * @throws InternalServletException when there are multiple instances of the key, with error GAL5006
	 */
    public String getSingleString(String queryParameterName, String defaultValue ) throws InternalServletException {
		String returnedValue = defaultValue ;
		String[] paramValues = params.get(queryParameterName);
		if (paramValues != null && paramValues.length >0) {

			if (paramValues.length >1) {
				ServletError error = new ServletError(
					GAL5006_INVALID_QUERY_PARAM_DUPLICATES,
					queryParameterName);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}

			String firstOccurrance = paramValues[0];
			if( firstOccurrance != null ) {
				String trimmedFirstOccurrance = firstOccurrance.trim();
				if ( !(trimmedFirstOccurrance.isEmpty())) {
					returnedValue = trimmedFirstOccurrance;
				}
			}
		}
		return returnedValue;
	}

	// /**
	//  * 
	//  * @param queryParameterName The query parameter
	//  * @param defaultValues
	//  * @return
	//  */
    public List<String> getMultipleString(String queryParameterName, List<String> defaultValues ) {
		List<String> returnedValues = defaultValues ;
		String[] paramValues = params.get(queryParameterName);
		if (paramValues != null && paramValues.length > 0) {
			returnedValues = Arrays.asList(paramValues[0].split(","));
		}

		return returnedValues;
	}


	/**
	 * @param queryParameterName
	 * @param defaultValue Returned if there are no occurrances of the query parameter.
	 * @return
	 * @throws InternalServletException when there are multiple values of this query parameter, or
	 * when the value of the query parameter is not a valid integer.
	 */
    public int getSingleInt(String queryParameterName, int defaultValue) throws InternalServletException {
		int returnedValue = defaultValue ;
		String paramValueStr = getSingleString(queryParameterName, Integer.toString(defaultValue));
		
		if (paramValueStr != null ) {
			try {
				returnedValue = Integer.parseInt(paramValueStr.trim());
			} catch (NumberFormatException ex) {
				
				ServletError error = new ServletError(
					GAL5005_INVALID_QUERY_PARAM_NOT_INTEGER,
					queryParameterName,paramValueStr);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		return returnedValue;
	}

	/**
	 * @param queryParameterName
	 * @param defaultValue Returned if there are no occurrances of the query parameter.
	 * @return
	 * @throws InternalServletException when there are multiple values of this query parameter, or
	 * when the value of the query parameter is not a valid date-time.
	 */
    public Instant getSingleInstant(String queryParameterName, Instant defaultValue) throws InternalServletException {
		Instant returnedValue = defaultValue ;
		String paramValueStr = getSingleString(queryParameterName, null);
		
		if (paramValueStr != null ) {
			try {
				returnedValue = Instant.parse(paramValueStr.trim());
			} catch (DateTimeParseException ex) {
				ServletError error = new ServletError(
					GAL5001_INVALID_DATE_TIME_FIELD,
					queryParameterName,paramValueStr
				);
				throw new InternalServletException(error, SC_BAD_REQUEST);
			}
		}
		return returnedValue;
	}

	public Instant getSingleInstantIfParameterNotPresent(String queryParameterName , String parameterToTest) throws InternalServletException {
		// This function will return null when neither of the two parameters provided are in the query
		String paramTestValueStr = getSingleString(parameterToTest, null);
		Instant result = getSingleInstant(queryParameterName, null);
		if (paramTestValueStr == null && result ==null) {
			return null;
		}
		return result;
	}
		
	public Instant getDefaultFromInstantIfNoQueryIsPresent () throws InternalServletException{
		// The default for 'from' is now-24 hours. If no query parameters are specified
		Integer querysize = this.params.size();
		Instant from = null ;
		if (querysize >= 0){
			from = getSingleInstantIfParameterNotPresent("from", "runname");
			//Check to see if there is no query (i.e. hit the /ras/runs/ endpoint)
			if (from == null && querysize == 0){
				Instant fromDefault = Instant.now().minus(24, ChronoUnit.HOURS);
				from = getSingleInstant("from", fromDefault);
			}else if (from == null){
				/*  RULE: Throw exception because a query exists but no from date has been supplied
				* EXCEPT: When a runname is present in the query
				*/
				ServletError error = new ServletError(GAL5010_FROM_DATE_IS_REQUIRED);
				throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		return from;
	}
}
