/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal;

import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Map;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;

public class SecretsServletTest extends BaseServletTest {

    protected static final Map<String, String> REQUEST_HEADERS = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

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
        return generateSecretJson(secretName, type, username, password, token, null, null, null);
    }

    protected JsonObject generateSecretJson(
        String secretName,
        String type,
        String username,
        String password,
        String token,
        String description,
        String lastUpdatedUser,
        Instant lastUpdatedTime
    ) {
        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("apiVersion", GalasaResourceValidator.DEFAULT_API_VERSION);

        secretJson.add("metadata", generateExpectedMetadata(secretName, type, description, lastUpdatedUser, lastUpdatedTime));
        secretJson.add("data", generateExpectedData(username, password, token));

        secretJson.addProperty("kind", "GalasaSecret");

        return secretJson;
    }

    private JsonObject generateExpectedMetadata(
        String secretName,
        String type,
        String description,
        String lastUpdatedUser,
        Instant lastUpdatedTime
    ) {
        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", secretName);
        if (lastUpdatedTime != null) {
            metadata.addProperty("lastUpdatedTime", lastUpdatedTime.toString());
        }

        if (lastUpdatedUser != null) {
            metadata.addProperty("lastUpdatedBy", lastUpdatedUser);
        }

        metadata.addProperty("encoding", "base64");

        if (description != null) {
            metadata.addProperty("description", description);
        }

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
