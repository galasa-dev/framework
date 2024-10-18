/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonArray;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.GalasaSecretdata;
import dev.galasa.framework.api.beans.generated.GalasaSecretmetadata;
import dev.galasa.framework.api.beans.generated.GalasaSecretmetadatatype;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockCredentialsService;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.api.secrets.internal.routes.SecretsRoute;
import dev.galasa.framework.api.secrets.mocks.MockSecretsServlet;
import dev.galasa.framework.spi.creds.CredentialsUsername;

public class SecretsRouteTest extends BaseServletTest {

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
    public void testGetSecretByNameReturnsSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        creds.put(secretName, new CredentialsUsername("my-user"));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/");
        mockRequest.setQueryParameter("name", secretName);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        JsonArray outputJson = gson.fromJson(outStream.toString(), JsonArray.class);
        assertThat(outputJson).hasSize(1);

        GalasaSecret outputSecret = gson.fromJson(outputJson.get(0).getAsJsonObject(), GalasaSecret.class);
        assertThat(outputSecret.getApiVersion()).isEqualTo(GalasaSecretType.DEFAULT_API_VERSION);

        GalasaSecretmetadata outputMetadata = outputSecret.getmetadata();
        GalasaSecretdata outputData = outputSecret.getdata();
        assertThat(outputMetadata.getname()).isEqualTo(secretName);
        assertThat(outputMetadata.gettype()).isEqualTo(GalasaSecretmetadatatype.Username);
        assertThat(outputData.getusername()).isEqualTo("my-user");
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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/");
        mockRequest.setQueryParameter("name", secretName);

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
    public void testGetSecretByNameWithBlankNameReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "     ";
        creds.put("BOB", new CredentialsUsername("my-user"));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/");
        mockRequest.setQueryParameter("name", secretName);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(output, 5090, "Invalid secret name provided");
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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/");
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
}
