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
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.mocks.MockCredentialsService;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.secrets.internal.routes.SecretsRoute;
import dev.galasa.framework.api.secrets.mocks.MockSecretsServlet;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;

public class SecretsRouteTest extends BaseServletTest {

    private JsonObject createSecretJson(String value, String encoding) {
        JsonObject secretJson = new JsonObject();
        if (value != null) {
            secretJson.addProperty("value", value);
        }

        if (encoding != null) {
            secretJson.addProperty("encoding", encoding);
        }

        return secretJson;
    }

    private JsonObject createSecretJson(String value) {
        return createSecretJson(value, null);
    }

    @Test
    public void testSecretsRouteRegexMatchesExpectedPaths() throws Exception {
        // Given...
        Pattern routePattern = new SecretsRoute(null, null).getPath();

        // Then...
        // The servlet's whiteboard pattern will match /secrets, so the secrets route
        // should only allow an optional / or an empty string (no suffix after "/secrets")
        assertThat(routePattern.matcher("/").matches()).isTrue();
        assertThat(routePattern.matcher("").matches()).isTrue();

        // The route should not accept the following
        assertThat(routePattern.matcher("////").matches()).isFalse();
        assertThat(routePattern.matcher("/wrongpath!").matches()).isFalse();
    }

    @Test
    public void testCreateUsernamePasswordSecretCreatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String username = "my-username";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(output).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsernamePassword createdCredentials = (CredentialsUsernamePassword) credsService.getCredentials(secretName);
        assertThat(createdCredentials).isNotNull();
        assertThat(createdCredentials.getUsername()).isEqualTo(username);
        assertThat(createdCredentials.getPassword()).isEqualTo(password);
    }

    @Test
    public void testCreateTokenSecretCreatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB_TOKEN";
        String token = "my-token";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("token", createSecretJson(token));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(output).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsToken createdCredentials = (CredentialsToken) credsService.getCredentials(secretName);
        assertThat(createdCredentials).isNotNull();
        assertThat(createdCredentials.getToken()).isEqualTo(token.getBytes());
    }

    @Test
    public void testCreateBase64EncodedSecretCreatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String username = "my-username";
        String encoding = "base64";
        String encodedUsername = Base64.getEncoder().encodeToString(username.getBytes());

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(encodedUsername, encoding));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(output).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsername createdCredentials = (CredentialsUsername) credsService.getCredentials(secretName);
        assertThat(createdCredentials).isNotNull();
        assertThat(createdCredentials.getUsername()).isEqualTo(username);
    }

    @Test
    public void testCreateBase64EncodedSecretWithBadlyEncodedDataThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String username = "this isn't base64 encoded!";
        String encoding = "base64";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username, encoding));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5095, "GAL5095E",
            "Expected the value to be encoded in 'base64' format but it was not.");
    }

    @Test
    public void testCreateSecretWithPasswordAndTokenThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String token = "my-token";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("token", createSecretJson(token));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5093, "GAL5093E",
            "The 'password' and 'token' fields are mutually exclusive");
    }

    @Test
    public void testCreateSecretWithMissingSecretNameThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String username = "my-username";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5090, "GAL5090E",
            "The name of a Galasa secret cannot be empty or contain only spaces or tabs");
    }

    @Test
    public void testCreateSecretWithBlankSecretNameThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "      ";
        String username = "my-username";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5090, "GAL5090E",
            "The name of a Galasa secret cannot be empty or contain only spaces or tabs");
    }

    @Test
    public void testCreateSecretWithMissingUsernameValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = null;
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5094, "GAL5094E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithMissingPasswordValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = "my-username";
        String password = null;

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5094, "GAL5094E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithMissingTokenValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = "my-username";
        String token = null;

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("token", createSecretJson(token));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5094, "GAL5094E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithBlankUsernameValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = " ";
        String token = "my-token";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("token", createSecretJson(token));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5094, "GAL5094E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithBlankPasswordValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = "my-username";
        String password = " ";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5094, "GAL5094E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithBlankTokenValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String token = " ";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("token", createSecretJson(token));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5094, "GAL5094E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithUnknownEncodingValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String token = "my-token";
        String encoding = "UNKNOWN ENCODING!";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("token", createSecretJson(token, encoding));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5073, "GAL5073E",
            "Unsupported data encoding scheme provided");
    }

    @Test
    public void testCreateSecretWithPasswordAndMissingUsernameThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5096, "GAL5096E",
            "A 'password' field was provided but the 'username' field was missing");
    }

    @Test
    public void testCreateSecretWithExistingSecretNameThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-EXISTING-SECRET";
        creds.put(secretName, new CredentialsUsername("my-username"));

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson("my-new-username"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(secretJsonStr, "/");
        mockRequest.setMethod(HttpMethod.POST.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(409);
        checkErrorStructure(output, 5075, "GAL5075E",
            "A secret with the provided name already exists");
    }
}
