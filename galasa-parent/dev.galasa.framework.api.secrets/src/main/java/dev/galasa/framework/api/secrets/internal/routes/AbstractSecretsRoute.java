/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.beans.generated.GalasaSecretType.*;

import java.util.Base64;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ICredentialsUsernameToken;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.GalasaSecretdata;
import dev.galasa.framework.api.beans.generated.GalasaSecretmetadata;
import dev.galasa.framework.api.beans.generated.SecretRequest;
import dev.galasa.framework.api.beans.generated.SecretRequestpassword;
import dev.galasa.framework.api.beans.generated.SecretRequesttoken;
import dev.galasa.framework.api.beans.generated.SecretRequestusername;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;

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

    protected ICredentials decodeCredentialsFromSecretPayload(SecretRequest secretRequest) throws InternalServletException {
        ICredentials credentials = null;
        SecretRequestusername username = secretRequest.getusername();
        SecretRequestpassword password = secretRequest.getpassword();
        SecretRequesttoken token = secretRequest.gettoken();

        if (username != null) {
            String decodedUsername = decodeSecretValue(username.getvalue(), username.getencoding());
            if (password != null) {
                // We have a username and password
                String decodedPassword = decodeSecretValue(password.getvalue(), password.getencoding());
                credentials = new CredentialsUsernamePassword(decodedUsername, decodedPassword);

            } else if (token != null) {
                // We have a username and token
                String decodedToken = decodeSecretValue(token.getvalue(), token.getencoding());
                credentials = new CredentialsUsernameToken(decodedUsername, decodedToken);
            } else {
                // We have a username
                credentials = new CredentialsUsername(decodedUsername);
            }
        } else if (token != null) {
            // We have a token
            String decodedToken = decodeSecretValue(token.getvalue(), token.getencoding());
            credentials = new CredentialsToken(decodedToken);
        }
        return credentials;
    }

    protected String decodeSecretValue(String possiblyEncodedValue, String encoding) throws InternalServletException {
        String decodedValue = possiblyEncodedValue;
        if (encoding != null && possiblyEncodedValue != null) {
            try {
                if (encoding.equalsIgnoreCase(DEFAULT_RESPONSE_ENCODING)) {
                    byte[] decodedBytes = Base64.getDecoder().decode(possiblyEncodedValue);
                    decodedValue = new String(decodedBytes);
                }
            } catch (IllegalArgumentException e) {
                ServletError error = new ServletError(GAL5097_FAILED_TO_DECODE_SECRET_VALUE, DEFAULT_RESPONSE_ENCODING);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        return decodedValue;
    }

    private void setSecretTypeValuesFromCredentials(GalasaSecretmetadata metadata, GalasaSecretdata data, ICredentials credentials) {
        if (credentials instanceof CredentialsUsername) {
            ICredentialsUsername usernameCredentials = (ICredentialsUsername) credentials;
            data.setusername(encodeValue(usernameCredentials.getUsername()));

            metadata.settype(Username);
        } else if (credentials instanceof CredentialsUsernamePassword) {
            ICredentialsUsernamePassword usernamePasswordCredentials = (ICredentialsUsernamePassword) credentials;
            data.setusername(encodeValue(usernamePasswordCredentials.getUsername()));
            data.setpassword(encodeValue(usernamePasswordCredentials.getPassword()));

            metadata.settype(USERNAME_PASSWORD);
        } else if (credentials instanceof CredentialsUsernameToken) {
            ICredentialsUsernameToken usernameTokenCredentials = (ICredentialsUsernameToken) credentials;
            data.setusername(encodeValue(usernameTokenCredentials.getUsername()));
            data.settoken(encodeValue(new String(usernameTokenCredentials.getToken())));

            metadata.settype(USERNAME_TOKEN);
        } else if (credentials instanceof CredentialsToken) {
            ICredentialsToken tokenCredentials = (ICredentialsToken) credentials;
            data.settoken(encodeValue(new String(tokenCredentials.getToken())));

            metadata.settype(Token);
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
