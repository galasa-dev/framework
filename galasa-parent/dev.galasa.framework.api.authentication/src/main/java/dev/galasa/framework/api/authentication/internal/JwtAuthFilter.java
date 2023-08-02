/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

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
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

@Component(service = Filter.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.filter.pattern=/*" }, name = "Galasa JWT Auth")
public class JwtAuthFilter implements Filter {

    private final Log logger = LogFactory.getLog(getClass());

    private static final Gson gson = GalasaGsonBuilder.build();

    private ResponseBuilder responseBuilder = new ResponseBuilder();

    protected Environment env = new SystemEnvironment();

    protected HttpClient httpClient = HttpClient.newHttpClient();

    // private static String SECRET_KEY = "framework.jwt.secret";

    // private Properties configurationProperties = new Properties();

    // @Activate
    // void activate(Map<String, Object> properties) {
    // synchronized (configurationProperties) {
    // String secret = (String) properties.get(SECRET_KEY);
    // if (secret != null) {
    // this.configurationProperties.put(SECRET_KEY, secret);
    // } else {
    // this.configurationProperties.remove(SECRET_KEY);
    // }
    // }
    // }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Galasa JWT Auth Filter initialised.");
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

        // Principal principal = servletRequest.getUserPrincipal();
        // if (principal != null) { // already authenticated
        //     chain.doFilter(request, response);
        //     return;
        // }

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

        // String sJwt = st.nextToken();
        // Algorithm algorithm = Algorithm.HMAC256(this.configurationProperties.getProperty(SECRET_KEY));

        // JWTVerifier verifier = JWT.require(algorithm).withIssuer("galasa").build();
        // try {
        //     DecodedJWT jwt = verifier.verify(sJwt);

        //     String subject = jwt.getSubject();
        //     String role = jwt.getClaim("role").asString();

        //     JwtRequestWrapper wrapper = new JwtRequestWrapper(subject, role, servletRequest);

        //     chain.doFilter(wrapper, servletResponse);
        //     return;
        // } catch (AlgorithmMismatchException e) {
        //     chain.doFilter(request, response);
        //     invalidAuth(servletRequest, servletResponse, "Incorrect Algorithim " + e);
        //     return;
        // } catch (SignatureVerificationException e) {
        //     chain.doFilter(request, response);
        //     invalidAuth(servletRequest, servletResponse, "Non valid signature " + e);
        // } catch (TokenExpiredException e) {
        //     chain.doFilter(request, response);
        //     invalidAuth(servletRequest, servletResponse, "Jwt has expired " + e);
        // } catch (InvalidClaimException e) {
        //     chain.doFilter(request, response);
        //     invalidAuth(servletRequest, servletResponse, "Invalid Claims " + e);
        // }
    }

    // Gets the JWT from a given request's Authorization header, returning null if
    // it does not have one
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
            String dexIssuer = env.getenv("GALASA_DEX_ISSUER");

            DecodedJWT decodedJwt = JWT.decode(jwt);
            RSAPublicKey publicKey = getRSAPublicKeyFromIssuer(decodedJwt.getKeyId(), dexIssuer);
            Algorithm algorithm = Algorithm.RSA256(publicKey);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(dexIssuer).build();

            verifier.verify(jwt);

            return (decodedJwt != null);

        } catch (JWTVerificationException e) {
            logger.debug("-----ERROR IS");
            logger.debug(e.getMessage());
        }
        return false;
    }

    private RSAPublicKey getRSAPublicKeyFromIssuer(String keyId, String issuer)
            throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException, InternalServletException {

        // Send GET request to the issuer's /keys endpoint
        String dexIssuer = env.getenv("GALASA_DEX_ISSUER");
        HttpRequest getRequest = HttpRequest.newBuilder().GET().header("Accept", "application/json")
                .uri(URI.create(dexIssuer + "/keys")).build();

        HttpResponse<String> response = httpClient.send(getRequest, BodyHandlers.ofString());
        JsonObject responseBodyJson = gson.fromJson(response.body(), JsonObject.class);

        if (!responseBodyJson.has("keys")) {
            logger.error("Error: No JSON Web Keys were found at the '" + dexIssuer + "/keys' endpoint.");
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // Iterate over returned JSON response for a JWK that matches the JWT's key ID
        // ('kid') field
        JsonArray keys = responseBodyJson.get("keys").getAsJsonArray();

        JsonObject matchingKey = null;
        for (JsonElement key : keys) {
            JsonObject keyAsJsonObject = key.getAsJsonObject();
            if (keyAsJsonObject.get("kid").getAsString().equals(keyId)) {
                matchingKey = keyAsJsonObject;
                break;
            }
        }

        if (matchingKey == null) {
            logger.error("Error: No matching JSON Web Key was found with key ID '" + keyId + "'.");
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // The JWK contains an 'n' field to represent the key's modulus, and an 'e'
        // field to represent the key's exponent
        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(matchingKey.get("n").getAsString()));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(matchingKey.get("e").getAsString()));

        // Build a public key from the JWK that matches
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey generatedPublicKey = (RSAPublicKey) kf.generatePublic(keySpec);
        return generatedPublicKey;
    }

    // private void invalidAuth(HttpServletRequest servletRequest,
    // HttpServletResponse servletResponse, String jwtResponse)
    // throws IOException {
    // servletResponse.setContentType("text/plain");
    // servletResponse.addHeader("WWW-Authenticate", "Bearer realm=\"Galasa\""); //
    // *** Ability to set the realm
    // servletResponse.getWriter().write(jwtResponse);
    // return;
    // }

    @Override
    public void destroy() {
    }

}
