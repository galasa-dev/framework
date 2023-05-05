/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.ras.internal.mocks.IServletUnderTest;
import dev.galasa.framework.api.ras.internal.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.ras.internal.mocks.MockResultArchiveStoreDirectoryService;
import dev.galasa.framework.api.ras.internal.mocks.MockRunResult;
import dev.galasa.framework.api.ras.internal.mocks.MockServletBaseEnvironment;
import dev.galasa.framework.mocks.MockPath;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestRunLogRoute extends BaseServletTest {
	
    /** 
	 * A subclass of the servlet we want to test, so that we can inject the mock framework in without 
	 * adding any extra code to the production servlet class. The framework field is protected scope, 
	 * so a subclass can do the injection instead of the injection framework.
	 */
	class MockRunLogServlet extends BaseServlet implements IServletUnderTest {
		@Override
		public void setFramework(IFramework framework) {
			super.framework = framework;
		}

		@Override
		public void setFileSystem(IFileSystem fileSystem) {
		}
	}

	class MockRunLogServletEnvironment extends MockServletBaseEnvironment {

		public MockRunLogServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest){ 
        	super(mockInpResults, mockRequest, new MockResultArchiveStoreDirectoryService(mockInpResults));
    	}

		public MockRunLogServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockResultArchiveStoreDirectoryService rasStore ){ 
			super(mockInpResults, mockRequest, rasStore);
		}

		public BaseServlet getServlet() {
			return super.getBaseServlet();
		}

		@Override
		public IServletUnderTest createServlet() {
        	return new MockRunLogServlet();
    	}
	}

	public List<IRunResult> generateTestData(String runId, String runName, String runLog) {
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();

		// Build the results the DB will return.
		String requestor = RandomStringUtils.randomAlphanumeric(8);
		
		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		testStructure.setResult("Passed");

		Path artifactRoot = new MockPath("/" + runName + "/artifacts",null);
		IRunResult result = new MockRunResult(runId, testStructure, artifactRoot, runLog);
		mockInputRunResults.add(result);

		return mockInputRunResults;
	}

	@Test
	public void testRunResultWithLogReturnsOK() throws Exception {
		//Given..
		String runId = "runA";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", "hello world");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, "/run/" + runId + "/runlog");
		MockRunLogServletEnvironment mockServletEnvironment = new MockRunLogServletEnvironment(mockRunResults, mockRequest);
		
		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this text:
		// {
		//    runId: "runA",
		//	  log: "hello world"
		// }
		String expectedJson = "{\n  \"runId\": \"runA\",\n  \"log\": \"hello world\"\n}";
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("Application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testRunResultWithEmptyLogReturnsEmptyLogOK() throws Exception {
		//Given..
		String runId = "runA";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", "");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, "/run/" + runId + "/runlog");
		MockRunLogServletEnvironment mockServletEnvironment = new MockRunLogServletEnvironment(mockRunResults, mockRequest);
		
		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this text:
		// {
		//    runId: "runA",
		//	  log: ""
		// }
		String expectedJson = "{\n  \"runId\": \"runA\",\n  \"log\": \"\"\n}";
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("Application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testBadRunIdGivesNotFoundError() throws Exception {
		//Given..
		String runId = "badRunId";
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/run/" + runId + "/runlog");
		MockRunLogServletEnvironment mockServletEnvironment = new MockRunLogServletEnvironment(null, mockRequest);
		
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
