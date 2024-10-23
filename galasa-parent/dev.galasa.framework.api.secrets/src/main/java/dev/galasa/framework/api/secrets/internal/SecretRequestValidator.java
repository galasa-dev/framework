/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal;

import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5073_UNSUPPORTED_GALASA_SECRET_ENCODING;
import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5090_INVALID_SECRET_NAME_PROVIDED;
import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5093_ERROR_PASSWORD_AND_TOKEN_PROVIDED;
import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5094_ERROR_MISSING_SECRET_VALUE;
import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5096_ERROR_PASSWORD_MISSING_USERNAME;
import static dev.galasa.framework.api.common.resources.GalasaSecretType.*;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.SecretRequest;
import dev.galasa.framework.api.beans.generated.SecretRequestpassword;
import dev.galasa.framework.api.beans.generated.SecretRequesttoken;
import dev.galasa.framework.api.beans.generated.SecretRequestusername;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.IBeanValidator;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;

public class SecretRequestValidator implements IBeanValidator<SecretRequest> {

    private HttpMethod requestMethod;

    public SecretRequestValidator(HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    @Override
    public void validate(SecretRequest secretRequest) throws InternalServletException {
        if (requestMethod == HttpMethod.POST) {
            validateCreateSecretRequest(secretRequest);
        } else if (requestMethod == HttpMethod.PUT) {
            validateUpdateSecretRequest(secretRequest);
        } else {
            
        }
    }

    private void validateCreateSecretRequest(SecretRequest secretRequest) throws InternalServletException {
        SecretRequestusername username = secretRequest.getusername();
        SecretRequestpassword password = secretRequest.getpassword();
        SecretRequesttoken token = secretRequest.gettoken();

        // Check that the secret has been given a name
        String secretName = secretRequest.getname();
        if (secretName == null || secretName.isBlank()) {
            ServletError error = new ServletError(GAL5090_INVALID_SECRET_NAME_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        // Password and token are mutually exclusive, so error if both are provided
        if (password != null && token != null) {
            ServletError error = new ServletError(GAL5093_ERROR_PASSWORD_AND_TOKEN_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        // Password cannot be specified on its own
        if (username == null && password != null) {
            ServletError error = new ServletError(GAL5096_ERROR_PASSWORD_MISSING_USERNAME);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        validateSecretRequestFields(username, password, token);
    }

    private void validateUpdateSecretRequest(SecretRequest secretRequest) throws InternalServletException {
        SecretRequestusername username = secretRequest.getusername();
        SecretRequestpassword password = secretRequest.getpassword();
        SecretRequesttoken token = secretRequest.gettoken();

        // Password and token are mutually exclusive, so error if both are provided
        if (password != null && token != null) {
            ServletError error = new ServletError(GAL5093_ERROR_PASSWORD_AND_TOKEN_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        // Password cannot be specified on its own
        if (username == null && password != null) {
            ServletError error = new ServletError(GAL5096_ERROR_PASSWORD_MISSING_USERNAME);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        validateSecretRequestFields(username, password, token);
    }

    private void validateSecretRequestFields(
        SecretRequestusername username,
        SecretRequestpassword password,
        SecretRequesttoken token
    ) throws InternalServletException {
        if (username != null) {
            validateField(username.getvalue(), username.getencoding());
        }

        if (password != null) {
            validateField(password.getvalue(), password.getencoding());
        }

        if (token != null) {
            validateField(token.getvalue(), token.getencoding());
        }
    }

    private void validateField(String value, String encoding) throws InternalServletException {
        if (encoding != null && !SUPPORTED_ENCODING_SCHEMES.contains(encoding)) {
            ServletError error = new ServletError(GAL5073_UNSUPPORTED_GALASA_SECRET_ENCODING, String.join(", ", SUPPORTED_ENCODING_SCHEMES));
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        if (value == null || value.isBlank()) {
            ServletError error = new ServletError(GAL5094_ERROR_MISSING_SECRET_VALUE);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
