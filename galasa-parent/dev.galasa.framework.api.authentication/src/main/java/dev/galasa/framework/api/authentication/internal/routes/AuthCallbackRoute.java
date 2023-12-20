/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class AuthCallbackRoute extends BaseRoute {

    public AuthCallbackRoute(ResponseBuilder responseBuilder, String path) {
        // Regex to match /auth/callback only
        super(responseBuilder, "\\/callback");
    }

    /**
     * GET requests to /auth/callback are sent from Dex, and only return the
     * authorization code received during the authorization code flow, which can be
     * later used later in exchange for a JWT and a refresh token.
     */
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        String authCode = queryParams.getSingleString("code", null);
        String state = queryParams.getSingleString("state", null);

        if (state != null && authCode != null) {
            // Make sure the state parameter is the same as the state that was previously stored in the session
            HttpSession session = request.getSession();
            String storedState = (String) session.getAttribute("state");
            if (storedState != null && state.equals(storedState)) {

                // Get the callback URL provided in the original /auth request, appending the
                // authorization code as a query parameter
                String clientCallbackUrl = (String) session.getAttribute("callbackUrl");
                clientCallbackUrl += "?code=" + authCode;

                // We don't need the session anymore, so invalidate it
                session.invalidate();

                // Redirect the user back to the callback URL provided in the original /auth request
                response.sendRedirect(clientCallbackUrl);
                return response;
            }
        }

        ServletError error = new ServletError(GAL5400_BAD_REQUEST, request.getServletPath());
        throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
    }
}
