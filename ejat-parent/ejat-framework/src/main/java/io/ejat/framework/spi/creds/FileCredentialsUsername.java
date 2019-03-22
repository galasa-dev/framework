package io.ejat.framework.spi;

import io.ejat.framework.spi.ICredentialsUsername;

public class FileCredentialsUsername implements ICredentialsUsername {
    private String username;

    public FileCredentialsUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    
}