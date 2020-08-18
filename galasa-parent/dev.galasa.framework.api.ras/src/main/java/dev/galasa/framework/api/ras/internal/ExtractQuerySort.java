package dev.galasa.framework.api.ras.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractQuerySort {
	

	
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
	    					List<String> split = Arrays.asList(item.split(":"));
	    					if(split.get(1).equals("desc")) {
	    						paramMap.put(split.get(0), false);
	    					}else {
	    						paramMap.put(split.get(0), true);
	    					}
	    				}
	    			}
	    		}
    		}
    	}
    	return paramMap;
    	
    }
	
	public static Boolean isAscending(Map<String, String[]> query, String param) {
		
		if(extractParameters(query).get(param) == false) {
			return false;
		}
		
		return true;
	}
}
	
