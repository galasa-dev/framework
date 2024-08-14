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
import dev.galasa.framework.api.users.UsersServlet;
import dev.galasa.framework.api.common.JwtWrapper;
import dev.galasa.framework.api.beans.generated.UserData;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class UsersRoute extends BaseRoute {

    // Regex to match endpoint /users and /users/
    private static final String path = "\\/users?";

    public static final String QUERY_PARAMETER_LOGIN_ID_VALUE_MYSELF = "me";

    private IFramework framework;
    private Environment env;

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

        validateQueryParam(queryParams, request.getServletPath());

        List<UserData> usersList = getUsersList(request);

        String payloadContent = gson.toJson(usersList);

        return getResponseBuilder().buildResponse(
                request, response, "application/json", payloadContent, HttpServletResponse.SC_OK);
    }

    private void validateQueryParam(QueryParameters queryParams, String servletPath) throws InternalServletException {

        String loginId = queryParams.getSingleString(UsersServlet.QUERY_PARAM_LOGIN_ID, null);

        if(loginId == null){
            ServletError error = new ServletError(GAL5082_NO_LOGINID_PARAM_PROVIDED, servletPath);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        
        if (!loginId.equalsIgnoreCase(QUERY_PARAMETER_LOGIN_ID_VALUE_MYSELF)) {
            ServletError error = new ServletError(GAL5081_INVALID_QUERY_PARAM_VALUE, servletPath);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private List<UserData> getUsersList(HttpServletRequest request) throws InternalServletException {

        UserData userData = new UserData();
        JwtWrapper jwtWrapper = new JwtWrapper(request, env);

        String extractedUsernameFromToken = jwtWrapper.getUsername();

        userData.setLoginId(extractedUsernameFromToken);

        List<UserData> usersList = new ArrayList<>();
        usersList.add(userData);

        return usersList;
    }

}
