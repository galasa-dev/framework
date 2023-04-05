/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import org.junit.Test;
import java.io.PrintWriter;

import dev.galasa.framework.api.ras.internal.mocks.*;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestRunQuery {	
	@Test
	public void testQueryWithRequestorNotSortedButNoDBServiceReturnsEmptyJsonArray() throws Exception {
		
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();

		MockArchiveStore archiveStore = new MockArchiveStore(directoryServices);

		MockFramework mockFramework = new MockFramework(archiveStore);

		RunQuery servlet = new RunQuery();
		servlet.framework = (IFramework) mockFramework;

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String[] requestorValues = {"mickey"};
		parameterMap.put("requestor", requestorValues );

		HttpServletRequest req = new MockHttpServletRequest(parameterMap);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(outStream);
			
		HttpServletResponse resp = new MockHttpServletResponse(writer);
		servlet.doGet(req,resp);

		assertThat( outStream.toString() ).contains("[]");
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

}
