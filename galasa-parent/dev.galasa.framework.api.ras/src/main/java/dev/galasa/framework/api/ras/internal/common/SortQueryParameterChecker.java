/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.common;

import static dev.galasa.framework.api.ras.internal.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletResponse;

public class SortQueryParameterChecker {
	
	
	/**
	 * Check if the sort value contains an asc for ascending or desc for descending
	 * otherwise return null
	 */
		
	public boolean isAscending(QueryParameters queryParams, String param) throws InternalServletException{
		String sortValue = queryParams.getSingleString("sort", null);
		boolean isAscending = false ;

		if (sortValue != null) {
			boolean isBad = false ;

			if(!sortValue.contains(":")) {
				isBad = true ; // Sort value doesn't contain a ':'
			} else {
				String[] split = sortValue.split(":");
				if (split.length != 2) {
					isBad = true; // Wrong number of parts
				} else {

					String fieldName = split[0].toLowerCase();
					if (fieldName.equals(param) || param == null ){

						String order = split[1].toLowerCase();
						if (order.equals("desc")) {
							isAscending = false;
						} else if (order.equals("asc")){
							isAscending = true;
						} else {
							isBad = true; // It's not 'asc' or 'desc'
						}
					}
				}
			
			}

			if (isBad) {
				ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED,sortValue);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}
		} 

		return isAscending;
	}

	public boolean validateSortValue(QueryParameters queryParams) throws InternalServletException{
		return isAscending(queryParams, null);
	}
}

