/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.teststructure.TestStructure;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.*;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.ras.internal.mocks.*;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockPath;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.nio.file.Path;

public class TestRunArtifactsListServlet extends BaseServletTest {	

	private MockFileSystem mockFileSystem;
	/** 
	 * A subclass of the servlet we want to test, so that we can inject the mock framework in without 
	 * adding any extra code to the production servlet class. The framework field is protected scope, 
	 * so a subclass can do the injection instead of the injection framework.
	 */
	class MockRasRunServlet extends BaseServlet implements IServletUnderTest {

		@Override
		public void setFramework(IFramework framework) {
			super.framework = framework;
		}

		@Override
		public void setFileSystem(IFileSystem fileSystem) {
			super.fileSystem = fileSystem;
		}
	}

    class MockRasRunServletEnvironment extends MockServletBaseEnvironment {

		public MockRasRunServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest){ 
        	super(mockInpResults, mockRequest, new MockResultArchiveStoreDirectoryService(mockInpResults));
    	}

		public MockRasRunServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockFileSystem mockFileSystem ){ 
			super(mockInpResults, mockRequest, mockFileSystem);
		}

		public BaseServlet getServlet() {
			return super.getBaseServlet();
		}

		@Override
		public IServletUnderTest createServlet() {
        	return new MockRasRunServlet();
    	}
	}

	public List<IRunResult> generateTestData (String runId, String runName) {
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();

		// Build the results the DB will return.
		String requestor = RandomStringUtils.randomAlphanumeric(8);
		
		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		testStructure.setResult("Passed");

		Path artifactRoot = new MockPath("/" + runName + "/artifacts",mockFileSystem);
		String log = RandomStringUtils.randomAlphanumeric(6);
		IRunResult result = new MockRunResult( runId, testStructure, artifactRoot , log);
		mockInputRunResults.add(result);

		return mockInputRunResults;
	}

	public String generateExpectedJson (String runId, List<Path> artifactPaths) {
		String jsonResult = "[\n";
		int numOfArtifacts = artifactPaths.size();
		if (numOfArtifacts > 0) {
			
			for (int i = 0; i < numOfArtifacts; i++ ) {
				String runData = "";
				if (0 < i && i < numOfArtifacts) {
					runData = ",\n";
				}
				runData += "  {\n"+
					   "    \"runId\": \""+runId+"\",\n"+
					   "    \"path\": \""+artifactPaths.get(i).toString()+"\",\n"+
					   "    \"url\": \""+artifactPaths.get(i).toString()+"\"\n"+
					   "  }";
				jsonResult += runData;
			}
		}
		jsonResult += "\n]";
		return jsonResult;
	}

	@Before
	public void setUp() {
		mockFileSystem = new MockFileSystem();
	}

    @Test
	public void testMultipleArtifactsToListReturnsOKWithArtifacts() throws Exception {
		//Given..
		String runName = "testA";
		MockPath mockArtifactsPath = new MockPath("/" + runName + "/artifacts",mockFileSystem);
		List<Path> dummyArtifactPaths = Arrays.asList(
			new MockPath(mockArtifactsPath + "/dummyB.gz",mockFileSystem),
			new MockPath(mockArtifactsPath + "/dummyC.txt",mockFileSystem),
			new MockPath(mockArtifactsPath + "/dummyA.json",mockFileSystem)
		);

		mockFileSystem.createDirectories(mockArtifactsPath);
		for (Path artifactPath : dummyArtifactPaths) {
			mockFileSystem.createFile(artifactPath);
		}
		
		String runId = "xxxxx678xxxxx";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName);

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		parameterMap.put("runId", new String[] {runId} );

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/run/" + runId + "/artifacts");
		MockRasRunServletEnvironment mockServletEnvironment = new MockRasRunServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// [
		//	 {
		//     "runId": "xxxxx678xxxxx",
		//     "path": "/testA/artifacts/dummyA.gz",
		//     "url": "/testA/artifacts/dummyA.gz",
		//   },
		//	 {
		//     "runId": "xxxxx678xxxxx",
		//     "path": "/testA/artifacts/dummyB.gz",
		//     "url": "/testA/artifacts/dummyB.gz",
		//   },
		//	 {
		//     "runId": "xxxxx678xxxxx",
		//     "path": "/testA/artifacts/dummyC.gz",
		//     "url": "/testA/artifacts/dummyC.gz",
		//   },
		// ]
		assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(3);

		String expectedJson = generateExpectedJson(runId, dummyArtifactPaths);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
	
		assertThat(resp.getContentType()).isEqualTo("Application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testOneArtifactToListReturnsOKWithArtifact() throws Exception {
		//Given..
		String runName = "testA";
		MockPath mockArtifactsPath = new MockPath("/" + runName + "/artifacts",mockFileSystem);
		MockPath dummyArtifactPath = new MockPath(mockArtifactsPath + "/dummy.gz",mockFileSystem);
		mockFileSystem.createDirectories(mockArtifactsPath);
		mockFileSystem.createFile(dummyArtifactPath);

		String runId = "xxxxx678xxxxx";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/run/" + runId + "/artifacts");
		MockRasRunServletEnvironment mockServletEnvironment = new MockRasRunServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// [
		//	 {
		//     "runId": "xxxxx678xxxxx",
		//     "path": "/testA/artifacts/dummy.gz",
		//     "url": "/testA/artifacts/dummy.gz",
		//   },
		// ]
		// assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(1);

		String expectedJson = generateExpectedJson(runId, Arrays.asList(dummyArtifactPath));
		assertThat(outStream.toString()).isEqualTo(expectedJson);
	
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}


    @Test
	public void testNoArtifactsToListGivesEmptyList() throws Exception {
		//Given..
		String runName = "testA";
		MockFileSystem mockFileSystem = new MockFileSystem();
		MockPath mockArtifactsPath = new MockPath("/" + runName + "/artifacts",mockFileSystem);
		mockFileSystem.createDirectories(mockArtifactsPath);

		String runId = "xxxxx678xxxxx";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/run/" + runId + "/artifacts");
		MockRasRunServletEnvironment mockServletEnvironment = new MockRasRunServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// []
		assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(0);
	
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testBadRunIdGivesNotFoundError() throws Exception {
		//Given..
		String runId = "badRunId";
		List<IRunResult> mockInputRunResults = new ArrayList<>();

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/run/" + runId + "/artifacts");
		MockRasRunServletEnvironment mockServletEnvironment = new MockRasRunServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);
		
		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5002,
		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'badRunId'.""
		// }
		assertThat(resp.getStatus()).isEqualTo(404);
		checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", "badRunId" );
	
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}
}