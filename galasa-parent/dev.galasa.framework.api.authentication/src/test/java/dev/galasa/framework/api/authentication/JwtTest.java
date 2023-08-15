/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
// package dev.galasa.framework.api.authentication;

// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertNotNull;
// import static org.junit.Assert.assertTrue;
// import static org.mockito.Mockito.when;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.Properties;

// import com.auth0.jwt.JWT;
// import com.auth0.jwt.JWTVerifier;
// import com.auth0.jwt.algorithms.Algorithm;
// import com.auth0.jwt.exceptions.JWTVerificationException;
// import com.auth0.jwt.interfaces.DecodedJWT;

// import dev.galasa.framework.api.authentication.internal.Authenticate;

// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.MockitoJUnitRunner;

// @RunWith(MockitoJUnitRunner.class)
// public class JwtTest {
//     private String jwt;
//     private String SECRET = "ATestSecretBananas";

//     @Before
//     public void before() {
//         Authenticate auth = new Authenticate();
//         String subject = "JimmyToken";
//         String role = "admin";
//         long expireDuration = 100000;

//         Map<String, Object> props = new HashMap<>();
//         props.put("framework.jwt.secret", SECRET);

//         auth.activate(props);
//         jwt = auth.createJWT(subject, role, expireDuration);
//     }

//     @Test
//     public void createJwt() {
//         assertNotNull(jwt);
//     }

//     @Test
//     public void decodeJwt() {

//         Algorithm algorithm = Algorithm.HMAC256(SECRET);
//         JWTVerifier verifier = JWT.require(algorithm).withIssuer("galasa").build();

//         DecodedJWT jwtdecode = verifier.verify(jwt);
//         jwtdecode = JWT.decode(jwt);

//         assertEquals("admin", jwtdecode.getClaim("role").asString());
//     }

//     @Test
//     public void decodeJwtWithBadKey() {
//         String wrongSecret = "NotBananas";
//         Boolean caught = false;

//         Algorithm algorithm = Algorithm.HMAC256(wrongSecret);
//         JWTVerifier verifier = JWT.require(algorithm).withIssuer("galasa").build();

//         try {
//             DecodedJWT jwtdecode = verifier.verify(jwt);
//         } catch (JWTVerificationException e) {
//             caught = true;
//         }

//         assertTrue(caught);
//     }
// }
