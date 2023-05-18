/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

import org.junit.Test;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.framework.api.ras.internal.mocks.*;

import static org.assertj.core.api.Assertions.*;

import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
public class TestRunQuery extends BaseServletTest {	

	private Map<String, String[]> addQueryParameter ( Map<String, String[]> map, String key, String value){
		if (value != null){
			map.put(key, new String[] {value});
		}
		return map;
	}

	private Map<String, String[]> addQueryParameter ( Map<String, String[]> map, String key, Integer value){
		if (value != null){
			map.put(key, new String[] {value.toString()});
		}
		return map;
	}

	private Map<String, String[]> addQueryTimeParameter ( Map<String, String[]> map, String key, Integer value){
		if (value != null){
			String TimeStr = Instant.now().minus(value, ChronoUnit.HOURS).toString();
			map.put(key, new String[] {TimeStr});
		}
		return map;
	}

	private Map<String, String[]> setQueryParameter (Integer page, Integer size,String sort, String runname, String requestor, Integer agemin, Integer agemax){
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		parameterMap = addQueryParameter(parameterMap, "page", page);
		parameterMap = addQueryParameter(parameterMap, "size", size);
		parameterMap = addQueryParameter(parameterMap, "sort", sort);
		parameterMap = addQueryParameter(parameterMap, "runname", runname);
		parameterMap = addQueryParameter(parameterMap, "requestor", requestor);
		parameterMap = addQueryTimeParameter(parameterMap, "from", agemin);
		parameterMap = addQueryTimeParameter(parameterMap, "to", agemax);
		return parameterMap;
	}
	
	public List<IRunResult> generateTestData (int resSize, int passTests ,int hoursDeducted){
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		int passCount = 0;
		// Build the results the DB will return.
		for(int c =0 ; c < resSize; c++){
			String runName = RandomStringUtils.randomAlphanumeric(5);
			String testShortName = RandomStringUtils.randomAlphanumeric(5);
			String requestor = RandomStringUtils.randomAlphanumeric(8);
			String runId = RandomStringUtils.randomAlphanumeric(16);
			TestStructure testStructure = new TestStructure();
			testStructure.setRunName(runName);
			testStructure.setRequestor(requestor);
			testStructure.setTestShortName(testShortName);
			testStructure.setQueued(Instant.now().minus(hoursDeducted, ChronoUnit.HOURS));
			testStructure.setStartTime(Instant.now().minus(hoursDeducted, ChronoUnit.HOURS));
			testStructure.setEndTime(Instant.now().minus(hoursDeducted, ChronoUnit.HOURS));
			if (passCount < passTests){
				testStructure.setResult("Passed");
				passCount ++;
			}else{
				testStructure.setResult("Failed");
			}
			Path artifactRoot = Paths.get(RandomStringUtils.randomAlphanumeric(12));
			String log = RandomStringUtils.randomAlphanumeric(6);
			IRunResult result = new MockRunResult( runId, testStructure, artifactRoot , log);
			mockInputRunResults.add(result);
		}
		return mockInputRunResults;
	}

	public List<String> generateExpectedRunNames(List<IRunResult> expectedRunResults) throws ResultArchiveStoreException{
		List <String> runnames = new ArrayList<String>();
		for (IRunResult run : expectedRunResults){
			runnames.add(run.getTestStructure().getRunName().toString());
		}

		return runnames;
	}

