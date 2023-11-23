/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.net.ssl.SSLSession;

/**
 * A mock implementation of the HttpResponse interface.
 */
public class MockHttpResponse<T> implements HttpResponse<T> {

    private T body;

    public MockHttpResponse(T body) {
        this.body = body;
    }

    @Override
    public T body() {
        return body;
    }

    @Override
    public int statusCode() {
        throw new UnsupportedOperationException("Unimplemented method 'statusCode'");
    }

    @Override
    public HttpRequest request() {
        throw new UnsupportedOperationException("Unimplemented method 'request'");
    }

    @Override
    public Optional<HttpResponse<T>> previousResponse() {
        throw new UnsupportedOperationException("Unimplemented method 'previousResponse'");
    }

    @Override
    public HttpHeaders headers() {
        throw new UnsupportedOperationException("Unimplemented method 'headers'");
    }

    @Override
    public Optional<SSLSession> sslSession() {
        throw new UnsupportedOperationException("Unimplemented method 'sslSession'");
    }

    @Override
    public URI uri() {
        throw new UnsupportedOperationException("Unimplemented method 'uri'");
    }

    @Override
    public Version version() {
        throw new UnsupportedOperationException("Unimplemented method 'version'");
    }

}
