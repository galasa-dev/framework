/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

import org.junit.Test;

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
public class TestRunQuery extends BaseServletTest {	


	/** 
	 * A subclass of the servlet we want to test, so that we can inject the mock framework in without 
	 * adding any extra code to the production servlet class. The framework field is protected scope, 
	 * so a subclass can do the injection instead of the injection framework.
	 */
	class MockRunQueryServlet extends RunQuery implements IServletUnderTest {
		@Override
		public void setFramework(IFramework framework) {
			super.framework = framework;
		}
	}

	class MockRunQueryServletEnvironment extends MockServletBaseEnvironment {

		public MockRunQueryServletEnvironment(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap){ 
        	super(mockInpResults, parameterMap, new MockResultArchiveStoreDirectoryService(mockInpResults));
    	}

		public MockRunQueryServletEnvironment(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap, MockResultArchiveStoreDirectoryService rasStore ){ 
			super(mockInpResults, parameterMap, rasStore);
		}

		public RunQuery getServlet() {
			return (RunQuery) super.getBaseServlet();
		}

		@Override
		public IServletUnderTest createServlet() {
        	return new MockRunQueryServlet();
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

		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment(null,parameterMap);

		// Oh no ! There are no directory services which represent a RAS store !
		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
				
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
	public void testNoQueryNotSortedWithDBServiceReturnsOK() throws Exception {
		// Given...
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		String[] pageSize = {"100"};
		String[] pageNo = {"1"};
		
		List<IRunResult> mockInputRunResults= new ArrayList<IRunResult>();

		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment(mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
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
		
		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment( mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
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


		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment( mockInputRunResults,parameterMap);

		String[] pageSize = {"100"};
		String[] pageNo = {"1"};
		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

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
		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment( mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

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


		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment( mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

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


		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment( mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

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


		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment( mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

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

		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment( mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();

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

		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment( mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
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

		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment(mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
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

		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment( mockInputRunResults,parameterMap,storeWhichThrowsUp);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
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
		

		MockRunQueryServletEnvironment mockServletEnvironment = new MockRunQueryServletEnvironment(mockInputRunResults,parameterMap);

		RunQuery servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ByteArrayOutputStream outStream = mockServletEnvironment.getOutStream();
		
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