	public String generateExpectedJson (List<IRunResult> mockInputRunResults, String[] pageSize,String[] pageNo){
		int resSize = Integer.parseInt(pageSize[0]);
		double numPages = Math.ceil((double)mockInputRunResults.size()/resSize);
		if (numPages == 0) {
			numPages = 1;
		}
		String jsonResult = "{\n"+
							"  \"pageNum\": "+pageNo[0]+",\n"+
							"  \"pageSize\": "+pageSize[0]+",\n"+
							"  \"numPages\": "+(int)numPages+",\n"+
							"  \"amountOfRuns\": "+mockInputRunResults.size()+",\n"+
							"  \"runs\": [";
		if (mockInputRunResults.size()>0){
			int iter;
			if (resSize < mockInputRunResults.size() ){
				iter = resSize;
			}else{
				iter= mockInputRunResults.size();
			}
			int pagedResult = (Integer.parseInt(pageNo[0])-1)*resSize;
			for ( int c= 0; c< iter; c++ ){
				String runData ="";
				if (0<c && c<iter){
					runData =",\n";
				}else if(c==0){
					runData ="\n";
				}
				try {
					runData = runData+ "    {\n"+
						   "      \"runId\": \""+mockInputRunResults.get(c+pagedResult).getRunId()+"\",\n"+
						   "      \"testStructure\": {\n"+
						   "        \"runName\": \""+mockInputRunResults.get(c+pagedResult).getTestStructure().getRunName()+"\",\n"+
						   "        \"testShortName\": \""+mockInputRunResults.get(c+pagedResult).getTestStructure().getTestShortName()+"\",\n"+
						   "        \"requestor\": \""+mockInputRunResults.get(c+pagedResult).getTestStructure().getRequestor()+"\",\n"+
						   "        \"result\": \""+mockInputRunResults.get(c+pagedResult).getTestStructure().getResult()+"\",\n"+
						   "        \"queued\": \""+mockInputRunResults.get(c+pagedResult).getTestStructure().getQueued()+"\",\n"+
						   "        \"startTime\": \""+mockInputRunResults.get(c+pagedResult).getTestStructure().getStartTime()+"\",\n"+
						   "        \"endTime\": \""+mockInputRunResults.get(c+pagedResult).getTestStructure().getEndTime()+"\"\n"+
						   "      }\n"+
						   "    }";
				} catch (ResultArchiveStoreException e) {
					e.printStackTrace();
				}
				jsonResult = jsonResult+runData;
			}
			jsonResult= jsonResult+"\n  ]\n}";
		}else{
			jsonResult= jsonResult+"]\n}";
		}
		return jsonResult;
	}

	private void testQueryParametersReturnsError(
		Map<String, String[]> parameterMap ,
		int expectedErrorCode,
		String... expectedErrorMsgSubStrings
	) throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(20,10,1);

