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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.PrintWriter;

import org.apache.commons.lang3.*;

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

	public List<IRunResult> generateTestData (int resSize, int passTests){
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		int passCount = 0;
		// Build the results the DB will return.
		for(int c =0 ; c < resSize; c++){
			String runName = RandomStringUtils.randomAlphanumeric(5);
			String requestor = RandomStringUtils.randomAlphanumeric(8);
			String runId = RandomStringUtils.randomAlphanumeric(16);
			TestStructure testStructure = new TestStructure();
			testStructure.setRunName(runName);
			testStructure.setRequestor(requestor);
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

	void checkErrorStructure(String jsonString , int expectedErrorCode , String... expectedErrorMessageParts ) throws Exception {

		JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

		// Check the error code
		int actualErrorCode = jsonObject.get("error_code").getAsInt();
		assertThat(actualErrorCode).isEqualTo(expectedErrorCode);

		// Check the error message...
		String msg = jsonObject.get("error_message").toString();
		for ( String expectedMessagePart : expectedErrorMessageParts ) {
			assertThat(msg).contains(expectedMessagePart);
		}
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
}