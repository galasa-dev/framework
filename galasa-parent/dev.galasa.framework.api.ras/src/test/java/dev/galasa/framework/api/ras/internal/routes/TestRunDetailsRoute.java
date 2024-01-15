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

		/*
     * Regex Path
     */

	@Test
	public void TestPathRegexExpectedLocalPathReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/lcl-abcd-1234.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexExpectedCouchDBPathReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-efgh-5678.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexExpectedNoTrailingForwardSlashReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-efgh-5678.run";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexLowerCasePathReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdbstoredrun/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void TestPathRegexExpectedPathWithCapitalLeadingLetterReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/ABC-DEFG-5678.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void TestPathRegexUpperCasePathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/RUNS/cdb-EFGH-5678.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	@Test
	public void TestPathRegexExpectedPathWithLeadingNumberReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/1runs/cdb-EFGH-5678.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	@Test
	public void TestPathRegexExpectedPathWithTrailingForwardSlashReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run//";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	@Test
	public void TestPathRegexNumberPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/1";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexUnexpectedPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/randompath";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexEmptyPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexSpecialCharacterPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/?";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexMultipleForwardSlashPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run///////";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	} 

	/*
	 * GET Requests
	 */

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

}