/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.SecretRequest;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.secrets.internal.SecretRequestValidator;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

public class SecretDetailsRoute extends AbstractSecretsRoute {

    // Regex to match /secrets/{secret-id} and /secrets/{secret-id}/
    // where {secret-id} can consist of the following characters:
    // - Alphanumeric characters (a-zA-Z0-9)
    // - Underscores (_)
    // - Dots (.)
    // - Dashes (-)
    private static final String PATH_PATTERN = "\\/([a-zA-Z0-9_.-]+)\\/?";

    private ICredentialsService credentialsService;

    private SecretRequestValidator updateSecretValidator = new SecretRequestValidator(HttpMethod.PUT);

    private Log logger = LogFactory.getLog(getClass());

    public SecretDetailsRoute(ResponseBuilder responseBuilder, ICredentialsService credentialsService) {
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
        logger.info("handleGetRequest() entered. Getting secret with the given name");
        String secretName = getSecretNameFromPath(pathInfo);
        GalasaSecret secret = getSecretByName(secretName);

        logger.info("handleGetRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, "application/json",
            gson.toJson(secret), HttpServletResponse.SC_OK);
    }


    @Override
    public HttpServletResponse handlePutRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws FrameworkException, IOException {
        logger.info("handlePutRequest() entered. Validating request payload");
        checkRequestHasContent(request);
        SecretRequest secretPayload = parseRequestBody(request, SecretRequest.class);
        updateSecretValidator.validate(secretPayload);

        logger.info("Request payload validated OK");

        String secretName = getSecretNameFromPath(pathInfo);
        ICredentials decodedSecret = decodeCredentialsFromSecretPayload(secretPayload);

        int responseCode = HttpServletResponse.SC_NO_CONTENT;
        if (credentialsService.getCredentials(secretName) == null) {
            // The secret doesn't already exist, so the secret will be created
            responseCode = HttpServletResponse.SC_CREATED;
        }
        credentialsService.setCredentials(secretName, decodedSecret);

        logger.info("handlePutRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, responseCode);
    }
    
    @Override
    public HttpServletResponse handleDeleteRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws FrameworkException {
        logger.info("handleDeleteRequest() entered");
        // The provided name is implicitly validated by the route's regex pattern
        String secretName = getSecretNameFromPath(pathInfo);
        deleteSecret(secretName);

        logger.info("handleDeleteRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_NO_CONTENT);
    }

    private String getSecretNameFromPath(String pathInfo) throws InternalServletException {
        Matcher matcher = this.getPath().matcher(pathInfo);
        matcher.matches();
        return matcher.group(1);
    }

    private GalasaSecret getSecretByName(String secretName) throws InternalServletException {
        GalasaSecret secret = null;
        try {
            ICredentials credentials = credentialsService.getCredentials(secretName);

            if (credentials == null) {
                ServletError error = new ServletError(GAL5091_ERROR_SECRET_NOT_FOUND);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }

            logger.info("A secret with the given name was found OK");

            secret = createGalasaSecretFromCredentials(secretName, credentials);
        } catch (CredentialsException e) {
            ServletError error = new ServletError(GAL5092_FAILED_TO_GET_SECRET_FROM_CREDS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return secret;
    }

    private void deleteSecret(String secretName) throws InternalServletException {
        try {
            if (credentialsService.getCredentials(secretName) == null) {
                ServletError error = new ServletError(GAL5076_ERROR_SECRET_DOES_NOT_EXIST);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            logger.info("Attempting to delete the secret with the given name");

            credentialsService.deleteCredentials(secretName);

            logger.info("The secret with the given name was deleted OK");

        } catch (CredentialsException e) {
            ServletError error = new ServletError(GAL5078_FAILED_TO_DELETE_SECRET);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
