/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.SecretRequest;
import dev.galasa.framework.api.beans.generated.SecretRequestpassword;
import dev.galasa.framework.api.beans.generated.SecretRequesttoken;
import dev.galasa.framework.api.beans.generated.SecretRequestusername;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.secrets.internal.SecretRequestValidator;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.creds.ICredentialsService;

public class SecretsRoute extends AbstractSecretsRoute {

    // Regex to match /secrets and /secrets/ only
    private static final String PATH_PATTERN = "\\/?";

    private static final String BASE64_ENCODING = "base64";

    private ICredentialsService credentialsService;
    private SecretRequestValidator validator = new SecretRequestValidator();

    public SecretsRoute(ResponseBuilder responseBuilder, ICredentialsService credentialsService) {
        super(responseBuilder, PATH_PATTERN);
        this.credentialsService = credentialsService;
    }

    @Override
    public HttpServletResponse handleGetRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws FrameworkException {

        // TODO get all secrets
        List<GalasaSecret> secrets = new ArrayList<>();

        return getResponseBuilder().buildResponse(request, response, "application/json",
            getSecretsAsJsonString(secrets), HttpServletResponse.SC_OK);
    }

    @Override
    public HttpServletResponse handlePostRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws FrameworkException, IOException {
        checkRequestHasContent(request);
        SecretRequest secretPayload = parseRequestBody(request, SecretRequest.class);
        validator.validate(secretPayload);

        // Check if a secret with the given name already exists, throwing an error if so
        String secretName = secretPayload.getname();
        if (credentialsService.getCredentials(secretName) != null) {
            ServletError error = new ServletError(GAL5075_ERROR_SECRET_ALREADY_EXISTS);
            throw new InternalServletException(error, HttpServletResponse.SC_CONFLICT);
        }

        ICredentials decodedSecret = getCredentialsFromSecretPayload(secretPayload);
        credentialsService.setCredentials(secretName, decodedSecret);

        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_CREATED);
    }

    private ICredentials getCredentialsFromSecretPayload(SecretRequest secretRequest) throws InternalServletException {
        ICredentials credentials = null;
        SecretRequestusername username = secretRequest.getusername();
        SecretRequestpassword password = secretRequest.getpassword();
        SecretRequesttoken token = secretRequest.gettoken();

        if (username != null) {
            String decodedUsername = decodeSecretValue(username.getvalue(), username.getencoding());
            if (password != null) {
                // We have a username and password
                String decodedPassword = decodeSecretValue(password.getvalue(), password.getencoding());
                credentials = new CredentialsUsernamePassword(decodedUsername, decodedPassword);

            } else if (token != null) {
                // We have a username and token
                String decodedToken = decodeSecretValue(token.getvalue(), token.getencoding());
                credentials = new CredentialsUsernameToken(decodedUsername, decodedToken);
            } else {
                // We have a username
                credentials = new CredentialsUsername(decodedUsername);
            }
        } else if (token != null) {
            // We have a token
            String decodedToken = decodeSecretValue(token.getvalue(), token.getencoding());
            credentials = new CredentialsToken(decodedToken);
        }
        return credentials;
    }

    private String decodeSecretValue(String possiblyEncodedValue, String encoding) throws InternalServletException {
        String decodedValue = possiblyEncodedValue;
        if (encoding != null) {
            try {
                if (encoding.equalsIgnoreCase(BASE64_ENCODING)) {
                    byte[] decodedBytes = Base64.getDecoder().decode(possiblyEncodedValue);
                    decodedValue = new String(decodedBytes);
                }
            } catch (IllegalArgumentException e) {
                ServletError error = new ServletError(GAL5095_FAILED_TO_DECODE_SECRET_VALUE, BASE64_ENCODING);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        return decodedValue;
    }

    private String getSecretsAsJsonString(List<GalasaSecret> secrets) {
        return gson.toJson(gson.toJsonTree(secrets));
    }
}
