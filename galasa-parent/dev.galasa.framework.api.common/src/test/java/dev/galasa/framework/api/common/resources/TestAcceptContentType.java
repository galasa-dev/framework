/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TestAcceptContentType {

    @Test
    public void testAcceptContentJSONReturnString() throws Exception {
        assertThat(AcceptContentType.APPLICATION_JSON.toString()).isEqualTo("application/json");
    }

    @Test
    public void testAcceptContentYAMLReturnString() throws Exception {
        assertThat(AcceptContentType.APPLICATION_YAML.toString()).isEqualTo("application/yaml");
    }

    @Test
    public void testAcceptContentTextReturnString() throws Exception {
        assertThat(AcceptContentType.TEXT_PLAIN.toString()).isEqualTo("text/plain");
    }

    @Test
    public void testAcceptContentAnyReturnString() throws Exception {
        assertThat(AcceptContentType.ALL.toString()).isEqualTo("*/*");
    }

    @Test
    public void testAcceptContentJSONReturnTrue() throws Exception {
        // Given...
        AcceptContentType contentType = AcceptContentType.APPLICATION_JSON;

        // Then...
        assertThat(contentType.isInHeader("application/json")).isTrue();
        assertThat(contentType.isInHeader("application/*")).isTrue();
        assertThat(contentType.isInHeader("*/*")).isTrue();
    }

    @Test
    public void testAcceptContentYAMLReturnTrue() throws Exception {
        // Given...
        AcceptContentType contentType = AcceptContentType.APPLICATION_YAML;

        // Then...
        assertThat(contentType.isInHeader("application/yaml")).isTrue();
        assertThat(contentType.isInHeader("application/*")).isTrue();
        assertThat(contentType.isInHeader("*/*")).isTrue();
    }

    @Test
    public void testAcceptContentTextReturnTrue() throws Exception {
        // Given...
        AcceptContentType contentType = AcceptContentType.TEXT_PLAIN;

        // Then...
        assertThat(contentType.isInHeader("text/plain")).isTrue();
        assertThat(contentType.isInHeader("text/*")).isTrue();
        assertThat(contentType.isInHeader("*/*")).isTrue();
    }

    @Test
    public void testAcceptContentAllReturnTrue() throws Exception {
        // Given...
        AcceptContentType contentType = AcceptContentType.ALL;

        // Then...
        assertThat(contentType.isInHeader("*/*")).isTrue();

        assertThat(contentType.isInHeader("text/*")).isFalse();
        assertThat(contentType.isInHeader("application/json")).isFalse();
    }
    

    @Test
    public void testAcceptContentWithInvalidTypeReturnFalse() throws Exception {
        // Given...
        AcceptContentType contentType = AcceptContentType.APPLICATION_JSON;

        // Then...
        assertThat(contentType.isInHeader("application/json/not/good!")).isFalse();
    }

    @Test
    public void TestAcceptContentTypeFromStringFullTypeReturnsType() throws Exception {
        // Given...
        String headerValue = "application/json";

        // Then...
        assertThat(AcceptContentType.getFromString(headerValue)).isEqualTo(AcceptContentType.APPLICATION_JSON);
    }
    
}
