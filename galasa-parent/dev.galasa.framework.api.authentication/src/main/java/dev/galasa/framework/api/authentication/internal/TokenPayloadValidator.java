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
import dev.galasa.framework.api.common.resources.BaseResourceValidator;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletResponse;

public class TokenPayloadValidator extends BaseResourceValidator implements IBeanValidator<TokenPayload> {

    @Override
    public void validate(TokenPayload tokenPayload) throws InternalServletException {
        boolean isValid = false;
        if (tokenPayload != null) {
            String clientId = tokenPayload.getClientId();
            String refreshToken = tokenPayload.getRefreshToken();
            String authCode = tokenPayload.getCode();
    
            if (clientId != null && isAlphanumWithDashes(clientId)) {
                // The refresh token and authorization code parameters are mutually exclusive
                isValid = ((refreshToken != null && isAlphanumWithDashes(refreshToken) && authCode == null)
                    || (authCode != null && isAlphanumWithDashes(authCode) && refreshToken == null));
            }
        }

        if (!isValid) {
            ServletError error = new ServletError(GAL5062_INVALID_TOKEN_REQUEST_BODY);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
