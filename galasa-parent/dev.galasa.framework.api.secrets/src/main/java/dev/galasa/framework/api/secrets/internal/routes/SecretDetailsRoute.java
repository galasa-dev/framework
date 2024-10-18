/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

public class SecretDetailsRoute extends BaseRoute {

    // Regex to match /secrets/{secret-id} and /secrets/{secret-id}/
    // where {secret-id} can consist of the following characters:
    // - Alphanumeric characters (a-zA-Z0-9)
    // - Underscores (_)
    // - Dots (.)
    // - Dashes (-)
    private static final String PATH_PATTERN = "\\/([a-zA-Z0-9_.-]+)\\/?";

    private ICredentialsService credentialsService;

    public SecretDetailsRoute(ResponseBuilder responseBuilder, ICredentialsService credentialsService) {
        super(responseBuilder, PATH_PATTERN);
        this.credentialsService = credentialsService;
    }
    
    @Override
    public HttpServletResponse handleDeleteRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws FrameworkException {
        // The provided name is implicitly validated by the route's regex pattern
        String secretName = getSecretNameFromPath(pathInfo);
        deleteSecret(secretName);

        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_NO_CONTENT);
    }

    private String getSecretNameFromPath(String pathInfo) throws InternalServletException {
        Matcher matcher = this.getPath().matcher(pathInfo);
        matcher.matches();
        return matcher.group(1);
    }

    private void deleteSecret(String secretName) throws InternalServletException {
        try {
            if (credentialsService.getCredentials(secretName) == null) {
                ServletError error = new ServletError(GAL5076_ERROR_SECRET_DOES_NOT_EXIST);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            credentialsService.deleteCredentials(secretName);

        } catch (CredentialsException e) {
            ServletError error = new ServletError(GAL5078_FAILED_TO_DELETE_SECRET);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
