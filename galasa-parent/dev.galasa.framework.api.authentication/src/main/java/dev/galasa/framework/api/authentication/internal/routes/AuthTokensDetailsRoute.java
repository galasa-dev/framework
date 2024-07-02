/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.authentication.JwtWrapper;
import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IInternalAuthToken;

public class AuthTokensDetailsRoute extends BaseRoute {
    private IAuthStoreService authStoreService;
    private DexGrpcClient dexGrpcClient;

    public AuthTokensDetailsRoute(
        ResponseBuilder responseBuilder,
        DexGrpcClient dexGrpcClient,
        IAuthStoreService authStoreService
    ) {
        // Regex to match /auth/tokens/{tokenid} and /auth/tokens/{tokenid}/, where {tokenid} 
        // is an ID that can contain only alphanumeric characters, underscores (_), and dashes (-)
        super(responseBuilder, "\\/tokens\\/[a-zA-Z0-9\\-\\_]+");
        this.dexGrpcClient = dexGrpcClient;
        this.authStoreService = authStoreService;
    }

    @Override
    public HttpServletResponse handleDeleteRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response)
            throws FrameworkException {
        String tokenId = getTokenIdFromUrl(pathInfo);
        String dexUserId = new JwtWrapper(request).getSubject();
        revokeToken(tokenId, dexUserId);

        String responseBody = "Successfully revoked token with ID '" + tokenId + "'";
        return getResponseBuilder().buildResponse(request, response, "text/plain", responseBody, HttpServletResponse.SC_OK);
    }

    private String getTokenIdFromUrl(String pathInfo) throws InternalServletException {
        try {
            // The URL path is '/auth/tokens/{tokenid}' so we'll grab the {tokenid} part of the path
            String[] urlParts = pathInfo.split("/");
            return urlParts[2];
        } catch (Exception ex) {
            // This should never happen since the URL's path will always contain a valid token ID
            // at this point, otherwise the route will not be matched
            ServletError error = new ServletError(GAL5065_FAILED_TO_GET_TOKEN_ID_FROM_URL);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, ex);
        }
    }

    private void revokeToken(String tokenId, String userId) throws InternalServletException {
        try {
            // Delete the Dex client associated with the token
            IInternalAuthToken tokenToRevoke = authStoreService.getToken(tokenId);
            if (tokenToRevoke == null) {
                ServletError error = new ServletError(GAL5064_FAILED_TO_REVOKE_TOKEN);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }

            String dexClientId = tokenToRevoke.getDexClientId();
            dexGrpcClient.deleteClient(dexClientId);

            // Revoke the refresh token
            dexGrpcClient.revokeRefreshToken(userId, dexClientId);

            // Delete the token's record in the auth store
            authStoreService.deleteToken(tokenId);
        } catch (AuthStoreException ex) {
            ServletError error = new ServletError(GAL5064_FAILED_TO_REVOKE_TOKEN);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
        }
    }
}
