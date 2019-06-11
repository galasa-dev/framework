package dev.voras.framework.spi.creds;

import dev.voras.framework.spi.creds.ICredentialsToken;

public class CredentialsToken implements ICredentialsToken {
    private String token;

    public CredentialsToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
    
}