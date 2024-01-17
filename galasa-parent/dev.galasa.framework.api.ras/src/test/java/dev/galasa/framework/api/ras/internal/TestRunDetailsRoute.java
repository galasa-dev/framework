/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal;

import org.junit.Test;

import dev.galasa.framework.api.ras.internal.mocks.MockArchiveStore;
import dev.galasa.framework.api.ras.internal.mocks.MockRasServletEnvironment;
import dev.galasa.framework.api.ras.internal.mocks.MockResultArchiveStoreDirectoryService;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockIFrameworkRuns;
import dev.galasa.framework.api.common.mocks.MockIRun;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.IRunResult;

import static org.assertj.core.api.Assertions.*;

import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class TestRunDetailsRoute extends RasServletTest {

    public String generateExpectedJson (String runId, String runName ){
		return  "{\n"+
        "  \"runId\": \""+ runId +"\",\n"+
        "  \"artifacts\": [],\n"+
        "  \"testStructure\": {\n"+
        "    \"runName\": \""+runName+"\",\n"+
        "    \"requestor\": \"galasa\",\n"+
        "    \"result\": \"Passed\",\n"+
        "    \"methods\": []\n"+
        "  }\n"+
        "}";
    }

	public String generateStatusUpdateJson(String status) {
		return
		"{\n" +
	    "  \"status\": \"" + status + "\"\n" +
		"}";
	}

    @Test
    public void testGoodRunIdReturnsOK() throws Exception {
		//Given..
		String runId = "xx12345xx";
        String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		String expectedJson = generateExpectedJson(runId, runName);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}


    @Test
    public void testBadRunIdReturnsError() throws Exception {
		//Given..
		String runId = "badRunId";

		List<IRunResult> mockInputRunResults = generateTestData("OtherRunId", "R123", null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
        assertThat(resp.getStatus()).isEqualTo(404);
        checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", runId );
        assertThat( resp.getContentType()).isEqualTo("application/json");
        assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

    @Test
    public void testNoRunReturnsError() throws Exception {
		//Given..
		String runId = "badRunId";

		List<IRunResult> mockInputRunResults = generateTestData(null, null, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
        assertThat(resp.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString() , 5000 , "GAL5000E" );
        assertThat( resp.getContentType()).isEqualTo("application/json");
        assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testRequestToResetRunReturnsOK() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("reset");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs);
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo("Successfully reset run " + runName);
	}

	@Test
	public void testRequestToDeleteRunReturnsOK() {
		

	}

	@Test
	public void testRequestToUpdateRunStatusWithInvalidStatusReturnsError() {
		
	}

}