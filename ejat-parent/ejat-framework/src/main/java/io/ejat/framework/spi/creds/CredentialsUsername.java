package io.ejat.framework.spi.creds;

import io.ejat.framework.spi.creds.ICredentialsUsername;

public class CredentialsUsername implements ICredentialsUsername {
    private String username;

    public CredentialsUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    
}