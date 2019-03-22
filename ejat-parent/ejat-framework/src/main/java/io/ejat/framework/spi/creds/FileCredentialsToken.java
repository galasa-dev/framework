package io.ejat.framework.spi;

import io.ejat.framework.spi.ICredentialsToken;

public class FileCredentialsToken implements ICredentialsToken {
    private String token;

    public FileCredentialsToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
    
}