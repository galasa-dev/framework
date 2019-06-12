package dev.voras.framework.spi.creds;

import dev.voras.ICredentialsUsernamePassword;

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