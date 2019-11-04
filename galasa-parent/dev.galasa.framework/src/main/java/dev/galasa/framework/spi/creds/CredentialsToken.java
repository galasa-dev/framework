/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi.creds;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsToken;

public class CredentialsToken extends Credentials implements ICredentialsToken {
    private final byte[] token;

    public CredentialsToken(SecretKeySpec key, String stoken) throws CredentialsException {
        super(key);

        this.token = decode(stoken);
    }

    public byte[] getToken() {
        return token;
    }

}
