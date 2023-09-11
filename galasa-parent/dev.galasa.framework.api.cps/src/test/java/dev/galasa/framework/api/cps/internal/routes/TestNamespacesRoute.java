/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.cps.internal.routes;
import dev.galasa.framework.api.cps.internal.CpsServlet;
import dev.galasa.framework.api.cps.internal.CpsServletTest;
import dev.galasa.framework.api.cps.internal.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.cps.internal.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.cps.internal.mocks.MockServletOutputStream;
import dev.galasa.framework.api.cps.internal.routes.TestNamespacesRoute;

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
		// We expect an error back, because the API server couldn't find any RAS database to query
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
}
