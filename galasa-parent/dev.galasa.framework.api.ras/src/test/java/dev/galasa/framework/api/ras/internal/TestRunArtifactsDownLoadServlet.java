// /*
//  * Copyright contributors to the Galasa project 
//  */
// package dev.galasa.framework.api.ras.internal;

// import java.nio.file.Path;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// import org.apache.commons.lang3.RandomStringUtils;
// import org.junit.Test;

// import dev.galasa.framework.IFileSystem;
// import dev.galasa.framework.api.ras.internal.mocks.IServletUnderTest;
// import dev.galasa.framework.api.ras.internal.mocks.MockHttpServletRequest;
// import dev.galasa.framework.api.ras.internal.mocks.MockResultArchiveStoreDirectoryService;
// import dev.galasa.framework.api.ras.internal.mocks.MockRunResult;
// import dev.galasa.framework.api.ras.internal.mocks.MockServletBaseEnvironment;
// import dev.galasa.framework.mocks.MockFileSystem;
// import dev.galasa.framework.mocks.MockPath;
// import dev.galasa.framework.spi.IFramework;
// import dev.galasa.framework.spi.IRunResult;
// import dev.galasa.framework.spi.teststructure.TestStructure;

// import org.junit.Before;
// import org.junit.Ignore;

// import static org.assertj.core.api.Assertions.*;

// import java.io.ByteArrayOutputStream;
// import java.util.*;

// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

// public class TestRunArtifactsDownLoadServlet extends BaseServletTest {
	
//     private MockFileSystem mockFileSystem;
// 	/** 
// 	 * A subclass of the servlet we want to test, so that we can inject the mock framework in without 
// 	 * adding any extra code to the production servlet class. The framework field is protected scope, 
// 	 * so a subclass can do the injection instead of the injection framework.
// 	 */
// 	class MockRasRunServlet extends BaseServlet implements IServletUnderTest {

// 		@Override
// 		public void setFramework(IFramework framework) {
// 			super.framework = framework;
// 		}

// 		@Override
// 		public void setFileSystem(IFileSystem fileSystem) {
// 			super.fileSystem = fileSystem;
// 		}
// 	}

//     class MockRasRunServletEnvironment extends MockServletBaseEnvironment {

// 		public MockRasRunServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest){ 
//         	super(mockInpResults, mockRequest, new MockResultArchiveStoreDirectoryService(mockInpResults));
//     	}

// 		public MockRasRunServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockFileSystem mockFileSystem ){ 
// 			super(mockInpResults, mockRequest, mockFileSystem);
// 		}

// 		public BaseServlet getServlet() {
// 			return super.getBaseServlet();
// 		}

// 		@Override
// 		public IServletUnderTest createServlet() {
//         	return new MockRasRunServlet();
//     	}
// 	}

// 	public List<IRunResult> generateTestData(String runId, String runName, String runLog) {
// 		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();

// 		// Build the results the DB will return.
// 		String requestor = RandomStringUtils.randomAlphanumeric(8);
		
// 		TestStructure testStructure = new TestStructure();
// 		testStructure.setRunName(runName);
// 		testStructure.setRequestor(requestor);
// 		testStructure.setResult("Passed");

// 		Path artifactRoot = new MockPath("/" + runName + "/artifacts", mockFileSystem);
// 		IRunResult result = new MockRunResult( runId, testStructure, artifactRoot, runLog);
// 		mockInputRunResults.add(result);

// 		return mockInputRunResults;
// 	}

// 	@Before
// 	public void setUp() {
// 		mockFileSystem = new MockFileSystem();
// 	}

// 	@Ignore
// 	@Test
// 	public void testGoodRunIdAndBadArtifactIdGivesNotFoundError() throws Exception {
// 		//Given..
// 		String runId = "xx12345xx";
//         String artifactPath = "/bad/artifact/path";
// 		List<IRunResult> mockInputRunResults = generateTestData(runId, "U123", null);

// 		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
// 		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/run/" + runId + "/artifacts/" + artifactPath);
// 		MockRasRunServletEnvironment mockServletEnvironment = new MockRasRunServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);
		
// 		BaseServlet servlet = mockServletEnvironment.getServlet();
// 		HttpServletRequest req = mockServletEnvironment.getRequest();
// 		HttpServletResponse resp = mockServletEnvironment.getResponse();
// 		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
// 		//When...
// 		servlet.activate();
// 		servlet.init();
// 		servlet.doGet(req,resp);

// 		// Then...
// 		// Expecting this json:
// 		// {
// 		//   "error_code" : 5002,
// 		//   "error_message" : "GAL5002E: Error retrieving artifact from path '/bad/artifact/path'.""
// 		// }
// 		assertThat(resp.getStatus()).isEqualTo(404);
// 		checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", "xx12345xx" );
	
// 		assertThat( resp.getContentType()).isEqualTo("Application/json");
// 		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
// 	}

// 	@Test
// 	public void testBadRunIdAndBadArtifactIdGivesNotFoundError() throws Exception {
// 		//Given..
// 		String runId = "badRunId";
//         String artifactPath = "badArtifactId";
// 		List<IRunResult> mockInputRunResults = new ArrayList<>();

// 		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
// 		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/run/" + runId + "/artifacts/" + artifactPath);
// 		MockRasRunServletEnvironment mockServletEnvironment = new MockRasRunServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);
		
// 		BaseServlet servlet = mockServletEnvironment.getServlet();
// 		HttpServletRequest req = mockServletEnvironment.getRequest();
// 		HttpServletResponse resp = mockServletEnvironment.getResponse();
// 		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
// 		//When...
// 		servlet.activate();
// 		servlet.init();
// 		servlet.doGet(req,resp);

// 		// Then...
// 		// Expecting this json:
// 		// {
// 		//   "error_code" : 5002,
// 		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'badRunId'.""
// 		// }
// 		assertThat(resp.getStatus()).isEqualTo(404);
// 		checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", "badRunId" );
	
// 		assertThat( resp.getContentType()).isEqualTo("Application/json");
// 		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
// 	}

//     @Test
//     public void testGoodRunIdAndRunLogReturnsOKAndFile() throws Exception {
// 		//Given..
// 		String runId = "12345";
// 		String runName = "testA";
//         String artifactPath = "run.log";
//         String runlog = "Very Detailed Run Log. DEBUGGGING SOMETHING.";
//         List<IRunResult> mockInputRunResults = generateTestData(runId, runName, runlog);

// 		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
// 		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/run/" + runId + "/artifacts/" + artifactPath);
// 		MockRasRunServletEnvironment mockServletEnvironment = new MockRasRunServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);
		
// 		BaseServlet servlet = mockServletEnvironment.getServlet();
// 		HttpServletRequest req = mockServletEnvironment.getRequest();
// 		HttpServletResponse resp = mockServletEnvironment.getResponse();
// 		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
// 		//When...
// 		servlet.activate();
// 		servlet.init();
// 		servlet.doGet(req,resp);

// 		// Then...
// 		// Expecting:
// 		assertThat(resp.getStatus()).isEqualTo(200);
//         assertThat(outStream.toString()).isEmpty();
// 		assertThat(resp.getContentType()).isEqualTo("text/plain");
// 		assertThat(resp.getHeader("Content-Disposition")).isEqualTo("attachment; filename=\"%s-run.log\"", runName );
// 	}
// }
