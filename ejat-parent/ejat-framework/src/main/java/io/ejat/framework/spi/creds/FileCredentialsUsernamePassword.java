package io.ejat.framework.spi;

import io.ejat.framework.spi.ICredentialsUsernamePassword;

public class FileCredentialsUsernamePassword implements ICredentialsUsernamePassword {
    private String username;
    private String password;

    public FileCredentialsUsernamePassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}