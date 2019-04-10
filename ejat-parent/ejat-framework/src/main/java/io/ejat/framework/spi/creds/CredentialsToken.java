package io.ejat.framework.spi.creds;

import io.ejat.framework.spi.creds.ICredentialsToken;

public class CredentialsToken implements ICredentialsToken {
    private String token;

    public CredentialsToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
    
}