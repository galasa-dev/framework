/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

import org.junit.Test;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.common.RasQueryParameters;
import dev.galasa.framework.api.ras.internal.mocks.*;
import dev.galasa.framework.mocks.MockPath;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TestRunQuery extends RasServletTest {

    private GalasaGson gson = new GalasaGson();

	private Map<String, String[]> addQueryParameter ( Map<String, String[]> map, String key, String value){
		if (value != null){
			map.put(key, new String[] {value});
		}
		return map;
	}

	private Map<String, String[]> addQueryIntParameter ( Map<String, String[]> map, String key, Integer value){
		if (value != null){
			return addQueryParameter(map, key, value.toString());
		}
		return map;
	}

	private Map<String, String[]> addQueryTimeParameter ( Map<String, String[]> map, String key, Integer value){
		if (value != null){
			return addQueryParameter(map, key, Instant.now().minus(value, ChronoUnit.HOURS).toString());
		}
		return map;
	}

	private Map<String, String[]> setQueryParameter (Integer page, Integer size,String sort, String runname, String requestor, Integer agemin, Integer agemax){
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		parameterMap = addQueryIntParameter(parameterMap, "page", page);
		parameterMap = addQueryIntParameter(parameterMap, "size", size);
		parameterMap = addQueryParameter(parameterMap, "sort", sort);
		parameterMap = addQueryParameter(parameterMap, "runname", runname);
		parameterMap = addQueryParameter(parameterMap, "requestor", requestor);
		parameterMap = addQueryTimeParameter(parameterMap, "from", agemin);
		parameterMap = addQueryTimeParameter(parameterMap, "to", agemax);
		return parameterMap;
	}

    private IRunResult createTestRun(String runId, Instant queuedTime, Instant startTime, Instant endTime) {
        String runName = RandomStringUtils.randomAlphanumeric(5);
        String testShortName = RandomStringUtils.randomAlphanumeric(5);
        String requestor = RandomStringUtils.randomAlphanumeric(8);
        String bundleName = RandomStringUtils.randomAlphanumeric(16);

        TestStructure testStructure = new TestStructure();
        testStructure.setRunName(runName);
        testStructure.setRequestor(requestor);
        testStructure.setTestShortName(testShortName);
        testStructure.setBundle(bundleName);
        testStructure.setTestName(testShortName + "." + RandomStringUtils.randomAlphanumeric(8));
        testStructure.setQueued(queuedTime);
        testStructure.setStartTime(startTime);
        testStructure.setEndTime(endTime);

        Path artifactsRoot = new MockPath("/", mockFileSystem);
        String log = RandomStringUtils.randomAlphanumeric(6);
        return new MockRunResult(runId, testStructure, artifactsRoot, log);
    }

	private List<IRunResult> generateTestDataAscendingTime(int resSize, int passTests, int hoursDeducted) throws ResultArchiveStoreException {
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		int passCount = 0;
		// Build the results the DB will return.
		for(int c =0 ; c < resSize; c++){
            String runId = RandomStringUtils.randomAlphanumeric(16);
            Instant baseTime = Instant.now().minus(hoursDeducted, ChronoUnit.HOURS).minus(c, ChronoUnit.MINUTES);
            IRunResult mockRun = createTestRun(runId, baseTime, baseTime, baseTime);

            TestStructure testStructure = mockRun.getTestStructure();
			if (passCount < passTests){
				testStructure.setResult("Passed");
				testStructure.setStatus("running");
				passCount ++;
			}else{
				testStructure.setResult("Failed");
				testStructure.setStatus("building");
			}
			mockInputRunResults.add(0, mockRun);
		}
		return mockInputRunResults;
	}

	private List<String> generateExpectedRunNames(List<IRunResult> expectedRunResults) throws ResultArchiveStoreException {
		List <String> runnames = new ArrayList<String>();
		for (IRunResult run : expectedRunResults){
			runnames.add(run.getTestStructure().getRunName().toString());
		}

		return runnames;
	}

    private JsonArray createRunsJsonArray(List<IRunResult> mockRuns) throws ResultArchiveStoreException {
        JsonArray runsJson = new JsonArray();
        for (IRunResult run : mockRuns) {
            JsonObject runJson = new JsonObject();
            runJson.addProperty("runId", run.getRunId());

            JsonElement testStructureJson = gson.toJsonTree(run.getTestStructure());
            runJson.add("testStructure", testStructureJson);
            runsJson.add(runJson);
        }
        return runsJson;
    }

	private String generateExpectedJson(List<IRunResult> mockInputRunResults, int pageSize, int pageNum) throws ResultArchiveStoreException {
        List<List<IRunResult>> pagedRuns = ListUtils.partition(mockInputRunResults, pageSize);
		
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("pageNum", pageNum);
        jsonResult.addProperty("pageSize", pageSize);

        JsonArray runsJson = new JsonArray();
        if (!pagedRuns.isEmpty()) {
            jsonResult.addProperty("numPages", pagedRuns.size());

            runsJson = createRunsJsonArray(pagedRuns.get(pageNum - 1));

        } else {
            jsonResult.addProperty("numPages", 1);
        }

        jsonResult.addProperty("amountOfRuns", mockInputRunResults.size());
        jsonResult.add("runs", runsJson);

		return gson.toJson(jsonResult);
	}

	private String generateExpectedJson(List<IRunResult> mockInputRunResults, String nextCursor, int pageSize) throws ResultArchiveStoreException {
		
        JsonObject jsonResult = new JsonObject();
        JsonArray runsJson = createRunsJsonArray(mockInputRunResults);

        jsonResult.addProperty("pageSize", pageSize);
        jsonResult.addProperty("amountOfRuns", mockInputRunResults.size());
        jsonResult.addProperty("nextCursor", nextCursor);
        jsonResult.add("runs", runsJson);
		return gson.toJson(jsonResult);
	}

	private void testQueryParametersReturnsError(
		Map<String, String[]> parameterMap ,
		int expectedErrorCode,
		String... expectedErrorMsgSubStrings
	) throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(20,10,1);

		String[] sortValues = {"result:asc"};
		parameterMap.put("sort", sortValues );

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()).isEqualTo(400);

		checkErrorStructure(
			outStream.toString(),
			expectedErrorCode,
			expectedErrorMsgSubStrings
		);

		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	private boolean checkIfSameOrder(String[] sortedList, String expectedResultString, String sortedElement){

		// If the query was sort=runname:asc the sortedElement would be runName

		JsonElement jsonElement = JsonParser.parseString(expectedResultString);
		JsonArray runs = jsonElement.getAsJsonObject().get("runs").getAsJsonArray();
		for (int i = 0; i <= runs.size(); i++) {
			JsonElement run = runs.get(i);
			JsonElement testStructure = run.getAsJsonObject().get("testStructure");
			String value = testStructure.getAsJsonObject().get(sortedElement).getAsString();

			if (value != sortedList[i]){
				return false;
			}
		}

		return true;
	}

	/*
	*TESTS
	*/

	/*
     * Regex Path
     */

	@Test
	public void TestPathRegexExpectedPathReturnsTrue(){
		//Given...
		String expectedPath = RunQueryRoute.path;
		String inputPath = "/runs";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexExpectedPathWithNumbersReturnsFalse(){
		//Given...
		String expectedPath = RunQueryRoute.path;
		String inputPath = "/r0ns";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexLowerCasePathReturnsTrue(){
		//Given...
		String expectedPath = RunQueryRoute.path;
		String inputPath = "/runs";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void TestPathRegexExpectedPathWithCapitalLeadingLetterReturnsFalse(){
		//Given...
		String expectedPath = RunQueryRoute.path;
		String inputPath = "/Runs";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
	
	@Test
	public void TestPathRegexUpperCasePathReturnsFalse(){
		//Given...
		String expectedPath = RunQueryRoute.path;
		String inputPath = "/RUNS";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	 @Test
	 public void TestPathRegexExpectedPathWithLeadingNumberReturnsFalse(){
		 //Given...
		 String expectedPath = RunQueryRoute.path;
		 String inputPath = "/0rans";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void TestPathRegexExpectedPathWithTrailingForwardSlashReturnsTrue(){
		 //Given...
		 String expectedPath = RunQueryRoute.path;
		 String inputPath = "/runs/";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isTrue();
	 }
 
	 @Test
	 public void TestPathRegexNumberPathReturnsFalse(){
		 //Given...
		 String expectedPath = RunQueryRoute.path;
		 String inputPath = "/runs1234";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void TestPathRegexUnexpectedPathReturnsFalse(){
		 //Given...
		 String expectedPath = RunQueryRoute.path;
		 String inputPath = "/requestor";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void TestPathRegexEmptyPathReturnsFalse(){
		 //Given...
		 String expectedPath = RunQueryRoute.path;
		 String inputPath = "";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void TestPathRegexSpecialCharacterPathReturnsFalse(){
		 //Given...
		 String expectedPath = RunQueryRoute.path;
		 String inputPath = "/runs/?";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void TestPathRegexMultipleForwardSlashPathReturnsFalse(){
		 //Given...
		 String expectedPath = RunQueryRoute.path;
		 String inputPath = "/runs//////";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 } 

	/*
	 * GET Requests
	 */

	@Test
	public void testQueryWithRequestorNotSortedButNoDBServiceReturnsError() throws Exception {
		// Given...
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null, null,"mickey", 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap,"/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(null,mockRequest);

		// Oh no ! There are no directory services which represent a RAS store !
		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any RAS database to query
		assertThat(resp.getStatus()).isEqualTo(500);
		assertThat(resp.getContentType()).isEqualTo("application/json");

		checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E: ",
			"Error occurred when trying to access the endpoint"
		);
	}

	@Test
	public void testQueryWithFailedRequestReturnsGenericError() throws Exception {
		// Given...
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(new HashMap<>(),"/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(null, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any RAS database to query
		assertThat(resp.getStatus()).isEqualTo(500);
		assertThat(resp.getContentType()).isEqualTo("application/json");

		checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E: ",
			"access",
			"endpoint"
		);
	}

	@Test
	public void testNoQueryNotSortedWithDBServiceReturnsOK() throws Exception {
		// Given...
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		int pageSize = 100;
		int pageNo = 1;

		List<IRunResult> mockInputRunResults= new ArrayList<IRunResult>();

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an empty page back, because the API server couldn't find any results
		String expectedJson = generateExpectedJson(mockInputRunResults, pageSize, pageNo);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithEmptyDBServiceReturnsOK() throws Exception {
		// Given...
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null, null,"mickey", 72, null);

		List<IRunResult> mockInputRunResults= new ArrayList<IRunResult>();

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
		int pageSize = 100;
		int pageNo = 1;

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 0,
		//   "runs": [
		// 	 ]
		//   }
		String expectedJson = generateExpectedJson(mockInputRunResults, pageSize, pageNo);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceWithOneRecordReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(1,1,1);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null, null,null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		int pageSize = 100;
		int pageNo = 1;
		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
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
		String expectedJson = generateExpectedJson(mockInputRunResults,pageSize, pageNo);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2,1);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null, null,null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [
		//     {
		//       "runId": "xxx-yyy-zzz-012345",
		//       "testStructure": {
		//         "runName": "A1234",
		//         "requestor": "mickey"
		//       }
		//     ....
		//     {
		//       "runId": "xxx-yyy-zzz-012345",
		//       "testStructure": {
		//         "runName": "A1244",
		//         "requestor": "mickey"
		//       }
		// 	   }
		// 	]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(mockInputRunResults);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		Collections.sort(expectedRunNames, Collections.reverseOrder());

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, actualOutput, "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateNotSortedWithDBServiceWithOneRecordReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(1,1,1);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null, null,null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		int pageSize = 100;
		int pageNo = 1;
		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
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
		String expectedJson = generateExpectedJson(mockInputRunResults ,pageSize, pageNo);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithoutFromDateOrRunNameNotSortedWithDBServiceTenRecordsReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 48);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null, null ,null, null, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:

		//Then...
		assertThat(resp.getStatus()).isEqualTo(400);
		assertThat(outStream.toString()).contains("GAL5010E: Error parsing the query parameters. from time is a mandatory field if no runname is supplied.");
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testNoQueryWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 15);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(null,null,null, null ,null, null, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:

		//Then...
		List<String> expectedRunNames = generateExpectedRunNames(mockInputRunResults);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		Collections.sort(expectedRunNames, Collections.reverseOrder());

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, actualOutput, "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithoutFromDateWithRunNameNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 48);
		//Build Http query parameters
		IRunResult run = mockInputRunResults.get(1);
        int pageSize = 100;
        int pageNum = 1;

		Map<String, String[]> parameterMap = setQueryParameter(pageNum,pageSize,null,run.getTestStructure().getRunName(),null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		List<IRunResult> expectedRun = new ArrayList<IRunResult>();
		expectedRun.add(run);
		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 0,
		//   "runs": []
		// }
		String expectedJson = generateExpectedJson(expectedRun, pageSize, pageNum);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateAndRunnameNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 48);
		//Build Http query parameters
		IRunResult run = mockInputRunResults.get(5);

        int pageSize = 100;
        int pageNum = 1;
		Map<String, String[]> parameterMap = setQueryParameter(pageNum,pageSize,null,run.getTestStructure().getRunName(),null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		List<IRunResult> expectedRun = new ArrayList<IRunResult>();
		expectedRun.add(run);
		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 0,
		//   "runs": []
		// }
		String expectedJson = generateExpectedJson(expectedRun, pageSize, pageNum);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateAndRunnameOutsideNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 48);
		//Build Http query parameters
		IRunResult run = mockInputRunResults.get(5);

        int pageSize = 100;
        int pageNum = 1;
		Map<String, String[]> parameterMap = setQueryParameter(pageNum,pageSize,null,run.getTestStructure().getRunName(),null, 24, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		List<IRunResult> expectedRun = new ArrayList<IRunResult>();
		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 0,
		//   "runs": []
		// }
		String expectedJson = generateExpectedJson(expectedRun, pageSize, pageNum);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTenRecordsPageSizeFiveReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,5,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null,null,requestor, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 5,
		//   "numPages": 2,
		//   "amountOfRuns": 10,
		//   "runs": [
		//     {
		//       "runId": "xxx-yyy-zzz-012345",
		//       "testStructure": {
		//         "runName": "A1234",
		//         "requestor": "mickey"
		//       }
		//     ....
		//     {
		//       "runId": "xxx-yyy-zzz-012345",
		//       "testStructure": {
		//         "runName": "A1244",
		//         "requestor": "mickey"
		//       }
		// 	   }
		// 	]
		// }
		List<IRunResult> expectedRunResults = new ArrayList<IRunResult>(); ;
		expectedRunResults.add(mockInputRunResults.get(0));
		List<String> expectedRunNames = generateExpectedRunNames(expectedRunResults);
        String actualOutput = outStream.toString();

		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		Collections.sort(expectedRunNames, Collections.reverseOrder());

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, actualOutput, "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTenRecordsPageSizeFivePageTwoReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,5,2);

		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(2,5,null,null,null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 2,
		//   "pageSize": 5,
		//   "numPages": 2,
		//   "amountOfRuns": 10,
		//   "runs": [
		//     {
		//       "runId": "xxx-yyy-zzz-012345",
		//       "testStructure": {
		//         "runName": "A1234",
		//         "requestor": "mickey"
		//       }
		//     ....
		//     {
		//       "runId": "xxx-yyy-zzz-012345",
		//       "testStructure": {
		//         "runName": "A1244",
		//         "requestor": "mickey"
		//       }
		// 	   }
		// 	]
		// }
		IResultArchiveStoreDirectoryService mockrasDirectoryService = mockServletEnvironment.getDirectoryService().get(0);
		IRasSearchCriteria[] criteria = {};
		List<IRunResult> runsInMockRas = mockrasDirectoryService.getRuns(criteria);
		List<String> orderedRunNames = generateExpectedRunNames(runsInMockRas);
        Collections.reverse(orderedRunNames);
    
		List<String> expectedRunNames = orderedRunNames.subList(5, 10);
        String actualOutput = outStream.toString();

		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, actualOutput, "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTwentyRecordsPageSizeFivePageThreeReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(20,5,1);
		//Build Http query parameters
        int pageSize = 5;
        int pageNum = 3;
		Map<String, String[]> parameterMap = setQueryParameter(pageNum,pageSize,null,null,null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 3,
		//   "pageSize": 5,
		//   "numPages": 4,
		//   "amountOfRuns": 20,
		//   "runs": [
		//     {
		//       "runId": "xxx-yyy-zzz-012345",
		//       "testStructure": {
		//         "runName": "A1234",
		//         "requestor": "mickey"
		//       }
		//     ....
		//     {
		//       "runId": "xxx-yyy-zzz-012345",
		//       "testStructure": {
		//         "runName": "A1244",
		//         "requestor": "mickey"
		//       }
		// 	   }
		// 	]
		// }
        Collections.reverse(mockInputRunResults);

		String expectedJson = generateExpectedJson(mockInputRunResults, pageSize, pageNum);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTwentyRecordsPageSizeFivePageFiveReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(20,3,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(5,5,null,null,requestor, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()).isEqualTo(400);

		checkErrorStructure(outStream.toString(), 5004, "GAL5004E: ", "Error retrieving page.");

		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithResultsSortedWithDBServiceTenRecordsPageSize100FalseFromDateReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(20,10,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"result:asc",null,requestor, null, null);
		String[] fromTime = {"erroneousValue"};
		parameterMap.put("from", fromTime);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()).isEqualTo(400);

		checkErrorStructure(
			outStream.toString(),
			5001,
			"GAL5001E:","Error parsing the date-time field","'from'",fromTime[0]
		);

		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithResultsSortedWithDBServiceTenRecordsPageSize100FalseToDateReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(20,10,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null,null,requestor, null, null);
		String[] toTime = {"erroneousValue"};
		parameterMap.put("to", toTime);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()).isEqualTo(400);

		checkErrorStructure(
			outStream.toString(),
			5001,
			"GAL5001E:","Error parsing the date-time field","'to'",toTime[0]
		);

		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithResultsSortedWithDBServiceTenRecordsPageSize100FalseRunIDReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(20,10,1);

		MockResultArchiveStoreDirectoryService storeWhichThrowsUp = new MockResultArchiveStoreDirectoryService(null) {
			@Override
			public IRunResult getRunById(@NotNull String runId) throws ResultArchiveStoreException {
				throw new ResultArchiveStoreException();
			}
		};

		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null,null,requestor, 72, null);;
		String[] runId = {"erroneousrunId"};
		parameterMap.put("runId", runId);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest,storeWhichThrowsUp);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()==404);
		assertThat(resp.getContentType()).isEqualTo("application/json");

		checkErrorStructure(
			outStream.toString(),
			5002,
			"GAL5002E:",
			"erroneousrunId",
			runId[0]
		);

	}

	@Test
	public void testQueryWithNonIntegerPageSizeReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String[] pageSize = {"NonIntegerErroneousValue"}; // << This is what should cause the failure.
		parameterMap.put("size", pageSize);

		testQueryParametersReturnsError(parameterMap ,5005, "GAL5005E:","'size'","Invalid","NonIntegerErroneousValue");
	}

	@Test
	public void testQueryWithNonIntegerPageNumberReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String[] pageNumber = {"NonIntegerErroneousValue"}; // << This is what should cause the failure.
		parameterMap.put("page", pageNumber);

		testQueryParametersReturnsError(parameterMap ,5005, "GAL5005E:","'page'","Invalid","NonIntegerErroneousValue");
	}

	@Test
	public void testQueryWithMultipleRequestorsReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two requestors ! should be invalid !
		String[] requestors = new String[] {"homer","bart"};
		parameterMap.put("requestor",  requestors);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'requestor'","Duplicate");
	}

	@Test
	public void testQueryWithMultipleTestNamesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two test names ! should be invalid !
		String[] testNames = new String[] {"testA","testB"};
		parameterMap.put("testname",  testNames);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'testname'","Duplicate");
	}

	@Test
	public void testQueryWithMultipleBundlesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two bundles ! should be invalid !
		String[] bundles = new String[] {"bundleA","bundleB"};
		parameterMap.put("bundle",  bundles);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'bundle'","Duplicate");
	}

	@Test
	public void testQueryWithMultipleResultsReturnsOK () throws Exception {

		//Given...
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(null,null,"result:asc",null, null, 72, null);
		// Two results should return all the results
		String[] results = new String[] {"Passed,Failed"};
		parameterMap.put("result",  results);

		int pageSize = 100;
		int pageNo = 1;

		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(20,10,1);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		String expectedJson = generateExpectedJson(mockInputRunResults,pageSize, pageNo);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
 		assertThat(outStream.toString()).isEqualTo(expectedJson);
	}

	@Test
	public void testQueryWithMultipleStatusesReturnsOK () throws Exception {

		//Given...
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(null,null,"result:asc",null, null, 72, null);
		// Two results should return all the results
		String[] statuses = new String[] {"building,running"};
		parameterMap.put("status",  statuses);

		int pageSize = 100;
		int pageNo = 1;

		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(20,10,1);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		String expectedJson = generateExpectedJson(mockInputRunResults,pageSize, pageNo);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
 		assertThat(outStream.toString()).isEqualTo(expectedJson);
	}

	@Test
	public void testQueryWithMultipleRunNamesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two runnames ! should be invalid !
		String[] runNames = new String[] {"runnameA","runnameB"};
		parameterMap.put("runname",  runNames);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'runname'","Duplicate");
	}

	@Test
	public void testQueryWithMultipleToTimesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two 'to' times ! should be invalid !
		String[] toTimes = new String[] {"2023-04-11T09:42:06.589180Z","2023-04-21T06:00:54.597509Z"};
		parameterMap.put("to",  toTimes);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'to'","Duplicate");
	}

	@Test
	public void testQueryWithMultipleFromTimesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two 'from' times ! should be invalid !
		String[] fromTimes = new String[] {"2023-04-11T09:42:06.589180Z","2023-04-21T06:00:54.597509Z"};
		parameterMap.put("from",  fromTimes);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'from'","Duplicate");
	}

	@Test
	public void testQueryWithMultiplePageSizesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two page sizes ! should be invalid !
		String[] pageSizes = new String[] {"5","10"};
		parameterMap.put("size",  pageSizes);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'size'","Duplicate");
	}

	@Test
	public void testQueryWithMultiplePageNumReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two page numbers ! should be invalid !
		String[] pageNums = new String[] {"5","10"};
		parameterMap.put("page",  pageNums);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'page'","Duplicate");
	}

	@Test
	public void testQueryWithFromDateAndToDateNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(10,2, 48);
		List<IRunResult> mockInputRunResults3 = generateTestDataAscendingTime(10,2, 24);
		mockInputRunResults.addAll(mockInputRunResults3);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,null,null, null, 72, 36);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
		List<String> excludedRunNames = generateExpectedRunNames(excludedRuns);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		assertThat(actualOutput).doesNotContain(excludedRunNames);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateAndToDatetSortedToDescendingWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(10,2, 48);
		List<IRunResult> mockInputRunResults3 = generateTestDataAscendingTime(10,2, 24);
		mockInputRunResults.addAll(mockInputRunResults3);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"to:desc",null, null, 72, 36);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		Collections.sort(expectedRunNames, Collections.reverseOrder());

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, actualOutput, "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateAndToDatetSortedToAscendingWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(10,2, 48);
		List<IRunResult> mockInputRunResults3 = generateTestDataAscendingTime(10,2, 24);
		mockInputRunResults.addAll(mockInputRunResults3);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"to:asc",null, null, 72, 36);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		Collections.sort(expectedRunNames, Collections.reverseOrder());

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, actualOutput, "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateAndToDatetSortedResultDescendingWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(10,2, 48);
		List<IRunResult> mockInputRunResults3 = generateTestDataAscendingTime(10,2, 24);
		mockInputRunResults.addAll(mockInputRunResults3);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"result:desc",null, null, 72, 36);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		Collections.sort(expectedRunNames, Collections.reverseOrder());

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, actualOutput, "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateAndToDateResultSortedResultAscendingWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(10,2, 48);
		List<IRunResult> mockInputRunResults3 = generateTestDataAscendingTime(10,2, 24);
		mockInputRunResults.addAll(mockInputRunResults3);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"result:asc",null, null, 72, 36);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		Collections.sort(expectedRunNames);

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, actualOutput, "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateSortedTestclassDescendingWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(10,2, 48);
		mockInputRunResults.addAll(expectedInputRunResults);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"testclass:desc",null, null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).contains(expectedRunNames);
		Collections.sort(expectedRunNames, Collections.reverseOrder());

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, outStream.toString(), "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateAndToDateResultSortedTestclassAscendingWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(10,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(10,2, 48);
		List<IRunResult> mockInputRunResults3 = generateTestDataAscendingTime(10,2, 24);
		mockInputRunResults.addAll(mockInputRunResults3);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"testclass:asc",null, null, 72, 36);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		Collections.sort(expectedRunNames);

		String[] sortedList = (expectedRunNames).toArray(new String[expectedRunNames.size()]);
		assertThat(checkIfSameOrder(sortedList, actualOutput, "runName"));
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateAndToDateBadSortWithDBServiceTenRecordsReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults =  generateTestDataAscendingTime(10,2, 48);

		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"badsort",null, null, 72, 36);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		assertThat(resp.getStatus()).isEqualTo(400);
		assertThat(outStream.toString()).contains("GAL5011E:","badsort");
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateBadSortValueWithDBServiceTenRecordsReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults =  generateTestDataAscendingTime(10,2, 48);

		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"to:erroneoussort",null, null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()).isEqualTo(400);
		assertThat(outStream.toString()).contains("GAL5011E:","to:erroneoussort");
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromDateBadSortKeyWithDBServiceTenRecordsReturnsOkUnsorted() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults =  generateTestDataAscendingTime(10,2, 48);

		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"erroneoussort:desc",null, null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		assertThat(resp.getStatus()).isEqualTo(400);
        assertThat(outStream.toString()).contains("GAL5011E", "Error parsing the query parameters", "sort value 'erroneoussort' not recognised");
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromResultNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(8,0, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(2,2, 48);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"to:asc",null, null, 72, null);
		parameterMap.put("result", new String[] {"Passed"});

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
		List<String> excludedRunNames = generateExpectedRunNames(excludedRuns);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		assertThat(actualOutput).doesNotContain(excludedRunNames);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromtestNameNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(9,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(1,1, 48);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		IRunResult run = expectedInputRunResults.get(0);
		String testName = run.getTestStructure().getTestName().toString();
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"to:asc",null, null, 72, null);
		parameterMap.put("testname", new String[] {testName});

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
		List<String> excludedRunNames = generateExpectedRunNames(excludedRuns);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		assertThat(actualOutput).doesNotContain(excludedRunNames);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromBundleNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(9,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(1,1, 48);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		IRunResult run = expectedInputRunResults.get(0);
		String bundle = run.getTestStructure().getBundle();
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"to:asc",null, null, 72, null);
		parameterMap.put("bundle", new String[] {bundle});

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		List<String> expectedRunNames = generateExpectedRunNames(expectedInputRunResults);
		List<String> excludedRunNames = generateExpectedRunNames(excludedRuns);
        String actualOutput = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(actualOutput).contains(expectedRunNames);
		assertThat(actualOutput).doesNotContain(excludedRunNames);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromRunIdNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(9,2, 48);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(1,1, 48);
		mockInputRunResults.addAll(expectedInputRunResults);
		IRunResult run = expectedInputRunResults.get(0);
		String runid = run.getRunId();
		
        // Build query parameters
        int pageSize = 100;
        int pageNum = 1;
		Map<String, String[]> parameterMap = setQueryParameter(pageNum,pageSize,"to:asc",null, null, 72, null);
		parameterMap.put("runId", new String[] {runid});

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
		String expectedRunNames = generateExpectedJson(expectedInputRunResults, pageSize, pageNum);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedRunNames);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithFromRunIdsNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(2,2, 48);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(2,1, 48);
		mockInputRunResults.addAll(expectedInputRunResults);
		IRunResult run = expectedInputRunResults.get(0);
		String runid = run.getRunId();
		IRunResult run1 = expectedInputRunResults.get(1);
		String runid1 = run1.getRunId();
		
        //Build query parameters
        int pageSize = 100;
        int pageNum = 1;
		Map<String, String[]> parameterMap = setQueryParameter(pageNum, pageSize, null, null, null, 72, null);
		parameterMap.put("runId", new String[] {runid+","+runid1});

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
        Collections.reverse(expectedInputRunResults);
		String expectedRunNames = generateExpectedJson(expectedInputRunResults, pageSize, pageNum);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedRunNames);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithRunIdsNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(2,2, 48);
		List<IRunResult> expectedInputRunResults = generateTestDataAscendingTime(2,1, 48);
		mockInputRunResults.addAll(expectedInputRunResults);
		IRunResult run = expectedInputRunResults.get(0);
		String runid = run.getRunId();
		IRunResult run1 = expectedInputRunResults.get(1);
		String runid1 = run1.getRunId();
		
        //Build Http query parameters
        int pageSize = 100;
        int pageNum = 1;
		Map<String, String[]> parameterMap = setQueryParameter(pageNum, pageSize, null, null, null, null, null);
		parameterMap.put("runId", new String[] {runid+","+runid1});

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		//  {
		//   "pageNum": 1,
		//   "pageSize": 100,
		//   "numPages": 1,
		//   "amountOfRuns": 10,
		//   "runs": [...]
		// }
        Collections.reverse(expectedInputRunResults);
		String expectedRunNames = generateExpectedJson(expectedInputRunResults, pageSize, pageNum);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedRunNames);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
    public void testGetDefaultFromInstantIfNoQueryIsPresentQuerySizeOneNoFromReturnsError() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {""} );
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));

		Throwable thrown = catchThrowable( () -> {
        	new RunQueryRoute( new ResponseBuilder(), null).getQueriedFromTime(params,Instant.now());
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5010E: Error parsing the query parameters. from time is a mandatory field if no runname is supplied.");
    }

	@Test
    public void testGetDefaultFromInstantIfNoQueryIsPresentWithFromEmptyReturnsError() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("from", new String[] {""} );
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));

		Throwable thrown = catchThrowable( () -> {
            new RunQueryRoute( new ResponseBuilder(), null).getQueriedFromTime(params,Instant.now());
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5010E: Error parsing the query parameters. from time is a mandatory field if no runname is supplied.");
    }

	@Test
    public void testGetDefaultFromInstantIfNoQueryIsPresentWithFromReturnsValue() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
		Instant fromInstant= Instant.now();
		String fromString = fromInstant.toString();
        map.put("from", new String[] {fromString} );
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));
        Instant checker = new RunQueryRoute(new ResponseBuilder(),null).getQueriedFromTime(params, Instant.parse("2023-07-21T06:10:29.640750Z"));

		assertThat(checker).isNotNull();
        assertThat(checker).isEqualTo(fromInstant);
    }

	@Test
    public void testGetDefaultFromInstantIfNoQueryIsPresentWithFromAndRunnameReturnsValue() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
		Instant fromInstant= Instant.now();
		String fromString = fromInstant.toString();
        map.put("from", new String[] {fromString} );
		map.put("runname", new String[] {"runname"} );
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));
        Instant checker = new RunQueryRoute(new ResponseBuilder(),null).getQueriedFromTime(params, Instant.parse("2023-07-21T06:10:29.640750Z"));

		assertThat(checker).isNotNull();
        assertThat(checker).isEqualTo(fromInstant);
    }

	@Test
    public void testGetDefaultFromInstantIfNoQueryIsPresentWithRunNameReturnsNull() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("runname", new String[] {"runname"} );
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));
        Instant checker = new RunQueryRoute(new ResponseBuilder(),null).getQueriedFromTime(params, Instant.parse("2023-07-21T06:10:29.640750Z"));

		assertThat(checker).isNull();
    }

	@Test
    public void testGetDefaultFromInstantIfNoQueryIsPresentNoQueryReturnsValue() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));
        Instant checker = new RunQueryRoute(new ResponseBuilder(),null).getQueriedFromTime(params, Instant.parse("2023-07-21T06:10:29.640750Z"));

		assertThat(checker).isNotNull();
    }

	@Test
	public void testNoQueryNotSortedWithAcceptHeaderWithDBServiceReturnsOK() throws Exception {
		// Given...
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		int pageSize = 100;
		int pageNo = 1;

		Map<String, String> headerMap = new HashMap<String,String>();
		headerMap.put("Accept","application/json");

		List<IRunResult> mockInputRunResults= new ArrayList<IRunResult>();

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs", headerMap);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an empty page back, because the API server couldn't find any results
		String expectedJson = generateExpectedJson(mockInputRunResults, pageSize, pageNo);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithIncludeCursorReturnsResultsWithNextTokenOK() throws Exception {
		// Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(1,1,1);

        // Build query parameters
        int pageSize = 100;
		Map<String, String[]> parameterMap = setQueryParameter(null,pageSize,null, null,null, 72, null);;
        parameterMap = addQueryParameter(parameterMap, "includeCursor", "true");

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults,mockRequest);
        MockResultArchiveStoreDirectoryService mockRasService = (MockResultArchiveStoreDirectoryService) mockServletEnvironment.getDirectoryService().get(0);

        String nextCursor = "next-page";
        mockRasService.setNextCursor(nextCursor);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		String expectedJson = generateExpectedJson(mockInputRunResults, nextCursor, pageSize);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithInvalidIncludeCursorValueThrowsError() throws Exception {
		// Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(1,1,1);

        // Build query parameters
		Map<String, String[]> parameterMap = setQueryParameter(null,100,null, null,null, 72, null);;
        parameterMap = addQueryParameter(parameterMap, "includeCursor", "notaboolean");

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(400);
		assertThat(outStream.toString()).contains("GAL5090E", "Error parsing the query parameter 'includeCursor'", "Expecting a boolean");
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testQueryWithCursorReturnsResultsWithNextTokenOK() throws Exception {
		// Given..
		List<IRunResult> mockInputRunResults = generateTestDataAscendingTime(1,1,1);

        // Build query parameters
        int pageSize = 100;
		Map<String, String[]> parameterMap = setQueryParameter(null,pageSize,null, null,null, 72, null);;
        parameterMap = addQueryParameter(parameterMap, "cursor", "iwantthispage");

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults,mockRequest);
        MockResultArchiveStoreDirectoryService mockRasService = (MockResultArchiveStoreDirectoryService) mockServletEnvironment.getDirectoryService().get(0);

        String nextCursor = "next-page";
        mockRasService.setNextCursor(nextCursor);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		String expectedJson = generateExpectedJson(mockInputRunResults, nextCursor, pageSize);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testQueryWithUnknownSortThrowsError() throws Exception {
		// Given..
        Instant time1 = Instant.EPOCH;
        Instant time2 = Instant.ofEpochSecond(10);

        String runId1 = "test1";
        String runId2 = "test2";

		List<IRunResult> mockInputRunResults = new ArrayList<>();
        mockInputRunResults.add(createTestRun(runId1, time1, time1, time1));
        mockInputRunResults.add(createTestRun(runId2, time2, time2, time2));

        // Build query parameters
        int pageSize = 100;
        int pageNum = 1;
        String unknownSort = "unknown:desc";
		Map<String, String[]> parameterMap = setQueryParameter(pageNum, pageSize,unknownSort, null,null, 72, null);
        parameterMap.put("runId", new String[] { runId1 + "," + runId2 });

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(400);
		assertThat(outStream.toString()).contains("GAL5011E", "Error parsing the query parameters", "sort value 'unknown' not recognised");
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testQueryWithSortByQueuedTimeAscendingReturnsRunsOk() throws Exception {
		// Given..
        Instant time1 = Instant.EPOCH;
        Instant time2 = Instant.ofEpochSecond(10);
        Instant time3 = Instant.ofEpochSecond(20);

        String runId1 = "test1";
        String runId2 = "test2";
        String runId3 = "test3";

        IRunResult testRun1 = createTestRun(runId1, time1, time1, time1);
        IRunResult testRun2 = createTestRun(runId2, time2, time2, time2);
        IRunResult testRun3 = createTestRun(runId3, time3, time3, time3);

		List<IRunResult> mockInputRunResults = List.of(
            testRun3,
            testRun1,
            testRun2
        );

        // Build query parameters
        int pageSize = 100;
        int pageNum = 1;
        String sort = "from:asc";
		Map<String, String[]> parameterMap = setQueryParameter(pageNum, pageSize, sort, null,null, 72, null);
        parameterMap.put("runId", new String[] { runId1 + "," + runId2 + "," + runId3 });

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
        List<IRunResult> expectedOrderedRunResults = List.of(
            testRun1,
            testRun2,
            testRun3
        );

        String expectedJson = generateExpectedJson(expectedOrderedRunResults, pageSize, pageNum);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testQueryWithSortByQueuedTimeDescendingReturnsRunsOk() throws Exception {
		// Given..
        Instant time1 = Instant.EPOCH;
        Instant time2 = Instant.ofEpochSecond(10);
        Instant time3 = Instant.ofEpochSecond(20);

        String runId1 = "test1";
        String runId2 = "test2";
        String runId3 = "test3";

        IRunResult testRun1 = createTestRun(runId1, time1, time1, time1);
        IRunResult testRun2 = createTestRun(runId2, time2, time2, time2);
        IRunResult testRun3 = createTestRun(runId3, time3, time3, time3);

		List<IRunResult> mockInputRunResults = List.of(
            testRun3,
            testRun1,
            testRun2
        );

        // Build query parameters
        int pageSize = 100;
        int pageNum = 1;
        String sort = "from:desc";
		Map<String, String[]> parameterMap = setQueryParameter(pageNum, pageSize, sort, null,null, 72, null);
        parameterMap.put("runId", new String[] { runId1 + "," + runId2 + "," + runId3 });

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
        List<IRunResult> expectedOrderedRunResults = List.of(
            testRun3,
            testRun2,
            testRun1
        );

        String expectedJson = generateExpectedJson(expectedOrderedRunResults, pageSize, pageNum);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}
}
