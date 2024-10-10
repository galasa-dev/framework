/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsUsernamePassword;

public class CredentialsUsernamePassword extends Credentials implements ICredentialsUsernamePassword {
    private String username;
    private String password;

    public CredentialsUsernamePassword(SecretKeySpec key, String username, String password)
            throws CredentialsException {
        super(key);

        this.username = decryptToString(username);
        this.password = decryptToString(password);

        if (this.username == null) {
            this.username = new String(decode(username), StandardCharsets.UTF_8);
        }

        if (this.password == null) {
            this.password = new String(decode(password), StandardCharsets.UTF_8);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
