/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.io.UnsupportedEncodingException;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsUsernameToken;

public class CredentialsUsernameToken extends Credentials implements ICredentialsUsernameToken {
    private String username;
    private byte[] token;

    public CredentialsUsernameToken(SecretKeySpec key, String username, String token) throws CredentialsException {
        super(key);
        try {
            this.username = new String(decode(username), "utf-8");
            this.token = decode(token);
        } catch (UnsupportedEncodingException e) {
            throw new CredentialsException("utf-8 is not available for credentials", e);
        } catch (CredentialsException e) {
            throw e;
        }

    }

    public String getUsername() {
        return username;
    }

    public byte[] getToken() {
        return token;
    }
}
