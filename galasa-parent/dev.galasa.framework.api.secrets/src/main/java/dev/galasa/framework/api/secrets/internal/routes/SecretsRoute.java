/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.SecretRequest;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.secrets.internal.SecretRequestValidator;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.ICredentialsService;

public class SecretsRoute extends AbstractSecretsRoute {

    // Regex to match /secrets and /secrets/ only
    private static final String PATH_PATTERN = "\\/?";

    private ICredentialsService credentialsService;
    private SecretRequestValidator createSecretValidator = new SecretRequestValidator();

    private Log logger = LogFactory.getLog(getClass());

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
        logger.info("handleGetRequest() entered. Getting secrets from the credentials store");
        List<GalasaSecret> secrets = new ArrayList<>();
        Map<String, ICredentials> retrievedCredentials = credentialsService.getAllCredentials();

        if (!retrievedCredentials.isEmpty()) {
            for (Entry<String, ICredentials> entry : retrievedCredentials.entrySet()) {
                GalasaSecret secret = createGalasaSecretFromCredentials(entry.getKey(), entry.getValue());
                secrets.add(secret);
            }
            logger.info("Secrets retrieved from credentials store OK");
        } else {
            logger.info("No secrets found in the credentials store");
        }

        logger.info("handleGetRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, "application/json",
            gson.toJson(secrets), HttpServletResponse.SC_OK);
    }

    @Override
    public HttpServletResponse handlePostRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws FrameworkException, IOException {
        logger.info("handlePostRequest() entered. Validating request payload");
        checkRequestHasContent(request);
        SecretRequest secretPayload = parseRequestBody(request, SecretRequest.class);
        createSecretValidator.validate(secretPayload);

        logger.info("Request payload validated");

        // Check if a secret with the given name already exists, throwing an error if so
        String secretName = secretPayload.getname();
        if (credentialsService.getCredentials(secretName) != null) {
            ServletError error = new ServletError(GAL5075_ERROR_SECRET_ALREADY_EXISTS);
            throw new InternalServletException(error, HttpServletResponse.SC_CONFLICT);
        }

        logger.info("Setting secret in credentials store");
        ICredentials decodedSecret = decodeCredentialsFromSecretPayload(secretPayload);
        credentialsService.setCredentials(secretName, decodedSecret);

        logger.info("Secret set in credentials store OK");
        logger.info("handlePostRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_CREATED);
    }
}
