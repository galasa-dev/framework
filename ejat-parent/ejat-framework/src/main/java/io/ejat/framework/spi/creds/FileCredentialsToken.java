package io.ejat.framework.spi.creds;

import io.ejat.framework.spi.creds.ICredentialsToken;

public class FileCredentialsToken implements ICredentialsToken {
    private String token;

    public FileCredentialsToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
    
}