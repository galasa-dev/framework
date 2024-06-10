/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import dev.galasa.framework.api.beans.TokenPayload;
import dev.galasa.framework.api.common.IBeanValidator;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletResponse;

public class TokenPayloadValidator implements IBeanValidator<TokenPayload> {

    // Regex to match a non-empty string that can contain only:
    // - Alphanumeric characters
    // - '-' (dashes)
    // - '_' (underscores)
    private static final String ALPHANUM_DASHES_PATTERN = "^[a-zA-Z0-9\\-_]+$";

    @Override
    public void validate(TokenPayload tokenPayload) throws InternalServletException {
        boolean isValid = false;
        if (tokenPayload != null) {
            String clientId = tokenPayload.getClientId();
            String refreshToken = tokenPayload.getRefreshToken();
            String authCode = tokenPayload.getCode();
    
            if (clientId != null && clientId.matches(ALPHANUM_DASHES_PATTERN)) {
                // The refresh token and authorization code parameters are mutually exclusive
                isValid = ((refreshToken != null && refreshToken.matches(ALPHANUM_DASHES_PATTERN) && authCode == null)
                    || (authCode != null && authCode.matches(ALPHANUM_DASHES_PATTERN) && refreshToken == null));
            }
        }

        if (!isValid) {
            ServletError error = new ServletError(GAL5062_INVALID_TOKEN_REQUEST_BODY);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
