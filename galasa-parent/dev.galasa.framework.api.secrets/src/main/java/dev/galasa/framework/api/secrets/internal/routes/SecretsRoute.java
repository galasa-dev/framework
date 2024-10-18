/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ICredentialsUsernameToken;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.GalasaSecretdata;
import dev.galasa.framework.api.beans.generated.GalasaSecretmetadata;
import dev.galasa.framework.api.beans.generated.GalasaSecretmetadatatype;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

public class SecretsRoute extends BaseRoute {

    // Regex to match /secrets and /secrets/ only
    private static final String PATH_PATTERN = "\\/?";

    private static final String QUERY_PARAM_SECRET_NAME = "name";

    private ICredentialsService credentialsService;

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

        List<GalasaSecret> secrets = new ArrayList<>();
        if (queryParams.isParameterPresent(QUERY_PARAM_SECRET_NAME)) {
            String secretName = queryParams.getSingleString(QUERY_PARAM_SECRET_NAME, null);
            validateSecretName(secretName);

            GalasaSecret secret = getSecretByName(secretName);
            secrets.add(secret);
        }

        return getResponseBuilder().buildResponse(request, response, "application/json",
            getSecretsAsJsonString(secrets), HttpServletResponse.SC_OK);
    }

    private String getSecretsAsJsonString(List<GalasaSecret> secrets) {
        return gson.toJson(gson.toJsonTree(secrets));
    }

    private GalasaSecret getSecretByName(String secretName) throws InternalServletException {
        GalasaSecret secret = null;
        try {
            ICredentials credentials = credentialsService.getCredentials(secretName);

            if (credentials == null) {
                ServletError error = new ServletError(GAL5091_ERROR_SECRET_NOT_FOUND);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }

            secret = createGalasaSecretFromCredentials(secretName, credentials);
        } catch (CredentialsException e) {
            ServletError error = new ServletError(GAL5092_FAILED_TO_GET_SECRET_FROM_CREDS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return secret;
    }

    private GalasaSecret createGalasaSecretFromCredentials(String secretName, ICredentials credentials) {
        GalasaSecretmetadata metadata = new GalasaSecretmetadata(null);
        GalasaSecretdata data = new GalasaSecretdata();
        
        metadata.setname(secretName);
        setSecretTypeValuesFromCredentials(metadata, data, credentials);

        GalasaSecret secret = new GalasaSecret();
        secret.setApiVersion(GalasaSecretType.DEFAULT_API_VERSION);
        secret.setdata(data);
        secret.setmetadata(metadata);

        return secret;
    }

    private void setSecretTypeValuesFromCredentials(GalasaSecretmetadata metadata, GalasaSecretdata data, ICredentials credentials) {
        if (credentials instanceof ICredentialsUsername) {
            ICredentialsUsername usernameCredentials = (ICredentialsUsername) credentials;
            data.setusername(usernameCredentials.getUsername());

            metadata.settype(GalasaSecretmetadatatype.Username);
        } else if (credentials instanceof ICredentialsUsernamePassword) {
            ICredentialsUsernamePassword usernamePasswordCredentials = (ICredentialsUsernamePassword) credentials;
            data.setusername(usernamePasswordCredentials.getUsername());
            data.setpassword(usernamePasswordCredentials.getPassword());

            metadata.settype(GalasaSecretmetadatatype.USERNAME_PASSWORD);
        } else if (credentials instanceof ICredentialsUsernameToken) {
            ICredentialsUsernameToken usernameTokenCredentials = (ICredentialsUsernameToken) credentials;
            data.setusername(usernameTokenCredentials.getUsername());
            data.settoken(new String(usernameTokenCredentials.getToken()));

            metadata.settype(GalasaSecretmetadatatype.USERNAME_TOKEN);
        } else if (credentials instanceof ICredentialsToken) {
            ICredentialsToken tokenCredentials = (ICredentialsToken) credentials;
            data.settoken(new String(tokenCredentials.getToken()));

            metadata.settype(GalasaSecretmetadatatype.Token);
        }
    }

    private void validateSecretName(String secretName) throws InternalServletException {
        if (secretName == null || secretName.isBlank()) {
            ServletError error = new ServletError(GAL5090_INVALID_SECRET_NAME_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
