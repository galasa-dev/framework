/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.teststructure.TestStructure;

import org.junit.Test;
import java.io.PrintWriter;

import dev.galasa.framework.api.ras.internal.mocks.*;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
public class TestRunQuery {	
	// @Test
	// public void testQueryWithRequestorNotSortedButNoDBServiceReturnsError() throws Exception {
	// 	// Given...

	// 	// Oh no ! There are no directory services which represent a RAS store !
	// 	List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();

	// 	MockArchiveStore archiveStore = new MockArchiveStore(directoryServices);

	// 	MockFramework mockFramework = new MockFramework(archiveStore);

	// 	RunQuery servlet = new RunQuery();
	// 	servlet.framework = (IFramework) mockFramework;

	// 	Map<String, String[]> parameterMap = new HashMap<String,String[]>();

	// 	String[] requestorValues = {"mickey"};
	// 	parameterMap.put("requestor", requestorValues );

	// 	HttpServletRequest req = new MockHttpServletRequest(parameterMap);

	// 	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	// 	PrintWriter writer = new PrintWriter(outStream);
			
	// 	HttpServletResponse resp = new MockHttpServletResponse(writer);

	// 	// When...
	// 	servlet.doGet(req,resp);

	// 	// Then...
	// 	// We expect an error back, because the API server couldn't find any RAS database to query
	// 	assertThat( outStream.toString() ).contains("error");
	// 	assertThat( resp.getContentType()).isEqualTo("Application/json");
	// 	assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	// }


	@Test
	public void testQueryWithRequestorNotSortedWithEmptyDBServiceReturnsOK() throws Exception {
		// Given...
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();

		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();

		MockResultArchiveStoreDirectoryService mockArchiveStoreDirServier = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		directoryServices.add(mockArchiveStoreDirServier);

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
		
		// When...
		servlet.doGet(req,resp);


		// Expecting:
		// [
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 0,
		//   "runs": [
		// 	 ]
		//   }
		// ]

		assertThat( outStream.toString() )
			.contains("\"pageNum\": 1,")
			.contains("\"pageSize\": 100,")
			.contains("\"numPages\": 1,")
			.contains("\"amountOfRuns\": 0,")
			.contains("\"runs\": []");
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceReturnsOK() throws Exception {
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		
		// Build the results the DB will return.
		String runName = "A1234";
		String requestor = "mickey";
		String runId = "xxx-yyy-zzz-012345";
		
		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		Path artifactRoot = Paths.get("something");
		String log = "a simple log";
		IRunResult result = new MockRunResult( runId, testStructure, artifactRoot , log);
		mockInputRunResults.add(result);

		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();

		MockResultArchiveStoreDirectoryService mockArchiveStoreDirServier = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		directoryServices.add(mockArchiveStoreDirServier);

		MockArchiveStore archiveStore = new MockArchiveStore(directoryServices);

		MockFramework mockFramework = new MockFramework(archiveStore);

		RunQuery servlet = new RunQuery();
		servlet.framework = (IFramework) mockFramework;

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String[] requestorValues = {requestor};
		parameterMap.put("requestor", requestorValues );

		HttpServletRequest req = new MockHttpServletRequest(parameterMap);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(outStream);
			
		HttpServletResponse resp = new MockHttpServletResponse(writer);
		servlet.doGet(req,resp);


		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 1,
		//   "runs": [
		//     {
		//       "runId": "xxx-yyy-zzz-012345",
		//       "testStructure": {
		//         "runName": "A1234",
		//         "requestor": "mickey"
		//       }
		// 	   }
		// 	]
		// }
		assertThat( outStream.toString() )
			.contains("\"pageNum\": 1,")
			.contains("\"pageSize\": 100,")
			.contains("\"numPages\": 1,")
			.contains("\"amountOfRuns\": "+mockInputRunResults.size()+",")
			.contains("\"runId\": \""+runId+"\"")
			.contains("\"runName\": \""+runName+"\"")
			.contains("\"requestor\": \""+requestor+"\"");
			
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}
}

