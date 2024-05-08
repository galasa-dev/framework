/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.auth.AuthToken;
import dev.galasa.framework.spi.auth.UserStoreException;

public class AuthTokensRoute extends BaseRoute {

    private IFramework framework;

    public AuthTokensRoute(ResponseBuilder responseBuilder, IFramework framework) {
        // Regex to match /auth/tokens only
        super(responseBuilder, "\\/tokens\\/?");
        this.framework = framework;
    }

    /**
     * GET requests to /auth/tokens return all the tokens stored in the tokens
     * database, sorted by creation date order by default.
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("handleGetRequest() entered");

        List<AuthToken> tokens = new ArrayList<>();
        try {
            // Retrieve all the tokens and put them into a mutable list before sorting
            // them based on their creation time
            tokens = new ArrayList<>(framework.getUserStoreService().getTokens());
            Collections.sort(tokens, Comparator.comparing(AuthToken::getCreationTime));

        } catch (UserStoreException e) {
            ServletError error = new ServletError(GAL5053_FAILED_TO_RETRIEVE_TOKENS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }

        return getResponseBuilder().buildResponse(response, "application/json", getTokensAsJsonString(tokens), HttpServletResponse.SC_OK);
    }

    /**
     * Converts a list of authentication tokens into a JSON string
     *
     * @param tokens the tokens to convert
     * @return a JSON representation of the tokens within a "tokens" JSON array
     */
    private String getTokensAsJsonString(List<AuthToken> tokens) {
        JsonArray tokensArray = new JsonArray();
        for (AuthToken token : tokens) {
            String tokenJson = gson.toJson(token);
            tokensArray.add(JsonParser.parseString(tokenJson));
        }

        JsonObject tokensObj = new JsonObject();
        tokensObj.add("tokens", tokensArray);

        return gson.toJson(tokensObj);
    }
}
