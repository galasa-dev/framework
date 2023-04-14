/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractQuerySort {
	
	/**
	 * Given a map of parameters from the HTTP query, look up all the 
	 * ?sort=xxx and ?sort=xxx:asc ?sort=yyy:desc ?sort=xxx:asc,yyy:desc
	 * 
	 * Create a map which contains simplified information of:
	 * { "xxx" : TRUE, "yyy" : FALSE }
	 */
	public static Map<String, Boolean> extractParameters(Map<String, String[]> query){
		
		Map<String, Boolean> paramMap = new HashMap<>();
    	//checks to see if sort is valid
    	if(query.containsKey("sort")) {
    		//retrieves parameters
    		String[] params = query.get("sort");
    		if(params != null) {
    			//for the value in the parameters, split at ','
	    		for(String value : params) {
	    			List<String> val = Arrays.asList(value.split(","));
	    			if(val != null) {
	    				for (String item : val) {
		    				if(item.contains(":")) {
		    					List<String> split = Arrays.asList(item.split(":"));
		    					if(split.get(1).equals("desc")) {
		    						paramMap.put(split.get(0), Boolean.FALSE);
		    					}else {
		    						paramMap.put(split.get(0), Boolean.TRUE);
		    					}
	    					}
	    				}
	    			}
	    		}
    		}
    	}
    	
    	return paramMap;
    }
	
	public static Boolean isAscending(Map<String, String[]> query, String param) {
			if(query.containsKey("sort")) {
				if(extractParameters(query).containsKey(param)) {
					return extractParameters(query).get(param);
				};
			}
		return Boolean.TRUE;
	}
}

	
