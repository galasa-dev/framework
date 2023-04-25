/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

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
import javax.validation.constraints.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
public class TestRunQuery {	

	public class TestParameters {
		

		MockFramework mockFramework;
		MockArchiveStore archiveStore;
		RunQuery servlet;

		HttpServletRequest req;
		List<IResultArchiveStoreDirectoryService> directoryServices;
		List<IRunResult> mockInputRunResults;
		ByteArrayOutputStream outStream;
		PrintWriter writer;
			
		HttpServletResponse resp;

		public TestParameters(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap){ 
			this(mockInpResults, parameterMap, new MockResultArchiveStoreDirectoryService(mockInpResults));
		}

		public TestParameters(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap, MockResultArchiveStoreDirectoryService rasStore ){ 
			this.setMockInputs(mockInpResults);
			this.directoryServices = getDirectoryService(rasStore);
			this.setArchiveStore(new MockArchiveStore(this.directoryServices));
			this.mockFramework = new MockFramework(this.archiveStore);
			this.req = new MockHttpServletRequest(parameterMap);
			this.outStream = new ByteArrayOutputStream();
			this.writer = new PrintWriter(this.outStream);
			this.resp = new MockHttpServletResponse(this.writer);
			this.servlet = new RunQuery();
			this.servlet.framework = (IFramework) this.mockFramework;
		}

		public HttpServletResponse getResponse (){
			return this.resp;
		}

		public HttpServletRequest getRequest (){
			return this.req;
		}

		public RunQuery getServlet(){
			return this.servlet;
		}

		public ByteArrayOutputStream getOutStream(){
			return this.outStream;
		}

		public void setMockInputs(List<IRunResult> mockInpResults){
			this.mockInputRunResults = mockInpResults;
		}

		public List<IResultArchiveStoreDirectoryService> getDirectoryService(MockResultArchiveStoreDirectoryService rasStore){
			List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
			directoryServices.add(rasStore);
			return directoryServices;
		}

		public void setArchiveStore(MockArchiveStore store){
			this.archiveStore = store;
		}

	}

	public IRunResult createRunResult (
		String runName,
		String requestor,
		String runId,
		String testShortName,
		String result
		){
		
		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		testStructure.setTestShortName(testShortName);
		testStructure.setResult(result);
		Path artifactRoot = Paths.get(RandomStringUtils.randomAlphanumeric(12));
		String log = RandomStringUtils.randomAlphanumeric(6);

		IRunResult mockRunResult = new MockRunResult( runId, testStructure, artifactRoot , log);
		return mockRunResult;
	}

	public List<IRunResult> generateTestData (int resSize, int passTests){
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
						   "        \"result\": \""+mockInputRunResults.get(c+pagedResult).getTestStructure().getResult()+"\"\n"+
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

	void checkErrorStructure(String jsonString , int expectedErrorCode , String... expectedErrorMessageParts ) throws Exception {

		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonObject jsonObject = jsonElement.getAsJsonObject();
		assertThat(jsonObject).isNotNull().as("Json parsed is not a json object.");

		// Check the error code
		JsonElement errorCodeField = jsonObject.get("error_code");
		assertThat(errorCodeField).isNotNull().as("Returned structure didn't contain the error_code field!");

		int actualErrorCode = jsonObject.get("error_code").getAsInt();
		assertThat(actualErrorCode).isEqualTo(expectedErrorCode);

		// Check the error message...
		String msg = jsonObject.get("error_message").toString();
		for ( String expectedMessagePart : expectedErrorMessageParts ) {
			assertThat(msg).contains(expectedMessagePart);
		}
	}

	@Test
	public void testQueryWithRequestorNotSortedButNoDBServiceReturnsError() throws Exception {
		// Given...
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		String[] requestorValues = {"mickey"};
		String[] sortValues = {"asc"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );

		TestParameters testParameters = new TestParameters(null,parameterMap);

		// Oh no ! There are no directory services which represent a RAS store !
		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();
				
		// When...
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
		// Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		// String[] requestorValues = {"mickey"};
		// String[] sortValues = {"asc"};
		// parameterMap.put("requestor", requestorValues );
		// parameterMap.put("sort", sortValues );

		TestParameters testParameters = new TestParameters(null,null);

		// Oh no ! There are no directory services which represent a RAS store !
		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();
				
		// When...
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

		TestParameters testParameters = new TestParameters(mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();
		
		// When...
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
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		String[] requestorValues = {"mickey"};
		String[] sortValues = {"asc"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );

		
		List<IRunResult> mockInputRunResults= new ArrayList<IRunResult>();
		
		TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();
		String[] pageSize = {"100"};
		String[] pageNo = {"1"};
		// When...
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
		List<IRunResult> mockInputRunResults = generateTestData(1,1);
		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		String[] requestorValues = {requestor};
		String[] sortValues = {"asc"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );


		TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap);

		String[] pageSize = {"100"};
		String[] pageNo = {"1"};
		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();

		//When...
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
		List<IRunResult> mockInputRunResults = generateTestData(10,2);
		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		String[] requestorValues = {requestor};
		String[] sortValues = {"asc"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );

		String[] pageSize = {"100"};
		String[] pageNo = {"1"};
		TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();

		//When...
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
	public void testQueryWithRequestorNotSortedWithDBServiceTenRecordsPageSizeFiveReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10,5);
		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		String[] requestorValues = {requestor};
		String[] pageSize = {"5"};
		String[] sortValues = {"asc"};
		String[] pageNo = {"1"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );
		parameterMap.put("size",pageSize);


		TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();

		//When...
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
		String expectedJson = generateExpectedJson(mockInputRunResults,pageSize, pageNo);
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTenRecordsPageSizeFivePageTwoReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10,5);
		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		String[] requestorValues = {requestor};
		String[] pageSize = {"5"};
		String[] sortValues = {"asc"};
		String[] pageNo = {"2"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );
		parameterMap.put("size",pageSize);
		parameterMap.put("page",pageNo);


		TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();

		//When...
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
		String expectedJson = generateExpectedJson(mockInputRunResults,pageSize,pageNo);
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTwentyRecordsPageSizeFivePageThreeReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(20,5);
		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		String[] requestorValues = {requestor};
		String[] pageSize = {"5"};
		String[] sortValues = {"asc"};
		String[] pageNo = {"3"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );
		parameterMap.put("size",pageSize);
		parameterMap.put("page",pageNo);


		TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();

		//When...
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
		String expectedJson = generateExpectedJson(mockInputRunResults,pageSize,pageNo);
		assertThat(resp.getStatus()==200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testQueryWithRequestorNotSortedWithDBServiceTwentyRecordsPageSizeFivePageFiveReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(20,3);
		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		String[] requestorValues = {requestor};
		String[] pageSize = {"5"};
		String[] sortValues = {"asc"};
		String[] pageNo = {"5"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );
		parameterMap.put("size",pageSize);
		parameterMap.put("page",pageNo);

		TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();

		//When...
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
		List<IRunResult> mockInputRunResults = generateTestData(20,10);
		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		String[] requestorValues = {requestor};
		String[] sortValues = {"result:asc"};
		String[] fromTime = {"erroneousValue"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );
		parameterMap.put("from", fromTime);

		TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();
		
		//When...
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
		List<IRunResult> mockInputRunResults = generateTestData(20,10);
		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		String[] requestorValues = {requestor};
		String[] sortValues = {"result:asc"};
		String[] toTime = {"erroneousValue"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );
		parameterMap.put("to", toTime);

		TestParameters testParameters = new TestParameters(mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();
		
		//When...
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
		List<IRunResult> mockInputRunResults = generateTestData(20,10);

		MockResultArchiveStoreDirectoryService storeWhichThrowsUp = new MockResultArchiveStoreDirectoryService(null) {
			@Override
			public IRunResult getRunById(@NotNull String runId) throws ResultArchiveStoreException {
				throw new ResultArchiveStoreException();
			}
		};

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		String[] requestorValues = {requestor};
		String[] sortValues = {"result:asc"};
		String[] runId = {"erroneousrunId"};
		parameterMap.put("requestor", requestorValues );
		parameterMap.put("sort", sortValues );
		parameterMap.put("runId", runId);

		TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap,storeWhichThrowsUp);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();
		
		//When...
		servlet.doGet(req,resp);

		//Then...
		assertThat(resp.getStatus()==404);
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(), 
			5002, 
			"GAL5002E:",
			"Error retrieving ras run from RunID",
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

		// Two requestors ! should be invalid !
		String[] testNames = new String[] {"testA","testB"};
		parameterMap.put("testname",  testNames);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'testname'","Duplicate");
	}


