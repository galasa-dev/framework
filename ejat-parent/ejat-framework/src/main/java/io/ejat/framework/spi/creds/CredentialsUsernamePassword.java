package io.ejat.framework.spi.creds;

import io.ejat.framework.spi.creds.ICredentialsUsernamePassword;

public class CredentialsUsernamePassword implements ICredentialsUsernamePassword {
    private String username;
    private String password;

    public CredentialsUsernamePassword(String username, String password) {
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