/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.mocks.MockCredentialsService;

public class GalasaSecretProcessorTest {

	private JsonObject generateSecretJson(String secretName, String type, String encoding, String username, String password) {
        return generateSecretJson(secretName, type, encoding, username, password, null);
	}

	private JsonObject generateSecretJson(
        String secretName,
        String type,
        String encoding,
        String username,
        String password,
        String token
    ) {
        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("apiVersion", "galasa-dev/v1alpha1");
        secretJson.addProperty("kind", "GalasaSecret");

        JsonObject secretMetadata = new JsonObject();
        secretMetadata.addProperty("name", secretName);
        secretMetadata.addProperty("type", type);

        if (encoding != null) {
            secretMetadata.addProperty("encoding", encoding);
        }

        JsonObject secretData = new JsonObject();
        if (username != null) {
            secretData.addProperty("username", username);
        }

        if (password != null) {
            secretData.addProperty("password", password);
        }

        if (token != null) {
            secretData.addProperty("token", token);
        }

        secretJson.add("metadata", secretMetadata);
        secretJson.add("data", secretData);

        // Expecting a JSON structure in the form:
        // {
        //     "apiVersion": "galasa-dev/v1alpha1",
        //     "kind": "GalasaSecret",
        //     "metadata": {
        //         "name": "SECRET1",
        //         "type": "Username",
        //         "encoding": "base64"
        //     },
        //     "data": {
        //         "username": "a-username"
        //     }
        // }
		return secretJson;
	}

    @Test
    public void testApplyGalasaSecretWithMissingDataThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // Remove the data from the JSON payload to force an error
        secretJson.remove("data");

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, "apply");
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains(
            "GAL5069E",
            "Invalid request body provided. One or more of the following mandatory fields are missing from the request body",
            "[apiVersion, metadata, data]");
    }

    @Test
    public void testApplyGalasaSecretWithMissingMetadataThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // Remove the metadata from the JSON payload to force an error
        secretJson.remove("metadata");

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, "apply");
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains(
            "GAL5069E",
            "Invalid request body provided. One or more of the following mandatory fields are missing from the request body",
            "[apiVersion, metadata, data]");
    }

    @Test
    public void testApplyGalasaSecretWithMissingApiVersionThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // Remove the apiVersion from the JSON payload to force an error
        secretJson.remove("apiVersion");

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, "apply");
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains(
            "GAL5069E",
            "Invalid request body provided. One or more of the following mandatory fields are missing from the request body",
            "[apiVersion, metadata, data]");
    }

    @Test
    public void testApplyGalasaSecretWithMissingUsernamePasswordFieldsThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = null;
        String password = null;
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, "apply");

        // Then...
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(
            "GAL5070E: Invalid GalasaSecret provided.",
            "One or more of the following mandatory fields are missing from the 'data' field",
            "[username, password]");
    }

    @Test
    public void testApplyGalasaSecretWithUnsupportedEncodingThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = "UNKNOWN!!!";
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, "apply");

        // Then...
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("GAL5072E: Unsupported data encoding scheme provided");
    }

    @Test
    public void testApplyGalasaSecretWithUnknownSecretTypeThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UNKNOWN TYPE!";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, "apply");

        // Then...
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("GAL5073E: Unknown GalasaSecret type provided");
    }

    @Test
    public void testCreateGalasaSecretSetsCredentialsOk() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "my-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, "apply");

        // Then...
        assertThat(errors).isEmpty();
    }
}
