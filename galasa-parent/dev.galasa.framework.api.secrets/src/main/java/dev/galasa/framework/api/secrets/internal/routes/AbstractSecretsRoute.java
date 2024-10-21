/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import java.util.Base64;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ICredentialsUsernameToken;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.GalasaSecretdata;
import dev.galasa.framework.api.beans.generated.GalasaSecretmetadata;
import dev.galasa.framework.api.beans.generated.GalasaSecretmetadatatype;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.resources.GalasaSecretType;

public abstract class AbstractSecretsRoute extends BaseRoute {

    private static final String DEFAULT_RESPONSE_ENCODING = "base64";

    public AbstractSecretsRoute(ResponseBuilder responseBuilder, String path) {
        super(responseBuilder, path);
    }

    protected GalasaSecret createGalasaSecretFromCredentials(String secretName, ICredentials credentials) {
        GalasaSecretmetadata metadata = new GalasaSecretmetadata(null);
        GalasaSecretdata data = new GalasaSecretdata();
        
        metadata.setname(secretName);
        metadata.setencoding(DEFAULT_RESPONSE_ENCODING);
        setSecretTypeValuesFromCredentials(metadata, data, credentials);

        GalasaSecret secret = new GalasaSecret();
        secret.setApiVersion(GalasaSecretType.DEFAULT_API_VERSION);
        secret.setdata(data);
        secret.setmetadata(metadata);

        return secret;
    }

    private void setSecretTypeValuesFromCredentials(GalasaSecretmetadata metadata, GalasaSecretdata data, ICredentials credentials) {
        if (credentials instanceof ICredentialsUsername) {
            ICredentialsUsername usernameCredentials = (ICredentialsUsername) credentials;
            data.setusername(encodeValue(usernameCredentials.getUsername()));

            metadata.settype(GalasaSecretmetadatatype.Username);
        } else if (credentials instanceof ICredentialsUsernamePassword) {
            ICredentialsUsernamePassword usernamePasswordCredentials = (ICredentialsUsernamePassword) credentials;
            data.setusername(encodeValue(usernamePasswordCredentials.getUsername()));
            data.setpassword(encodeValue(usernamePasswordCredentials.getPassword()));

            metadata.settype(GalasaSecretmetadatatype.USERNAME_PASSWORD);
        } else if (credentials instanceof ICredentialsUsernameToken) {
            ICredentialsUsernameToken usernameTokenCredentials = (ICredentialsUsernameToken) credentials;
            data.setusername(encodeValue(usernameTokenCredentials.getUsername()));
            data.settoken(encodeValue(new String(usernameTokenCredentials.getToken())));

            metadata.settype(GalasaSecretmetadatatype.USERNAME_TOKEN);
        } else if (credentials instanceof ICredentialsToken) {
            ICredentialsToken tokenCredentials = (ICredentialsToken) credentials;
            data.settoken(encodeValue(new String(tokenCredentials.getToken())));

            metadata.settype(GalasaSecretmetadatatype.Token);
        }
    }

    private String encodeValue(String value) {
        String encodedValue = value;
        if (DEFAULT_RESPONSE_ENCODING.equals("base64")) {
            encodedValue = Base64.getEncoder().encodeToString(value.getBytes());
        }
        return encodedValue;
    }
}
