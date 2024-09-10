/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.authentication.AuthenticationServlet;
import dev.galasa.framework.api.authentication.IOidcProvider;
import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.beans.AuthToken;
import dev.galasa.framework.api.beans.User;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.AuthStoreException;

import dev.galasa.framework.spi.auth.IAuthStoreService;


public class AuthTokensByLoginIdRoute extends BaseRoute{

    private IAuthStoreService authStoreService;
    private IOidcProvider oidcProvider;
    private DexGrpcClient dexGrpcClient;
    private Environment env;

    // Regex to match /auth/tokens and /auth/tokens/ only
    private static final String PATH_PATTERN = "\\/getTokensByLoginId\\/?";

    public AuthTokensByLoginIdRoute(
        ResponseBuilder responseBuilder,
        IOidcProvider oidcProvider,
        DexGrpcClient dexGrpcClient,
        IAuthStoreService authStoreService,
        Environment env
    ) {
        super(responseBuilder, PATH_PATTERN);
        this.oidcProvider = oidcProvider;
        this.dexGrpcClient = dexGrpcClient;
        this.authStoreService = authStoreService;
        this.env = env;
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

        List<AuthToken> tokensToReturn = new ArrayList<>();
        try {
            // Retrieve all the tokens and put them into a mutable list before sorting them based on their creation time

            validateQueryParam(queryParams, pathInfo);

            List<IInternalAuthToken> tokens = new ArrayList<>(authStoreService.getTokensByLoginId(queryParams.getSingleString(AuthenticationServlet.QUERY_PARAM_LOGIN_ID, null)));

            // Convert the token received from the auth store into the token bean that will be returned as JSON
            for (IInternalAuthToken token : tokens) {
                User user = new User(token.getOwner().getLoginId());
                tokensToReturn.add(new AuthToken(
                    token.getTokenId(),
                    token.getDescription(),
                    token.getCreationTime(),
                    user)
                );
            }
        } catch (AuthStoreException e) {
            ServletError error = new ServletError(GAL5053_FAILED_TO_RETRIEVE_TOKENS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }

        return getResponseBuilder().buildResponse(request, response, "application/json", getTokensAsJsonString(tokensToReturn), HttpServletResponse.SC_OK);
    }

    private void validateQueryParam(QueryParameters queryParams, String servletPath) throws InternalServletException {

        String loginId = queryParams.getSingleString(AuthenticationServlet.QUERY_PARAM_LOGIN_ID, null);

        if(loginId == null){
            ServletError error = new ServletError(GAL5400_BAD_REQUEST, servletPath);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        
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
