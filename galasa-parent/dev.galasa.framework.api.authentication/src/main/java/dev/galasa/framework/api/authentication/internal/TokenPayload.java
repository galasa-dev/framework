/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import com.google.gson.annotations.SerializedName;

/**
 * A class that represents the payload of a POST request to an OpenID
 * Connect provider's token endpoint. It can be used when deserialising a
 * request's body.
 */
public class TokenPayload {

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("secret")
    private String secret;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("code")
    private String code;

    public String getClientId() {
        return clientId;
    }

    public String getSecret() {
        return secret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getCode() {
        return code;
    }

    public boolean isValid() {
        return (clientId != null) && (secret != null) && (refreshToken != null || code != null);
    }
}
