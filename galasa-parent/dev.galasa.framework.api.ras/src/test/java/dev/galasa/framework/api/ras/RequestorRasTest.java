package dev.galasa.framework.api.ras;
import java.util.HashMap;
import java.util.Map;
import dev.galasa.framework.api.ras.internal.ExtractQuerySort;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class RequestorRasTest {
	
	@Test
	public void testWithParameterThatIsNull() {
		Map<String, String[]> map = new HashMap<>();
		
		String[] stringArr = {"requestor:asc"};
		
		map.put("sort", stringArr);
		
		assertTrue(ExtractQuerySort.isAscending(map, "jim"));
	}
	
	@Test
	public void testWithParameterThatIsNotNull() {
		
		Map<String, String[]> map = new HashMap<>();
		
		String[] stringArr = {"requestor:desc"};
		
		map.put("sort", stringArr);
		
		assertFalse(ExtractQuerySort.isAscending(map, "requestor"));
		
	}

}
