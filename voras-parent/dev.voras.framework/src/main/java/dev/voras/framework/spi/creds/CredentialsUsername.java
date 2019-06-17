package dev.voras.framework.spi.creds;

import dev.voras.ICredentialsUsername;

public class CredentialsUsername implements ICredentialsUsername {
    private String username;

    public CredentialsUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    
}