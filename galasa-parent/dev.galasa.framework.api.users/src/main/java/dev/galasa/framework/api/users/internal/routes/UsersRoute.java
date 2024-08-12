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

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.JwtWrapper;
import dev.galasa.framework.api.beans.generated.UserData;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class UsersRoute extends BaseRoute {

    // Regex to match endpoint /users and /users/
    private static final String path = "\\/users?";

    protected IFramework framework;
    protected Environment env;

    public UsersRoute(ResponseBuilder responseBuilder, IFramework framework, Environment env) {
        super(responseBuilder, path);
        this.framework = framework;
        this.env = env;
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("UserRoute: handleGetRequest() entered.");

        String nameString = queryParams.getSingleString("name", null);
        UserData userData = new UserData();

        // Make sure the required query parameters exist
        if (nameString == null) {
            ServletError error = new ServletError(GAL5400_BAD_REQUEST, request.getServletPath());
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        String extractedUsernameFromToken = returnExtractedUsername(request, env);

        userData.setLoginId(extractedUsernameFromToken);

        List<UserData> usersList = new ArrayList<>();
        usersList.add(userData);

        // converting our UserData class to JsonObject
        String payloadContent = gson.toJson(usersList);

        return getResponseBuilder().buildResponse(
            request, response, "application/json", payloadContent , HttpServletResponse.SC_OK
        );
    }

    private String returnExtractedUsername(HttpServletRequest servletRequest, Environment env)
            throws InternalServletException {

        String tokenInHeader = servletRequest.getHeader("Authorization");

        String[] splits = tokenInHeader.split(" ");

        JwtWrapper jwtWrapper = new JwtWrapper(splits[1], env);

        String username = jwtWrapper.getUsername();

        return username;

    }
}
