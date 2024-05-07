package dev.galasa.framework.api.authentication.mocks;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.IOidcProvider;

public class MockOidcProvider implements IOidcProvider {

    private String redirectUrl;

    private boolean isJwtValid = false;

    private boolean throwException = false;

    private HttpResponse<String> mockResponse;

    public MockOidcProvider() {}

    public MockOidcProvider(boolean throwException) {
        this.throwException = throwException;
    }

    public MockOidcProvider(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public MockOidcProvider(HttpResponse<String> mockResponse) {
        this.mockResponse = mockResponse;
    }

    public void setJwtValid(boolean isJwtValid) {
        this.isJwtValid = isJwtValid;
    }

    @Override
    public JsonObject getOpenIdConfiguration() throws IOException, InterruptedException {
        return null;
    }

    @Override
    public boolean isJwtValid(String jwt)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        if (throwException) {
            throw new IOException("simulating an unexpected failure!");
        }
        return isJwtValid;
    }

    @Override
    public String getConnectorRedirectUrl(String clientId, String callbackUrl, HttpSession session)
            throws IOException, InterruptedException {
        if (throwException) {
            throw new IOException("simulating an unexpected failure!");
        }
        return redirectUrl;
    }

    @Override
    public HttpResponse<String> sendTokenPost(String clientId, String clientSecret, String refreshToken)
            throws IOException, InterruptedException {
        if (throwException) {
            throw new IOException("simulating an unexpected failure!");
        }
        return mockResponse;
    }

    @Override
    public HttpResponse<String> sendTokenPost(String clientId, String clientSecret, String authCode, String redirectUri)
            throws IOException, InterruptedException {
        if (throwException) {
            throw new IOException("simulating an unexpected failure!");
        }
        return mockResponse;
    }

    @Override
    public HttpResponse<String> sendAuthorizationGet(String clientId, String callbackUrl, HttpSession session)
            throws IOException, InterruptedException {
        if (throwException) {
            throw new IOException("simulating an unexpected failure!");
        }
        return mockResponse;
    }
}
