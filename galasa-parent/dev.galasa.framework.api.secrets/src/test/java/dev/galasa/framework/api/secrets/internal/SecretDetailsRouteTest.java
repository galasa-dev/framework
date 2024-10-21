/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal;

import static org.assertj.core.api.Assertions.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64.Encoder;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.mocks.MockCredentialsService;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.api.secrets.internal.routes.SecretDetailsRoute;
import dev.galasa.framework.api.secrets.mocks.MockSecretsServlet;
import dev.galasa.framework.spi.creds.CredentialsUsername;

public class SecretDetailsRouteTest extends BaseServletTest {

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

        if (password != null) {
            data.addProperty("token", encoder.encodeToString(token.getBytes()));
        }

        return data;
    }

    private String generateExpectedJson(
        String secretName,
        String type,
        String username,
        String password,
        String token
    ) {
        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("apiVersion", GalasaSecretType.DEFAULT_API_VERSION);
        // secretJson.addProperty("kind", "GalasaSecret");

        secretJson.add("metadata", generateExpectedMetadata(secretName, type));
        secretJson.add("data", generateExpectedData(username, password, token));

        return gson.toJson(secretJson);
    }

    @Test
    public void testSecretDetailsRouteRegexMatchesExpectedPaths() throws Exception {
        // Given...
        Pattern routePattern = new SecretDetailsRoute(null, null).getPath();

        // Then...
        // The servlet's whiteboard pattern will match /secrets, so this route should
        // allow the name of a secret to be provided
        assertThat(routePattern.matcher("/MYSECRET").matches()).isTrue();
        assertThat(routePattern.matcher("/MYSECRET/").matches()).isTrue();
        assertThat(routePattern.matcher("/mysecret").matches()).isTrue();
        assertThat(routePattern.matcher("/myS3cret123").matches()).isTrue();
        assertThat(routePattern.matcher("/123My.Secret.456").matches()).isTrue();
        assertThat(routePattern.matcher("/My-Secret.456").matches()).isTrue();
        assertThat(routePattern.matcher("/My-Secret_456").matches()).isTrue();

        // The route should not accept the following
        assertThat(routePattern.matcher("////").matches()).isFalse();
        assertThat(routePattern.matcher("").matches()).isFalse();
        assertThat(routePattern.matcher("/my secret").matches()).isFalse();
        assertThat(routePattern.matcher("/<html>thisisbad</html>").matches()).isFalse();
        assertThat(routePattern.matcher("/javascript:thisisbad;").matches()).isFalse();
    }

    @Test
    public void testGetSecretByNameReturnsSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String username = "my-user";
        creds.put(secretName, new CredentialsUsername(username));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        String expectedJson = generateExpectedJson(secretName, "Username", username, null, null);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testGetNonExistantSecretByNameReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "NON_EXISTANT_SECRET";
        creds.put("BOB", new CredentialsUsername("my-user"));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(output, 5091, "Unable to retrieve a secret with the given name");
    }

    @Test
    public void testGetSecretByNameWithFailingCredsStoreReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        MockCredentialsService credsService = new MockCredentialsService(creds);

        // Force a server error from the creds service
        credsService.setThrowError(true);

        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName);
        mockRequest.setQueryParameter("name", secretName);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(output, 5092,
            "Failed to retrieve a secret with the given name from the credentials store");
    }

    @Test
    public void testDeleteSecretDeletesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        creds.put(secretName, new CredentialsUsername("my-user"));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(output).isEmpty();
    }

    @Test
    public void testDeleteNonExistantSecretReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "NON_EXISTANT_SECRET";
        creds.put("BOB", new CredentialsUsername("my-user"));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(output, 5076, "GAL5076E",
            "Error occurred. A secret with the provided ID does not exist.");
    }

    @Test
    public void testDeleteSecretWithFailingCredsStoreReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        MockCredentialsService credsService = new MockCredentialsService(creds);

        // Force a server error from the creds service
        credsService.setThrowError(true);

        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(output, 5078, "GAL5078E",
            "Failed to delete a secret with the given ID from the credentials store");
    }
}