		@Test
	public void testQueryWithMultipleBundlesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two requestors ! should be invalid !
		String[] bundles = new String[] {"bundleA","bundleB"};
		parameterMap.put("bundle",  bundles);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'bundle'","Duplicate");
	}

	@Test
	public void testQueryWithMultipleResultsReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two requestors ! should be invalid !
		String[] results = new String[] {"resultA","resultB"};
		parameterMap.put("result",  results);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'result'","Duplicate");
	}

	@Test
	public void testQueryWithMultipleRunNamesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two requestors ! should be invalid !
		String[] runNames = new String[] {"runnameA","runnameB"};
		parameterMap.put("runname",  runNames);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'runname'","Duplicate");
	}
	

	@Test
	public void testQueryWithMultipleToTimesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two requestors ! should be invalid !
		String[] toTimes = new String[] {"2023-04-11T09:42:06.589180Z","2023-04-21T06:00:54.597509Z"};
		parameterMap.put("to",  toTimes);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'to'","Duplicate");
	}

	@Test
	public void testQueryWithMultipleFromTimesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two requestors ! should be invalid !
		String[] fromTimes = new String[] {"2023-04-11T09:42:06.589180Z","2023-04-21T06:00:54.597509Z"};
		parameterMap.put("from",  fromTimes);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'from'","Duplicate");
	}

	@Test
	public void testQueryWithMultiplePageSizesReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two requestors ! should be invalid !
		String[] pageSizes = new String[] {"5","10"};
		parameterMap.put("size",  pageSizes);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'size'","Duplicate");
	}

	@Test
	public void testQueryWithMultiplePageNumReturnsError () throws Exception {

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		// Two requestors ! should be invalid !
		String[] pageNums = new String[] {"5","10"};
		parameterMap.put("page",  pageNums);

		testQueryParametersReturnsError(parameterMap ,5006, "GAL5006E:","'page'","Duplicate");
	}

	// @Test
	// public void testQueryWithMultipleSortReturnsError () throws Exception {

	// 	//Build Http query parameters
	// 	Map<String, String[]> parameterMap = new HashMap<String,String[]>();

	// 	// Two requestors ! should be invalid !
	// 	String[] sortValues = new String[] {"result:asc", "testclass:desc"};
	// 	parameterMap.put("sort",  sortValues);
		


	// 	//Given..
	// 	List<IRunResult> mockInputRunResults = new ArrayList<>();
		
	// 	IRunResult mockResultTestA = createRunResult("C0001", "galasa", "abc-123", "testA", "Failed");
	// 	IRunResult mockResultTestB = createRunResult("C0002", "galasa", "abc-345", "testB", "Failed");
	// 	IRunResult mockResultTestC = createRunResult("C0003", "galasa", "abc-567", "testC", "Passed");
	// 	IRunResult mockResultTestD = createRunResult("C0004", "galasa", "abc-789", "testD", "Passed");
	// 	mockInputRunResults.add(mockResultTestA);
	// 	mockInputRunResults.add(mockResultTestB);
	// 	mockInputRunResults.add(mockResultTestC);
	// 	mockInputRunResults.add(mockResultTestD);
		
	// 	String[] pageSize = {"100"};
	// 	String[] pageNo = {"1"};
		
	// 	TestParameters testParameters = new TestParameters( mockInputRunResults,parameterMap);

	// 	RunQuery servlet = testParameters.getServlet();
	// 	HttpServletRequest req = testParameters.getRequest();
	// 	HttpServletResponse resp = testParameters.getResponse();
	// 	ByteArrayOutputStream outStream = testParameters.getOutStream();


	// 	//When...
	// 	servlet.doGet(req,resp);

	// 	//Then...
	// 	// Expecting:
	// 	//  {
	// 	//   "pageNum": 1,
	// 	//   "pageSize": 100,
	// 	//   "numPages": 1,
	// 	//   "amountOfRuns": 10,
	// 	//   "runs": [
	// 	//     {
	// 	//       "runId": "xxx-yyy-zzz-012345",
	// 	//       "testStructure": {
	// 	//         "runName": "A1234",
	// 	//         "requestor": "mickey"
	// 	//       }
	// 	//     ....
	// 	//     {
	// 	//       "runId": "xxx-yyy-zzz-012345",
	// 	//       "testStructure": {
	// 	//         "runName": "A1244",
	// 	//         "requestor": "mickey"
	// 	//       }
	// 	// 	   }
	// 	// 	]
	// 	// }
	// 	List<IRunResult> mockOrderedInputRunResults = new ArrayList<>();
	// 	mockOrderedInputRunResults.add(mockResultTestB);
	// 	mockOrderedInputRunResults.add(mockResultTestA);
	// 	mockOrderedInputRunResults.add(mockResultTestD);
	// 	mockOrderedInputRunResults.add(mockResultTestC);
	// 	String expectedJson = generateExpectedJson(mockOrderedInputRunResults, pageSize, pageNo);;
	// 	assertThat(resp.getStatus()==200);
	// 	assertThat( outStream.toString() ).isEqualTo(expectedJson);
	// 	assertThat( resp.getContentType()).isEqualTo("Application/json");
	// 	assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		
	// }

	private void testQueryParametersReturnsError(
		Map<String, String[]> parameterMap , 
		int expectedErrorCode, 
		String... expectedErrorMsgSubStrings
	) throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(20,10);
		
		// String requestor = mockInputRunResults.get(0).getTestStructure().getRequestor();
		// String[] requestorValues = {requestor};
		// parameterMap.put("requestor", requestorValues );
 
		String[] sortValues = {"result:asc"};
		parameterMap.put("sort", sortValues );
		

		TestParameters testParameters = new TestParameters(mockInputRunResults,parameterMap);

		RunQuery servlet = testParameters.getServlet();
		HttpServletRequest req = testParameters.getRequest();
		HttpServletResponse resp = testParameters.getResponse();
		ByteArrayOutputStream outStream = testParameters.getOutStream();
		
		//When...
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

}

