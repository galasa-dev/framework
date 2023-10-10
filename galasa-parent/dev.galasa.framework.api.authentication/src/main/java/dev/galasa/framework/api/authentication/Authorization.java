/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.authentication;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.authentication.internal.OidcProvider;

public class Authorization {

    private Environment env = new SystemEnvironment();
    protected DecodedJWT decodedJwt;
    protected OidcProvider oidcProvider = new OidcProvider(env.getenv("GALASA_DEX_ISSUER"));
    private  String jwt;

    public Authorization (HttpServletRequest req) {
        this.jwt = getBearerTokenFromAuthHeader(req);}

    public void decodeJwt() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException{
        this.decodedJwt = oidcProvider.decodeJwt(jwt);
    }

    // Gets the JWT from a given request's Authorization header, returning null if it does not have one
    public static String getBearerTokenFromAuthHeader(HttpServletRequest servletRequest) {
        String sJwt = null;
        String authorization = servletRequest.getHeader("Authorization");
        if (authorization != null) {
            StringTokenizer st = new StringTokenizer(authorization);
            if (st.hasMoreTokens()) {
                String bearer = st.nextToken();
                if (bearer.equalsIgnoreCase("bearer") && st.hasMoreTokens()) {
                    sJwt = st.nextToken();
                }
            }
        }
        return sJwt;
    }

    public String getUser() {
        return decodedJwt.getSubject();
    }
}

