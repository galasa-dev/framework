package dev.galasa.framework.api.ras;
import java.util.HashMap;
import java.util.Map;
import dev.galasa.framework.api.ras.internal.ExtractQuerySort;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class RequestorRasTest {
	
	@Test
	public void testAssertsTrueWhenParameterDoesntExist() {
		Map<String, String[]> map = new HashMap<>();
		
		String[] stringArr = {"requestor:asc"};
		
		map.put("sort", stringArr);
		
		assertTrue(ExtractQuerySort.isAscending(map, "jim"));
	}
	
	@Test
	public void testAssertsTrueForRequestorAsc() {
		
		Map<String, String[]> map = new HashMap<>();
		
		String[] stringArr = {"requestor:asc"};
		
		map.put("sort", stringArr);
		
		assertTrue(ExtractQuerySort.isAscending(map, "requestor"));
		
	}
	
	@Test
	public void testAssertsFalseForRequestorDesc() {
		
		Map<String, String[]> map = new HashMap<>();
		
		String[] stringArr = {"requestor:desc"};
		
		map.put("sort", stringArr);
		
		assertFalse(ExtractQuerySort.isAscending(map, "requestor"));
		
	}
	
	@Test
	public void testMultipleSortOptions() {
		
		Map<String, String[]> map = new HashMap<>();
		
		String[] stringArr = {"requestor:asc,testclass:asc,bundle:asc,bob:desc"};
		
		map.put("sort", stringArr);
		
		assertTrue(ExtractQuerySort.isAscending(map, "requestor"));
		assertTrue(ExtractQuerySort.isAscending(map, "testclass"));
		assertTrue(ExtractQuerySort.isAscending(map, "bundle"));
		assertTrue(ExtractQuerySort.isAscending(map, "jenny"));
		
		assertFalse(ExtractQuerySort.isAscending(map, "bob"));
	}

}
