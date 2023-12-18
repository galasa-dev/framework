/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

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
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        String authCode = queryParams.getSingleString("code", null);
        String state = queryParams.getSingleString("state", null);
        
        if (state != null && authCode != null) {
            // Make sure the state parameter is the same as the state that was previously stored in the browser
            Cookie stateCookie = getCookieByName(request.getCookies(), "state");
            if (stateCookie != null && state.equals(stateCookie.getValue())) {

                // The state parameter is valid and is no longer needed, so we'll delete it as part of the response
                deleteCookie(stateCookie, response);

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("code", authCode);
                return getResponseBuilder().buildResponse(response, "application/json", gson.toJson(responseJson), HttpServletResponse.SC_OK);
            }
        }

        ServletError error = new ServletError(GAL5400_BAD_REQUEST, request.getServletPath());
        throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
    }

    /**
     * Returns a cookie that matches a given name, or null if no such cookie exists.
     * @param cookies the collection of cookies to search through
     * @param name the name to match
     */
    private Cookie getCookieByName(Cookie[] cookies, String name) {
        Cookie cookieToReturn = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                cookieToReturn = cookie;
                break;
            }
        }
        return cookieToReturn;
    }

    private void deleteCookie(Cookie cookie, HttpServletResponse response) {
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
