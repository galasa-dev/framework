/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.authentication;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class Authorization {

    protected DecodedJWT decodedJwt;
    private  String jwt;

    public Authorization (HttpServletRequest req) {
        String galasaHeader = req.getHeader("Galasa-Application");
        if (galasaHeader != null){
            this.jwt = getBearerTokenFromAuthHeader(req);        
            decodeJwt();
        }
    }

    public void decodeJwt() {
        this.decodedJwt = JWT.decode(jwt);
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
        if (decodedJwt == null){
            return null;
        }
        return decodedJwt.getSubject();
    }
}

