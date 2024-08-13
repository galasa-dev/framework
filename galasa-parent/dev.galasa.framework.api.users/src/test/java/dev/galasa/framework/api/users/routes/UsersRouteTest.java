/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;

import org.junit.Test;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.users.UsersServlet;
import dev.galasa.framework.api.users.mocks.MockUsersServlet;

public class UsersRouteTest extends BaseServletTest {

    Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);


    @Test
    public void testUsersGetRequestWithMissingNameParamReturnsBadRequest() throws Exception {
        // Given...
        UsersServlet servlet = new UsersServlet();

        Map<String, String[]> queryParams = new HashMap<>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/users");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/users/name'. Please check your request parameters or report the problem to
        // your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occured when trying to execute request");
    }

    @Test
    public void testUsersGetRequestReturnsArrayOfUsersReturns_OK() throws Exception {
        // Given...
        MockUsersServlet servlet = new MockUsersServlet();
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        servlet.setEnvironment(env);

        String requestorLoginId = "me";

        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { requestorLoginId });

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/users", headerMap);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting an array of json objects:
        // [{
        // "login_id": "testRequestor",
        // }]

        String gotBackPayload = outStream.toString();
        String expectedPayload = 
            "[\n"+
            "  {\n"+
            "    \"login_id\": \"testRequestor\"\n"+
            "  }\n"+
            "]";

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(gotBackPayload).isEqualTo(expectedPayload);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    }

}
