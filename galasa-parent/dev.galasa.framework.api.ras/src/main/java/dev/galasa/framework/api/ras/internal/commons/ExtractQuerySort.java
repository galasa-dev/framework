/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.commons;

import javax.servlet.http.HttpServletResponse;

import static dev.galasa.framework.api.ras.internal.commons.ServletErrorMessage.*;

public class ExtractQuerySort {
	
	/**
	 * Check if the sort value contains an asc for ascending or desc for descending
	 * otherwise return null
	 */
		
	public static Boolean isAscending(QueryParameters queryParams, String param) throws InternalServletException{
		if (queryParams.getSingleString("sort", null) != null) {
			String sortValue = queryParams.getSingleString("sort", null);
			if(sortValue.contains(":")) {
				String[] split = sortValue.split(":");
				if (split[0].equals(param)){
					if (split[1].equals("desc")) {
						return false;
					} else if (split[1].equals("asc")){
						return true;
					}else {
						ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED,sortValue);
						throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
					}
				}
				return false;
			}
			ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED,sortValue);
			throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
		}
		return false;
	}
}

