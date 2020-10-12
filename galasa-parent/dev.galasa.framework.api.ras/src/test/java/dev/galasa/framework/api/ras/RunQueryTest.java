package dev.galasa.framework.api.ras;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import org.junit.Test;

import dev.galasa.framework.api.ras.internal.ExtractQuerySort;

public class RunQueryTest {
	
	@Test
	public void testAssertsTrueForToAsc() {
		
		Map<String, String[]> map = new HashMap<>();
		
		String[] stringArr = {"to:asc"};
		
		map.put("sort", stringArr);
		
		assertTrue(ExtractQuerySort.isAscending(map, "to"));
	}
	

	
}
