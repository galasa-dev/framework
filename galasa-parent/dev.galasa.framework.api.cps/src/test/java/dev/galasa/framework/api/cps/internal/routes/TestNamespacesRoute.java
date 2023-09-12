/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.cps.internal.routes;
import dev.galasa.framework.api.cps.internal.CpsServlet;
import dev.galasa.framework.api.cps.internal.CpsServletTest;
import dev.galasa.framework.api.cps.internal.mocks.MockCpsServlet;
import dev.galasa.framework.api.cps.internal.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.cps.internal.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.cps.internal.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.cps.internal.mocks.MockServletOutputStream;
import dev.galasa.framework.api.cps.internal.routes.TestNamespacesRoute;
import dev.galasa.framework.api.cps.internal.mocks.MockFramework;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

import static org.assertj.core.api.Assertions.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class TestNamespacesRoute extends CpsServletTest{
    
    @Test
    public void TestGetNamespacesNoFrameworkReturnError () throws Exception{
		// Given...
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

        ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
		// Oh no ! There are no etcd services which represent a CPS store !
		CpsServlet servlet = new CpsServlet();
		HttpServletRequest req = new MockHttpServletRequest(parameterMap,"/cps");
		HttpServletResponse resp = new MockHttpServletResponse(writer, outStream);
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to query
		assertThat(resp.getStatus()==500);
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
	public void TestGetNamespacesWithFrameworkNoDataReturnsOk() throws Exception{
		// Given...
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

        ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
		IConfigurationPropertyStoreService cpsstore = new MockIConfigurationPropertyStoreService("empty");
		IFramework framework = new MockFramework(cpsstore);
		MockCpsServlet servlet = new MockCpsServlet();
		servlet.setFramework(framework);
		HttpServletRequest req = new MockHttpServletRequest(parameterMap,"/cps");
		HttpServletResponse resp = new MockHttpServletResponse(writer, outStream);
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(outStream.toString()).isEqualTo("[]");
	}

	@Test
	public void TestGetNamespacesWithFrameworkWithDataReturnsOk() throws Exception{
		// Given...
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

        ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
		IConfigurationPropertyStoreService cpsstore = new MockIConfigurationPropertyStoreService("framework");
		IFramework framework = new MockFramework(cpsstore);
		MockCpsServlet servlet = new MockCpsServlet();
		servlet.setFramework(framework);
		HttpServletRequest req = new MockHttpServletRequest(parameterMap,"/cps");
		HttpServletResponse resp = new MockHttpServletResponse(writer, outStream);
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(outStream.toString()).isEqualTo("[\n  \"nampespace1\","+
				"\n  \"nampespace2\",\n  \"nampespace3\",\n  \"nampespace4\","+
				"\n  \"nampespace5\",\n  \"nampespace6\",\n  \"nampespace7\"\n]");
	}

	@Test
	public void TestGetNamespacesWithFrameworkNullNamespacesReturnsError() throws Exception{
		// Given...
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

        ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
		IConfigurationPropertyStoreService cpsstore = new MockIConfigurationPropertyStoreService("error");
		IFramework framework = new MockFramework(cpsstore);
		MockCpsServlet servlet = new MockCpsServlet();
		servlet.setFramework(framework);
		HttpServletRequest req = new MockHttpServletRequest(parameterMap,"/cps");
		HttpServletResponse resp = new MockHttpServletResponse(writer, outStream);
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server has thrown a ConfigurationPropertyStoreException
		assertThat(resp.getStatus()==500);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5015,
			"E: Error occured when trying to access the Configuration Property Store.",
			" Report the problem to your Galasa Ecosystem owner."
		);
    }
}
