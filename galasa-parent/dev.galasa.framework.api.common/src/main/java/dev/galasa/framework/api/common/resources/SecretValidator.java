/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;

public abstract class SecretValidator<T> extends GalasaResourceValidator<T> {

    public static final List<String> SUPPORTED_ENCODING_SCHEMES = List.of("base64");

    public SecretValidator() {}

    public SecretValidator(ResourceAction action) {
        super(action);
    }

    protected void validateSecretName(String secretName) throws InternalServletException {
        if (secretName == null || secretName.isBlank() || secretName.contains(".") || !isLatin1(secretName)) {
            ServletError error = new ServletError(GAL5092_INVALID_SECRET_NAME_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    protected void validateDescription(String description) throws InternalServletException {
        if (description != null && (description.isBlank() || !isLatin1(description))) {
            ServletError error = new ServletError(GAL5102_INVALID_SECRET_DESCRIPTION_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