		String[] sortValues = {"result:asc"};
		parameterMap.put("sort", sortValues );

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
		
		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()==400);

		checkErrorStructure(
			outStream.toString(),
			expectedErrorCode,
			expectedErrorMsgSubStrings
		);

		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	/* 
	*TESTS 
	*/

	@Test
	public void testQueryWithRequestorNotSortedButNoDBServiceReturnsError() throws Exception {
		// Given...
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc", null,"mickey", 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap,"/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment(null,mockRequest);

		// Oh no ! There are no directory services which represent a RAS store !
		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any RAS database to query
		assertThat(resp.getStatus()==500);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5003,
			"GAL5003E: ",
			"Error retrieving runs"
		);
	}

	@Test
	public void testQueryWithFailedRequestReturnsGenericError() throws Exception {
		// Given...
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(null,"/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment(null, mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
		
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any RAS database to query
		assertThat(resp.getStatus()==500);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

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
		String[] pageSize = {"100"};
		String[] pageNo = {"1"};

		List<IRunResult> mockInputRunResults= new ArrayList<IRunResult>();

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment(mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
		
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an empty page back, because the API server couldn't find any results
		String expectedJson = generateExpectedJson(mockInputRunResults, pageSize, pageNo);
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithEmptyDBServiceReturnsOK() throws Exception {
		// Given...
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc", null,"mickey", 72, null);
		
		List<IRunResult> mockInputRunResults= new ArrayList<IRunResult>();
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
		String[] pageSize = {"100"};
		String[] pageNo = {"1"};

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
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceWithOneRecordReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(1,1,1);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc", null,"mickey", 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		String[] pageSize = {"100"};
		String[] pageNo = {"1"};
		BaseServlet servlet = mockServletEnvironment.getServlet();
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
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10,2,1);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc", null,"mickey", 72, null);

		String[] pageSize = {"100"};
		String[] pageNo = {"1"};
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
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
		String expectedJson = generateExpectedJson(mockInputRunResults,pageSize,pageNo);
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithFromDateNotSortedWithDBServiceWithOneRecordReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(1,1,1);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc", null,"mickey", 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		String[] pageSize = {"100"};
		String[] pageNo = {"1"};
		BaseServlet servlet = mockServletEnvironment.getServlet();
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
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithoutFromDateOrRunNameNotSortedWithDBServiceTenRecordsReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10,2, 48);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc", null ,null, null, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:

		//Then...
		assertThat(resp.getStatus()==400);
		assertThat( outStream.toString() ).contains("GAL5010E: Error parsing the query parameters. from time is a mandatory field if no runname is supplied.");
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testNoQueryWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10,2, 15);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(null,null,null, null ,null, null, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:

		//Then...
		String expectedJson = generateExpectedJson(mockInputRunResults,new String[] {"100"},new String[] {"1"});
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithoutFromDateWithRunNameNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10,2, 48);
		//Build Http query parameters
		IRunResult run = mockInputRunResults.get(1);
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc",run.getTestStructure().getRunName(),"mickey", 72, null);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
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
		String expectedJson = generateExpectedJson(expectedRun,parameterMap.get("size"),parameterMap.get("page"));
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithFromDateAndRunnameNotSortedWithDBServiceTenRecordsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10,2, 48);
		//Build Http query parameters
		IRunResult run = mockInputRunResults.get(5);
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc",run.getTestStructure().getRunName(),null, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
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
		String expectedJson = generateExpectedJson(expectedRun,parameterMap.get("size"),parameterMap.get("page"));
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTenRecordsPageSizeFiveReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10,5,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc",null,requestor, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
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
		String expectedJson = generateExpectedJson(mockInputRunResults,parameterMap.get("size"),parameterMap.get("page"));
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTenRecordsPageSizeFivePageTwoReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10,5,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(2,5,"asc",null,requestor, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
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
		String expectedJson = generateExpectedJson(mockInputRunResults,parameterMap.get("size"),parameterMap.get("page"));
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTwentyRecordsPageSizeFivePageThreeReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(20,5,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(3,5,"asc",null,requestor, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
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
		String expectedJson = generateExpectedJson(mockInputRunResults,parameterMap.get("size"),parameterMap.get("page"));
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTwentyRecordsPageSizeFivePageFiveReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(20,3,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(5,5,"asc",null,requestor, 72, null);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()==500);

		checkErrorStructure(outStream.toString(), 5004, "GAL5004E: ", "Error retrieving page.");

		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithResultsSortedWithDBServiceTenRecordsPageSize100FalseFromDateReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(20,10,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"result:asc",null,requestor, null, null);
		String[] fromTime = {"erroneousValue"};
		parameterMap.put("from", fromTime);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
		
		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()==500);

		checkErrorStructure(
			outStream.toString(),
			5001,
			"GAL5001E:","Error parsing the date-time field","'from'",fromTime[0]
		);

		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithResultsSortedWithDBServiceTenRecordsPageSize100FalseToDateReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(20,10,1);
		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc",null,requestor, null, null);
		String[] toTime = {"erroneousValue"};
		parameterMap.put("to", toTime);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
		
		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()==500);

		checkErrorStructure(
			outStream.toString(),
			5001,
			"GAL5001E:","Error parsing the date-time field","'to'",toTime[0]
		);

		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithResultsSortedWithDBServiceTenRecordsPageSize100FalseRunIDReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(20,10,1);

		MockResultArchiveStoreDirectoryService storeWhichThrowsUp = new MockResultArchiveStoreDirectoryService(null) {
			@Override
			public IRunResult getRunById(@NotNull String runId) throws ResultArchiveStoreException {
				throw new ResultArchiveStoreException();
			}
		};

		//Build Http query parameters
		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc",null,requestor, 72, null);;
		String[] runId = {"erroneousrunId"};
		parameterMap.put("runId", runId);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest,storeWhichThrowsUp);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
		
		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()==404);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

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
	public void testQueryWithMultipleResultsReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two results ! should be invalid !
		String[] results = new String[] {"resultA","resultB"};
		parameterMap.put("result",  results);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'result'","Duplicate");
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
		List<IRunResult> mockInputRunResults = generateTestData(10,2, 72);
		List<IRunResult> expectedInputRunResults = generateTestData(10,2, 48);
		List<IRunResult> mockInputRunResults3 = generateTestData(10,2, 24);
		mockInputRunResults.addAll(mockInputRunResults3);
		List<IRunResult> excludedRuns = new ArrayList<IRunResult>();
		excludedRuns.addAll(mockInputRunResults);
		mockInputRunResults.addAll(expectedInputRunResults);
		//Build Http query parameters
		Map<String, String[]> parameterMap = setQueryParameter(1,100,"asc",null, null, 72, 36);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment( mockInputRunResults,mockRequest);
		
		BaseServlet servlet = mockServletEnvironment.getServlet();
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
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).contains(expectedRunNames);
		assertThat( outStream.toString() ).doesNotContain(excludedRunNames);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

}
