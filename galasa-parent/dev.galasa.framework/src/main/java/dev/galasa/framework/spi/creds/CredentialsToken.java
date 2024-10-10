/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsToken;

public class CredentialsToken extends Credentials implements ICredentialsToken {
    private final byte[] token;

    public CredentialsToken(SecretKeySpec key, String stoken) throws CredentialsException {
        super(key);

        String decryptedToken = decryptToString(stoken);
        if (decryptedToken == null) {
            this.token = decode(stoken);
        } else {
            this.token = decryptedToken.getBytes();
        }
    }

    public byte[] getToken() {
        return token;
    }

}
