/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.ServletOutputStream;

import java.time.Instant;
import org.junit.Test;

import dev.galasa.framework.api.beans.generated.FrontEndClient;
import dev.galasa.framework.api.beans.generated.UserData;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.mocks.MockAuthStoreService;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockFrontEndClient;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockTimeService;
import dev.galasa.framework.api.common.mocks.MockUser;
import dev.galasa.framework.api.users.mocks.MockUsersServlet;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.utils.GalasaGson;


public class UsersRouteTest extends BaseServletTest {

    Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

    GalasaGson gson = new GalasaGson();
    
    @Test
    public void testUsersGetRequestReturnsUserByLoginIdReturnsOK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework framework = new MockFramework(authStoreService);

        String baseUrl = "http://my.server/api";


        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { "user-1" });

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(framework, env);


        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams,null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        {
            MockFrontEndClient newClient = new MockFrontEndClient("web-ui");
            newClient.name = "web-ui";
            newClient.lastLoginTime = Instant.parse("2024-10-18T14:49:50.096329Z");
            

            MockUser user1Data = new MockUser();
            user1Data.userNumber = "docid";
            user1Data.loginId = "user-1";
            user1Data.addClient(newClient);

            authStoreService.addUser(user1Data);
        }

        {
            MockFrontEndClient newClient = new MockFrontEndClient("rest-api");
            newClient.name = "rest-api";
            newClient.lastLoginTime = Instant.parse("2024-10-18T14:49:50.096329Z");
            

            MockUser user1Data = new MockUser();
            user1Data.userNumber = "docid-2";
            user1Data.loginId = "user-2";
            user1Data.addClient(newClient);

            authStoreService.addUser(user1Data);
        }

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);


        FrontEndClient[] user1clients = new FrontEndClient[1];
        FrontEndClient newClient = new FrontEndClient();
        newClient.setClientName("web-ui");
        newClient.setLastLogin("2024-10-18T14:49:50.096329Z");
        user1clients[0] = newClient;

        UserData user1Data = new UserData();
        user1Data.setid("docid");
        user1Data.setLoginId("user-1");
        user1Data.setclients(user1clients);
        user1Data.seturl(baseUrl + "/users/" + user1Data.getid());

        List<UserData> users = List.of(user1Data);

        String gotBackPayload = outStream.toString();
        String expectedPayload = gson.toJson(users);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(gotBackPayload).isEqualTo(expectedPayload);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    }

    @Test
    public void testUsersGetRequestReturnsAllUsersReturnsOK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework framework = new MockFramework(authStoreService);

        String baseUrl = "http://my.server/api";

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(framework, env);


        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        {
            MockFrontEndClient newClient = new MockFrontEndClient("web-ui");
            newClient.name = "web-ui";
            newClient.lastLoginTime = Instant.parse("2024-10-18T14:49:50.096329Z");
            

            MockUser user1Data = new MockUser();
            user1Data.userNumber = "docid";
            user1Data.loginId = "user-1";
            user1Data.addClient(newClient);

            authStoreService.addUser(user1Data);
        }

        {
            MockFrontEndClient newClient = new MockFrontEndClient("rest-api");
            newClient.name = "rest-api";
            newClient.lastLoginTime = Instant.parse("2024-10-18T14:49:50.096329Z");
            

            MockUser user1Data = new MockUser();
            user1Data.userNumber = "docid-2";
            user1Data.loginId = "user-2";
            user1Data.addClient(newClient);

            authStoreService.addUser(user1Data);
        }

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);


        FrontEndClient[] user1clients = new FrontEndClient[1];
        FrontEndClient newClient = new FrontEndClient();
        newClient.setClientName("web-ui");
        newClient.setLastLogin("2024-10-18T14:49:50.096329Z");
        user1clients[0] = newClient;

        UserData user1Data = new UserData();
        user1Data.setid("docid");
        user1Data.setLoginId("user-1");
        user1Data.setclients(user1clients);
        user1Data.seturl(baseUrl + "/users/" + user1Data.getid());

        FrontEndClient[] user2clients = new FrontEndClient[1];
        FrontEndClient newClient2 = new FrontEndClient();
        newClient2.setClientName("rest-api");
        newClient2.setLastLogin("2024-10-18T14:49:50.096329Z");
        user2clients[0] = newClient2;

        UserData user2Data = new UserData();
        user2Data.setid("docid-2");
        user2Data.setLoginId("user-2");
        user2Data.setclients(user2clients);
        user2Data.seturl(baseUrl + "/users/" + user2Data.getid());

        List<UserData> users = List.of(user1Data, user2Data);

        String gotBackPayload = outStream.toString();
        String expectedPayload = gson.toJson(users);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(gotBackPayload).isEqualTo(expectedPayload);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    }

}
