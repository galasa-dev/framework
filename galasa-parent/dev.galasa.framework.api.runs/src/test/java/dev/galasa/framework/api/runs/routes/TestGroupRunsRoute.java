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

    protected List<IRun> addRun(String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName){
		this.runs.add(new MockIRun( runName, runType, requestor, test, runStatus, bundle, testClass, groupName));
		return this.runs;
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
        assertThat(resp.getStatus()).isEqualTo(500);

		checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E: ",
			"Error occured when trying to access the endpoint"
		);
        
    }

    @Test
    public void TestGetRunsWithValidGroupNameReturnsOk() throws Exception {
        // Given...
		String groupName = "framework";
        //String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName
        addRun("name1", "type1", "requestor1", "test1", "BUILDING",
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
        assertThat(resp.getStatus()).isEqualTo(200);
        
    }

// Framework not there
// valid groupName
// invalid groupName
// empty groupname - error
// 

}
