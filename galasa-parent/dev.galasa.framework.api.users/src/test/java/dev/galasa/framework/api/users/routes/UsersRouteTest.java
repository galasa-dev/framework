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

import java.time.Instant;
import org.junit.Test;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.mocks.MockAuthStoreService;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockTimeService;
import dev.galasa.framework.api.users.mocks.MockUsersServlet;

public class UsersRouteTest extends BaseServletTest {

    Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

    
    @Test
    public void testUsersGetRequestReturnsUserByLoginIdReturns_OK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework framework = new MockFramework(authStoreService);
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        MockUsersServlet servlet = new MockUsersServlet(framework, env);

        String requestorLoginId = "admin";

        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { requestorLoginId });

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        String gotBackPayload = outStream.toString();
        String expectedPayload = "[\n" +
                "  {\n" +
                "    \"login-id\": \"admin\",\n" +
                "    \"id\": \"docid\",\n" +
                "    \"clients\": [\n" +
                "      {\n" +
                "        \"last-login\": \"2024-10-18T14:49:50.096329Z\",\n" +
                "        \"client-name\": \"web-ui\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(gotBackPayload).isEqualTo(expectedPayload);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    }

    @Test
    public void testUsersGetRequestReturnsAllUsersReturns_OK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework framework = new MockFramework(authStoreService);
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        MockUsersServlet servlet = new MockUsersServlet(framework, env);


        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        String gotBackPayload = outStream.toString();
        String expectedPayload = "[\n" +
                "  {\n" +
                "    \"login-id\": \"user-1\",\n" +
                "    \"id\": \"docid\",\n" +
                "    \"clients\": [\n" +
                "      {\n" +
                "        \"last-login\": \"2024-10-18T14:49:50.096329Z\",\n" +
                "        \"client-name\": \"web-ui\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"login-id\": \"user-2\",\n" +
                "    \"id\": \"docid-2\",\n" +
                "    \"clients\": [\n" +
                "      {\n" +
                "        \"last-login\": \"2024-10-18T14:49:50.096329Z\",\n" +
                "        \"client-name\": \"rest-api\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(gotBackPayload).isEqualTo(expectedPayload);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    }

}
