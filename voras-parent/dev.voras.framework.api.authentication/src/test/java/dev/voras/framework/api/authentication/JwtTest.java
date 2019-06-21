package dev.voras.framework.api.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.DatatypeConverter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.junit.Test;

import dev.voras.framework.api.authentication.internal.Authenticate;


public class JwtTest {

    @Test
    public void createJwt() {
        String subject = "JimmyToken";
        String role = "admin";
        long expireDuration = 100000;

        String Jwt = Authenticate.createJWT(subject, role, expireDuration);
        System.out.println(Jwt);
    }

    @Test
    public void decodeJwt() {

        String subject = "JimmyToken";
        String role = "admin";
        long expireDuration = 100000;
        String secret = "thisIsthineKey";

        String jwt = Authenticate.createJWT(subject, role, expireDuration);

        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                        .withIssuer("voras")
                        .build();

        DecodedJWT jwtdecode = verifier.verify(jwt);
        jwtdecode = JWT.decode(jwt);

        assertEquals("admin", jwtdecode.getClaim("role").asString());
    }

    @Test
    public void decodeJwtWithBadKey() {

        String subject = "JimmyToken";
        String role = "admin";
        long expireDuration = 100000;
        String secret = "thisIsNOTthineKey";
        boolean caught = false;

        String jwt = Authenticate.createJWT(subject, role, expireDuration);

        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                        .withIssuer("voras")
                        .build();

        try {
            DecodedJWT jwtdecode = verifier.verify(jwt);
        } catch (JWTVerificationException e) {
            caught = true;
        }

        assertTrue(caught);
    }
}