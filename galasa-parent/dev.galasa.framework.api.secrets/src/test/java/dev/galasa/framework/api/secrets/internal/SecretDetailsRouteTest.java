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
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.mocks.MockCredentialsService;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.secrets.internal.routes.SecretDetailsRoute;
import dev.galasa.framework.api.secrets.mocks.MockSecretsServlet;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;

public class SecretDetailsRouteTest extends SecretsServletTest {

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

        String expectedJson = gson.toJson(generateSecretJson(secretName, "Username", username, null, null));
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
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5091, "GAL5091E",
            "Unable to retrieve a secret with the given name");
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
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5092, "GAL5092E",
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
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();
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
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5076, "GAL5076E",
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
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5078, "GAL5078E",
            "Failed to delete a secret with the given ID from the credentials store");
    }

    @Test
    public void testUpdateSecretUsernameUpdatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String oldPassword = "not-a-password";
        String newUsername = "my-new-username";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsernamePassword(oldUsername, oldPassword));

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(newUsername));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/" + secretName);
        mockRequest.setMethod(HttpMethod.PUT.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsername updatedCredentials = (CredentialsUsername) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getUsername()).isEqualTo(newUsername);
    }

    @Test
    public void testUpdateSecretUsernamePasswordUpdatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String oldPassword = "not-a-password";
        String newUsername = "my-new-username";
        String newPassword = "my-new-password";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsernamePassword(oldUsername, oldPassword));

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(newUsername));
        secretJson.add("password", createSecretJson(newPassword));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/" + secretName);
        mockRequest.setMethod(HttpMethod.PUT.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsernamePassword updatedCredentials = (CredentialsUsernamePassword) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getUsername()).isEqualTo(newUsername);
        assertThat(updatedCredentials.getPassword()).isEqualTo(newPassword);
    }

    @Test
    public void testUpdateSecretUsernameTokenUpdatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String newToken = "my-new-token";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsername(oldUsername));

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/" + secretName);
        mockRequest.setMethod(HttpMethod.PUT.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsToken updatedCredentials = (CredentialsToken) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getToken()).isEqualTo(newToken.getBytes());
    }

    @Test
    public void testUpdateSecretWithMixedEncodingUpdatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String newUsername = "my-new-username";
        String newToken = "my-new-token";
        String newTokenEncoded = Base64.getEncoder().encodeToString(newToken.getBytes());

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsername(oldUsername));

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(newUsername));
        secretJson.add("token", createSecretJson(newTokenEncoded, "base64"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/" + secretName);
        mockRequest.setMethod(HttpMethod.PUT.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsernameToken updatedCredentials = (CredentialsUsernameToken) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getUsername()).isEqualTo(newUsername);
        assertThat(updatedCredentials.getToken()).isEqualTo(newToken.getBytes());
    }

    @Test
    public void testUpdateNonExistantSecretCreatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String newToken = "my-new-token";

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/" + secretName);
        mockRequest.setMethod(HttpMethod.PUT.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        // Expect a 201 Created
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsToken updatedCredentials = (CredentialsToken) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getToken()).isEqualTo(newToken.getBytes());
    }

    @Test
    public void testUpdateSecretWithUnknownTokenEncodingReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String newToken = "my-new-token";

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken, "UNKNOWN ENCODING!"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/" + secretName);
        mockRequest.setMethod(HttpMethod.PUT.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5073, "GAL5073E",
            "Unsupported data encoding scheme provided");
    }

    @Test
    public void testUpdateSecretWithUnknownUsernameEncodingReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String newUsername = "my-new-username";

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(newUsername, "UNKNOWN ENCODING!"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/" + secretName);
        mockRequest.setMethod(HttpMethod.PUT.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5073, "GAL5073E",
            "Unsupported data encoding scheme provided");
    }
}
