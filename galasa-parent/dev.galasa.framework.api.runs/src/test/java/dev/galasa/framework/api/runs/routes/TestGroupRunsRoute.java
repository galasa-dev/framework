/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import dev.galasa.framework.api.common.mocks.MockIRun;
import dev.galasa.framework.api.runs.RunsServletTest;
import dev.galasa.framework.api.runs.mocks.MockRunsServlet;
import dev.galasa.framework.spi.IRun;

public class TestGroupRunsRoute extends RunsServletTest {

    List<IRun> runs = new ArrayList<IRun>();

    protected void addRun(String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName){
		this.runs.add(new MockIRun( runName, runType, requestor, test, runStatus, bundle, testClass, groupName));
    }

    @Test
    public void TestGetRunsNoFrameworkReturnsError() throws Exception {
        //Given...
        setServlet("group", null, null);
        MockRunsServlet servlet = getServlet();
        HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	

        //When...
        servlet.init();
		servlet.doGet(req,resp);

        //Then...
        assertThat(resp.getStatus()).isEqualTo(500);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E: ",
			"Error occured when trying to access the endpoint"
		);
    }

    @Test
    public void TestGetRunsWithInvalidGroupNameReturnsError() throws Exception {
        // Given...
        // /runs/empty is an empty runs set and should return an error as runs can not be null
		String groupName = "invalid";
        setServlet(groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(500);

		checkErrorStructure(
			outStream.toString(),
			5019, "E: Unable to retrieve runs for Run Group: 'invalid'."
		);
    }

    @Test
    public void TestGetRunsWithEmptyGroupNameReturnsOK() throws Exception {
        // Given...
        // /runs/empty is an empty runs set and should return an error as runs can not be null
		String groupName = "empty";
        setServlet(groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo("{\n  \"complete\": true,\n  \"runs\": []\n}");
    }

    @Test
    public void TestGetRunsWithValidGroupNameReturnsOk() throws Exception {
        // Given...
		String groupName = "framework";
        //String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName
        addRun("name1", "type1", "requestor1", "test1", "FINISHED",
               "bundle1", "testClass1", groupName);
        setServlet(groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "true");
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void TestGetRunsWithValidGroupNameReturnsMultiple() throws Exception {
        // Given...
		String groupName = "framework";
        //String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName
        addRun("name1", "type1", "requestor1", "test1", "BUILDING",
               "bundle1", "testClass1", groupName);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING",
               "bundle2", "testClass2", groupName);
        setServlet(groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }


     @Test
    public void TestGetRunsWithValidGroupNameMultipleWithFinishedRunReturnsCompleteFalse() throws Exception {
        // Given...
		String groupName = "framework";
        //String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName
        addRun("name1", "type1", "requestor1", "test1", "BUILDING",
               "bundle1", "testClass1", groupName);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING",
               "bundle2", "testClass2", groupName);
        addRun("name3", "type3", "requestor3", "test3", "FINISHED",
               "bundle3", "testClass3", groupName);
        addRun("name4", "type4", "requestor4", "test4", "BUILDING",
               "bundle4", "testClass4", groupName);
        addRun("name5", "type6", "requestor5", "test5", "BUILDING",
               "bundle5", "testClass6", groupName);
        addRun("name6", "type6", "requestor6", "test6", "BUILDING",
               "bundle6", "testClass6", groupName);
        addRun("name7", "type7", "requestor7", "test7", "BUILDING",
               "bundle7", "testClass7", groupName);
        addRun("name8", "type8", "requestor8", "test8", "BUILDING",
               "bundle8", "testClass8", groupName);
        addRun("name9", "type9", "requestor9", "test9", "BUILDING",
               "bundle9", "testClass9", groupName);
        addRun("name10", "type10", "requestor10", "test10", "BUILDING",
               "bundle10", "testClass10", groupName);
        setServlet(groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

// Framework not there
// valid groupName
// invalid groupName
// empty groupname - error
// 

}
