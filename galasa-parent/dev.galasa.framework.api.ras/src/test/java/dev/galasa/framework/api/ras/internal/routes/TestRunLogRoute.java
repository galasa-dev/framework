/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.mocks.MockRasServletEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.ras.internal.mocks.MockRunResult;
import dev.galasa.framework.mocks.MockPath;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestRunLogRoute extends RasServletTest {

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
        String runLog = "hello world";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", runLog);
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(new HashMap<>(), "/runs/" + runId + "/runlog");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(runLog);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testRunResultWithEmptyLogReturnsEmptyLogOK() throws Exception {
		//Given..
		String runId = "runA";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", "");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(new HashMap<>(), "/runs/" + runId + "/runlog");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEmpty();
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testRunResultWithNullLogReturnsNotFoundError() throws Exception {
		//Given..
		String runId = "runA";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", null);
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(new HashMap<>(), "/runs/" + runId + "/runlog");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockRunResults, mockRequest);

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
		//   "error_code" : 5002,
		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'runA'.""
		// }
		assertThat(resp.getStatus()).isEqualTo(404);
		checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", "runA");
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testBadRunIdGivesNotFoundError() throws Exception {
		//Given..
		String runId = "badRunId";
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/runlog");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(null, mockRequest);

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
		//   "error_code" : 5002,
		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'badRunId'.""
		// }
		assertThat(resp.getStatus()).isEqualTo(404);
		checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", "badRunId" );
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	/*
	* REGEX TESTS
	*/
	@Test
	public void TestRunLogRouteRegexWithGenericExpectedInput() throws Exception {

		String testInput = "/runs/TEST123/runlog";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isTrue();
	}

	@Test
	public void TestRunLogRouteRegexWithMissingInitialSlashDoesntMatch() throws Exception {

		String testInput = "runs/OHNO123/runlog";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunLogRouteRegexWithJargonFirstSection() throws Exception {

		String testInput = "/thisisjargon/OHNO456/runlog";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunLogRouteRegexWithJargonLastSection() throws Exception {

		String testInput = "/runs/OHNO789/morejargon";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunLogRouteRegexWithSlashOnEndWorks() throws Exception {

		String testInput = "/runs/TEST456/runlog/";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isTrue();
	}

	@Test
	public void TestRunLogRouteRegexWithMissingSecondSlashDoesntMatch() throws Exception {

		String testInput = "/runsOHNO111/runlog";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunLogRouteRegexWithMissingThirdSlashDoesntMatch() throws Exception {

		String testInput = "/runs/OHNO112runlog";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunLogRouteRegexWithCompleteJargonButSlashesDoesntMatch() throws Exception {

		String testInput = "/gszjnkjasfdfd/alkjdfg/asdkadg";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunLogRouteRegexWithJargonAtStartDoesntMatch() throws Exception {

		String testInput = "JARGON/runs/OHNO113/runlog";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunLogRouteRegexWithJargonAtEndDoesntMatch() throws Exception {

		String testInput = "/runs/OHNO114/runlog/MOAR_JARGON";

		Pattern pattern = Pattern.compile(RunLogRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}
}