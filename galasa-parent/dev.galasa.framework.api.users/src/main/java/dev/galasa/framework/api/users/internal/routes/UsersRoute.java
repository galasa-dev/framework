/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.galasa.framework.api.beans.generated.FrontendClient;
import dev.galasa.framework.api.beans.generated.UserData;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.users.UsersServlet;
import dev.galasa.framework.api.common.JwtWrapper;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.UserDoc;

public class UsersRoute extends BaseRoute {

    // Regex to match endpoint /users and /users/
    private static final String path = "\\/?";

    public static final String QUERY_PARAMETER_LOGIN_ID_VALUE_MYSELF = "me";

    private IFramework framework;
    private Environment env;
    private IAuthStoreService authStoreService;

    public UsersRoute(ResponseBuilder responseBuilder, IFramework framework, Environment env,
            IAuthStoreService authStoreService) {
        super(responseBuilder, path);
        this.framework = framework;
        this.env = env;
        this.authStoreService = authStoreService;
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("UserRoute: handleGetRequest() entered.");

        List<UserData> usersList = new ArrayList<>();
        String payloadContent = "{}";

        String loginId = queryParams.getSingleString(UsersServlet.QUERY_PARAM_LOGIN_ID, null);

        if (loginId != null) {
            usersList = getUserByLoginIdList(request, loginId);
        }
        else{
            List<UserDoc> users = authStoreService.getAllUsers();
            usersList = convertAllUsersToUserBean(users);
        }

        if (!usersList.isEmpty()) {
            payloadContent = gson.toJson(usersList);
        }

        return getResponseBuilder().buildResponse(
                request, response, "application/json", payloadContent, HttpServletResponse.SC_OK);
    }

    private List<UserData> getUserByLoginIdList(HttpServletRequest request, String loginId)
            throws InternalServletException, AuthStoreException {

        List<UserData> usersList = new ArrayList<>();
        JwtWrapper jwtWrapper = new JwtWrapper(request, env);
        UserData userData = null;

        if (loginId.equals("me")) {
            loginId = jwtWrapper.getUsername();
        }

        UserDoc currentUser = authStoreService.getUserByLoginId(loginId);

        if (currentUser != null) {
            userData = convertUserDocToUserBean(currentUser);
            usersList.add(userData);
        }
    
        return usersList;
    }

    private UserData convertUserDocToUserBean(UserDoc user) {

        UserData userData = new UserData();

        // Map each client in user.getClients() to a new FrontendClient instance
        List<FrontendClient> clients = user.getClients().stream()
                .map(client -> {
                    FrontendClient newClient = new FrontendClient();
                    newClient.setClientName(client.getClientName());
                    newClient.setLastLogin(client.getLastLoggedIn().toString());
                    return newClient;
                })
                .collect(Collectors.toList());

        userData.setLoginId(user.getLoginId());
        userData.setid(user.getUserNumber());
        userData.setclients(clients.toArray(new FrontendClient[0])); // Convert the list to an array

        return userData;

    }

    private List<UserData> convertAllUsersToUserBean(List<UserDoc> users) {

        List<UserData> convertedUserList = new ArrayList<>();

        users.forEach(user -> {

            UserData userData = new UserData();

            // Map each client in user.getClients() to a new FrontendClient instance
            List<FrontendClient> clients = user.getClients().stream()
                    .map(client -> {
                        FrontendClient newClient = new FrontendClient();
                        newClient.setClientName(client.getClientName());
                        newClient.setLastLogin(client.getLastLoggedIn().toString());
                        return newClient;
                    })
                    .collect(Collectors.toList());

            userData.setLoginId(user.getLoginId());
            userData.setid(user.getUserNumber());
            userData.setclients(clients.toArray(new FrontendClient[0]));

            convertedUserList.add(userData);

        });

        return convertedUserList;
    }

}
