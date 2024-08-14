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
import dev.galasa.framework.api.users.mocks.MockUsersServlet;

public class UsersRouteTest extends BaseServletTest {

    Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

    @Test
    public void testUsersGetRequestWithBadParamNameReturnsBadRequest() throws Exception {
        // Given...
        MockUsersServlet servlet = new MockUsersServlet();
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        servlet.setEnvironment(env);

        String requestorLoginId = "notMe";
        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { requestorLoginId });

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/users", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5081,
        // "error_message" : "A request to get the user details for a particular user failed.
        // The query parameter provided is not valid.
        // Supported values for the ‘loginId’ query parameter are : ‘me’."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5081, "GAL5081",
                "A request to get the user details for a particular user failed. The query parameter provided is not valid. Supported values for the ‘loginId’ query parameter are : ‘me’.");
    }

    @Test
    public void testUsersGetRequestWithMiisongOrNullParamReturnsBadRequest() throws Exception {
        // Given...
        MockUsersServlet servlet = new MockUsersServlet();
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        servlet.setEnvironment(env);

        String requestorLoginId = null;
        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { requestorLoginId });

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/users", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5082,
        // "error_message" : "A request to get the user details failed. The request did not supply a `loginId` filter.
        //  A `loginId` query parameter with a value of : ‘me’ was expected....."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5082, "GAL5082",
                "A request to get the user details failed. The request did not supply a ‘loginId’ filter. A ‘loginId’ query parameter with a value of : ‘me’ was expected");
    }

    @Test
    public void testUsersGetRequestWithJwtUsernameNullReturnsBadRequest() throws Exception {
        // Given...
        MockUsersServlet servlet = new MockUsersServlet();
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, null);
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
        // Expecting this json:
        // {
        // "error_code" : 5058,
        // [""GAL5058E: Unable to retrieve a username from the given JWT. No JWT claims
        // to retrieve a username from were provided.
        // This could be because the Galasa Ecosystem is badly configured.
        // Report the problem to your Galasa Ecosystem owner.""]
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5058, "GAL5058E", "Unable to retrieve a username from the given JWT");
    }

    @Test
    public void testUsersGetRequestWithJwtUsernameEmptyStringReturnsBadRequest() throws Exception {
        // Given...
        MockUsersServlet servlet = new MockUsersServlet();
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "");
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
        // Expecting this json:
        // {
        // "error_code" : 5058,
        // [""GAL5058E: Unable to retrieve a username from the given JWT. No JWT claims
        // to retrieve a username from were provided.
        // This could be because the Galasa Ecosystem is badly configured.
        // Report the problem to your Galasa Ecosystem owner.""]
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5058, "GAL5058E", "Unable to retrieve a username from the given JWT");
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
        String expectedPayload = "[\n" +
                "  {\n" +
                "    \"login_id\": \"testRequestor\"\n" +
                "  }\n" +
                "]";

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(gotBackPayload).isEqualTo(expectedPayload);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    }

}
