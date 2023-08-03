/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.net.http.HttpClient;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.StringTokenizer;
import java.util.Base64.Decoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SystemEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

@Component(service = Filter.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.filter.pattern=/*" }, name = "Galasa JWT Auth")
public class JwtAuthFilter implements Filter {

    private final Log logger = LogFactory.getLog(getClass());

    private ResponseBuilder responseBuilder = new ResponseBuilder();

    protected Environment env = new SystemEnvironment();

    protected HttpClient httpClient = HttpClient.newHttpClient();

    protected OidcProvider oidcProvider;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        oidcProvider = new OidcProvider(env.getenv("GALASA_DEX_ISSUER"));
        logger.info("Galasa JWT Auth Filter initialised");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        // Do not apply the filter to the /auth endpoint
        if ((servletRequest.getServletPath()).equals("/auth") || "eclipse".equalsIgnoreCase(servletRequest.getHeader("Application"))) {
            chain.doFilter(request, response);
            return;
        }

        String errorString = "";
        int httpStatusCode = HttpServletResponse.SC_OK;

        try {
            String sJwt = getBearerTokenFromAuthHeader(servletRequest);
            if (sJwt != null) {

                // Only allow the request through the filter if the provided JWT is valid
                if (isJwtValid(sJwt)) {
                    chain.doFilter(request, response);
                    return;
                }
            }

            // Throw an unauthorized exception
            ServletError error = new ServletError(GAL5401_UNAUTHORIZED);
            throw new InternalServletException(error, HttpServletResponse.SC_UNAUTHORIZED);

        } catch (InternalServletException e) {
            errorString = e.getMessage();
            httpStatusCode = e.getHttpFailureCode();
        } catch (Exception e) {
            errorString = new ServletError(GAL5000_GENERIC_API_ERROR).toString();
            httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        responseBuilder.buildResponse(servletResponse, "application/json", errorString, httpStatusCode);
    }

    // Gets the JWT from a given request's Authorization header, returning null if it does not have one
    private String getBearerTokenFromAuthHeader(HttpServletRequest servletRequest) {
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

    // Checks if the provided JWT is valid or not
    private boolean isJwtValid(String jwt)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException, InternalServletException {
        try {
            String dexIssuer = oidcProvider.getIssuer();

            DecodedJWT decodedJwt = JWT.decode(jwt);
            RSAPublicKey publicKey = getRSAPublicKeyFromIssuer(decodedJwt.getKeyId(), dexIssuer);
            Algorithm algorithm = Algorithm.RSA256(publicKey);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(dexIssuer).build();

            verifier.verify(jwt);
            return (decodedJwt != null);

        } catch (JWTVerificationException e) {
            // The JWT is not valid
            logger.info("Invalid JWT '" + jwt + "'. Reason: " + e.getMessage());
            return false;
        }
    }

    // Constructs an RSA public key from a JSON Web Key (JWK) that contains the provided key ID
    // A JWK contains the following fields:
    // {
    //   "use": "sig",
    //   "kty": "RSA",
    //   "kid": "123abc",
    //   "alg": "RS256",
    //   "n": "abcdefg",
    //   "e": "xyz"
    // }
    private RSAPublicKey getRSAPublicKeyFromIssuer(String keyId, String issuer)
            throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException, InternalServletException {

        // Get the JWK with the given key ID
        JsonObject matchingJwk = oidcProvider.getJsonWebKeyFromIssuerByKeyId(keyId, issuer);
        if (matchingJwk == null) {
            logger.error("Error: No matching JSON Web Key was found with key ID '" + keyId + "'.");
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // A JWK contains an 'n' field to represent the key's modulus, and an 'e' field to represent the key's exponent, both are Base64URL-encoded
        Decoder decoder = Base64.getUrlDecoder();
        BigInteger modulus = new BigInteger(1, decoder.decode(matchingJwk.get("n").getAsString()));
        BigInteger exponent = new BigInteger(1, decoder.decode(matchingJwk.get("e").getAsString()));

        // Build a public key from the JWK that was matched
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey generatedPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        return generatedPublicKey;
    }

    @Override
    public void destroy() {
    }

}
