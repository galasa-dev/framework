/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.mocks;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public class MockDecodedJWT implements DecodedJWT {
    public MockDecodedJWT() {

    }

    public String getIssuer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getIssuer'");
    }
    @Override
    public List<String> getAudience() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAudience'");
    }
    @Override
    public Date getExpiresAt() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getExpiresAt'");
    }
    @Override
    public Date getNotBefore() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNotBefore'");
    }
    @Override
    public Date getIssuedAt() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getIssuedAt'");
    }
    @Override
    public String getId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }
    @Override
    public Claim getClaim(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getClaim'");
    }
    @Override
    public Map<String, Claim> getClaims() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getClaims'");
    }
    @Override
    public String getAlgorithm() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAlgorithm'");
    }
    @Override
    public String getType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }
    @Override
    public String getContentType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getContentType'");
    }
    @Override
    public String getKeyId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKeyId'");
    }
    @Override
    public Claim getHeaderClaim(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeaderClaim'");
    }
    @Override
    public String getToken() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getToken'");
    }
    @Override
    public String getHeader() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeader'");
    }
    @Override
    public String getPayload() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPayload'");
    }
    @Override
    public String getSignature() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public String getSubject() {
        return "validUserID";
    }
}
