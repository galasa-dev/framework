/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal;

import java.util.Base64;
import java.util.Base64.Encoder;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.resources.GalasaSecretType;

public class SecretsServletTest extends BaseServletTest {

    protected JsonObject createSecretJson(String value, String encoding) {
        JsonObject secretJson = new JsonObject();
        if (value != null) {
            secretJson.addProperty("value", value);
        }

        if (encoding != null) {
            secretJson.addProperty("encoding", encoding);
        }

        return secretJson;
    }

    protected JsonObject createSecretJson(String value) {
        return createSecretJson(value, null);
    }

    protected JsonObject generateSecretJson(
        String secretName,
        String type,
        String username,
        String password,
        String token
    ) {
        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("apiVersion", GalasaSecretType.DEFAULT_API_VERSION);

        secretJson.add("metadata", generateExpectedMetadata(secretName, type));
        secretJson.add("data", generateExpectedData(username, password, token));

        secretJson.addProperty("kind", "GalasaSecret");

        return secretJson;
    }

    private JsonObject generateExpectedMetadata(String secretName, String type) {
        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", secretName);
        metadata.addProperty("encoding", "base64");
        metadata.addProperty("type", type);
        return metadata;
    }

    private JsonObject generateExpectedData(String username, String password, String token) {
        JsonObject data = new JsonObject();
        Encoder encoder = Base64.getEncoder();
        if (username != null) {
            data.addProperty("username", encoder.encodeToString(username.getBytes()));
        }

        if (password != null) {
            data.addProperty("password", encoder.encodeToString(password.getBytes()));
        }

        if (token != null) {
            data.addProperty("token", encoder.encodeToString(token.getBytes()));
        }

        return data;
    }
}
