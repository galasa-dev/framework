/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.SystemEnvironment;

public class JwtWrapper {

    protected DecodedJWT decodedJwt;
    private Log logger = LogFactory.getLog(this.getClass());

    private static final List<String> DEFAULT_USERNAME_CLAIMS = List.of(
        "preferred_username",
        "name",
        "sub"
    );

    private final String usernameClaimOverrides;

    public JwtWrapper(HttpServletRequest req) {
        this(req, new SystemEnvironment());
    }

    public JwtWrapper(HttpServletRequest req, Environment env) {
        this.decodedJwt = decodeJwt(getBearerTokenFromAuthHeader(req));
        usernameClaimOverrides = env.getenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS);
    }

    public DecodedJWT decodeJwt(@NotNull String jwt) {
        return JWT.decode(jwt);
    }

    /**
     * Gets a JSON Web Token (JWT) from a given request's Authorization header,
     * returning null if it does not have one.
     *
     * @param servletRequest the request to retrieve a JWT from
     * @return the JWT stored in the request's "Authorization" header, or null
     */
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

    /**
     * Gets the username from a request's JWT, or null if no matching claim exists
     * within the JWT.
     *
     * @return the username retrieved from a claim in the JWT, or null if no
     *         matching claim in the JWT can be used as a username.
     */
    public String getUsername() {
        if (decodedJwt == null){
            return null;
        }

        // Use an environment variable to override the default list of username claims
        List<String> usernameClaims = DEFAULT_USERNAME_CLAIMS;
        if (usernameClaimOverrides != null && !usernameClaimOverrides.isEmpty()) {
            logger.info("Environment variable '" + EnvironmentVariables.GALASA_USERNAME_CLAIMS
                    + "' used to override the JWT claims that map to a username");
            usernameClaims = getUsernameClaimOverridesAsList();
        }

        // Go through the preference list of JWT claims to get a username from the decoded JWT
        String userName = getUsernameFromClaims(usernameClaims);
        if (userName == null && usernameClaims != DEFAULT_USERNAME_CLAIMS) {

            // Try to get a username from the default list of claims to match against
            logger.info("Could not get username from overridden claims, attempting to use default claims instead");
            userName = getUsernameFromClaims(DEFAULT_USERNAME_CLAIMS);
        }
        return userName;
    }

    private String getUsernameFromClaims(List<String> usernameClaims) {
        String username = null;
        for (String claim : usernameClaims) {
            username = decodedJwt.getClaim(claim).asString();
            if (username != null) {
                break;
            }
        }
        return username;
    }

    private List<String> getUsernameClaimOverridesAsList() {
        String[] overrideClaims = usernameClaimOverrides.split(",");
        List<String> usernameClaims = new ArrayList<>();

        for (String claim : overrideClaims) {
            usernameClaims.add(claim.trim());
        }
        return usernameClaims;
    }
}

