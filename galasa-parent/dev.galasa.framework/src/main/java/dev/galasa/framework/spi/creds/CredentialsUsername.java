/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.io.UnsupportedEncodingException;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsUsername;

public class CredentialsUsername extends Credentials implements ICredentialsUsername {
    private String username;

    public CredentialsUsername(SecretKeySpec key, String username) throws CredentialsException {
        super(key);
        try {
            this.username = new String(decode(username), "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new CredentialsException("utf-8 is not available for credentials", e);
        } catch (CredentialsException e) {
            throw e;
        }
    }

    public String getUsername() {
        return username;
    }

}
