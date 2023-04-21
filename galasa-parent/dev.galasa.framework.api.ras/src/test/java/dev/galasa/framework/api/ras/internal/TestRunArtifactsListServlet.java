/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.*;

import java.io.PrintWriter;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.framework.api.ras.internal.mocks.*;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.nio.file.Path;
import java.nio.file.Paths;

public class TestRunArtifactsListServlet extends BaseServletTest {	


	/** 
	 * A subclass of the servlet we want to test, so that we can inject the mock framework in without 
	 * adding any extra code to the production servlet class. The framework field is protected scope, 
	 * so a subclass can do the injection instead of the injection framework.
	 */
	class MockRunArtifactListServlet extends RunArtifactListServlet implements IServletUnderTest {
		@Override
		public void setFramework(IFramework framework) {
			super.framework = framework;
		}
	}

    class MockArtifactListServletEnvironment extends MockServletBaseEnvironment {

		public MockArtifactListServletEnvironment(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap){ 
        	super(mockInpResults, parameterMap, new MockResultArchiveStoreDirectoryService(mockInpResults));
    	}

		public MockArtifactListServletEnvironment(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap, MockResultArchiveStoreDirectoryService rasStore ){ 
			super(mockInpResults, parameterMap, rasStore);
		}

		public RunArtifactListServlet getServlet() {
			return (RunArtifactListServlet) super.getBaseServlet();
		}

		@Override
		public IServletUnderTest createServlet() {
        	return new MockRunArtifactListServlet();
    	}
	}

	@Ignore("Need a mock filesystem implemented, so we can feed it an empty folder to iterate over.")
    @Test
	public void testNoArtifactsToDownloadGivesEmptyList() throws Exception {
		//Given..
		String runId = "xxxxx678xxxxx";
		List<IRunResult> mockInputRunResults = generateTestData(runId);

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		parameterMap.put("runId", new String[] {runId} );

		MockArtifactListServletEnvironment mockServletEnvironment = new MockArtifactListServletEnvironment( mockInputRunResults,parameterMap);

		RunArtifactListServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

		//When...
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// []
		assertThat(resp.getStatus()==200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(0);
	
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}


	public List<IRunResult> generateTestData (String runId ){
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		int passCount = 0;
		// Build the results the DB will return.

		String runName = RandomStringUtils.randomAlphanumeric(5);
		String requestor = RandomStringUtils.randomAlphanumeric(8);
		
		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		testStructure.setResult("Passed");

		Path artifactRoot = Paths.get(RandomStringUtils.randomAlphanumeric(12));
		String log = RandomStringUtils.randomAlphanumeric(6);
		IRunResult result = new MockRunResult( runId, testStructure, artifactRoot , log);
		mockInputRunResults.add(result);

		return mockInputRunResults;
	}

	@Test
	public void testBadRunIdGivesNotFoundError() throws Exception {
		//Given..
		String runId = "xxxxx678xxxxx";
		List<IRunResult> mockInputRunResults = generateTestData(runId);

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		parameterMap.put("runId", new String[] {"badRunId"} );

		MockArtifactListServletEnvironment mockServletEnvironment = new MockArtifactListServletEnvironment( mockInputRunResults,parameterMap);

		RunArtifactListServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

		//When...
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5002,
		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'badRunId'.""
		// }
		assertThat(resp.getStatus()==404);
		checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", "badRunId" );
	
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}
}