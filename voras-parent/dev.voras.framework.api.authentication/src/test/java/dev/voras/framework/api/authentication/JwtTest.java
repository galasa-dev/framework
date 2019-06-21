package dev.voras.framework.api.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Properties;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.voras.framework.api.authentication.internal.Authenticate;

@RunWith(MockitoJUnitRunner.class)
public class JwtTest {

    @Mock
    Properties configurationProperties;

    @InjectMocks
    Authenticate mockAuth = new Authenticate();

    @Test
    public void createJwt() {
        String subject = "JimmyToken";
        String role = "admin";
        long expireDuration = 100000;
        Authenticate auth = new Authenticate();
        String SECRET_KEY = "framework.jwt.secret";

        when(this.configurationProperties.get(SECRET_KEY)).thenReturn("mockedSecret");
        
        String Jwt = mockAuth.createJWT(subject, role, expireDuration);
    }

    @Test
    public void decodeJwt() {

        String subject = "JimmyToken";
        String role = "admin";
        long expireDuration = 100000;
        String secret = "mockedSecret";
        Authenticate auth = new Authenticate();
        String SECRET_KEY = "framework.jwt.secret";

        when(this.configurationProperties.get(SECRET_KEY)).thenReturn("mockedSecret");
        String jwt = mockAuth.createJWT(subject, role, expireDuration);

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
        Authenticate auth = new Authenticate();
        String SECRET_KEY = "framework.jwt.secret";

        when(this.configurationProperties.get(SECRET_KEY)).thenReturn("mockedSecret");
        String jwt = mockAuth.createJWT(subject, role, expireDuration);

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