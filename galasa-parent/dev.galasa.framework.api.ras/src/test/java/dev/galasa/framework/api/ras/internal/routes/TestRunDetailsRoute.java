/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import org.junit.Test;

import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.mocks.MockRasServletEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.spi.IRunResult;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	/*
	* REGEX TESTS
	*/
	@Test
	public void TestRunDetailsRouteRegexWithGenericExpectedInput() throws Exception {

		String testInput = "/runs/TEST123";

		Pattern pattern = Pattern.compile(RunDetailsRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isTrue();
	}

	@Test
	public void TestRunDetailsRouteRegexWithMissingInitialSlashDoesntMatch() throws Exception {

		String testInput = "runs/WHOOPSIE123";

		Pattern pattern = Pattern.compile(RunDetailsRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunDetailsRouteRegexWithAdditionalLastSlashMatches() throws Exception {

		String testInput = "/runs/TEST456/";

		Pattern pattern = Pattern.compile(RunDetailsRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isTrue();
	}

	@Test
	public void TestRunDetailsRouteRegexWithJargonAtStartDoesntMatch() throws Exception {

		String testInput = "tinsie_bit_of_jargon/runs/WHOOPSIE456/";

		Pattern pattern = Pattern.compile(RunDetailsRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunDetailsRouteRegexWithJargonFirstSectionDoesntMatch() throws Exception {

		String testInput = "/tad_more_jargon/WHOOPSIE789/";

		Pattern pattern = Pattern.compile(RunDetailsRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunDetailsRouteRegexWithMissingSecondSlashDoesntMatch() throws Exception {

		String testInput = "/runsWHOOPSIE111/";

		Pattern pattern = Pattern.compile(RunDetailsRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunDetailsRouteRegexWithPureJargonDoesntMatch() throws Exception {

		String testInput = "THIS_IS_JARGON";

		Pattern pattern = Pattern.compile(RunDetailsRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}
}