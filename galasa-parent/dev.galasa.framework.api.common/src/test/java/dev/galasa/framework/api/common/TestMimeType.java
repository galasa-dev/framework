/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TestMimeType {

    @Test
    public void testMimeTypeJsonReturnString() throws Exception {
        assertThat(MimeType.APPLICATION_JSON.toString()).isEqualTo("application/json");
    }

    @Test
    public void testMimeTypeYamlReturnString() throws Exception {
        assertThat(MimeType.APPLICATION_YAML.toString()).isEqualTo("application/yaml");
    }

    @Test
    public void testMimeTypeTextReturnString() throws Exception {
        assertThat(MimeType.TEXT_PLAIN.toString()).isEqualTo("text/plain");
    }

    @Test
    public void testMimeTypeJsonReturnTrue() throws Exception {
        // Given...
        MimeType contentType = MimeType.APPLICATION_JSON;

        // Then...
        assertThat(contentType.matchesType("application/json")).isTrue();
        assertThat(contentType.matchesType("application/*")).isTrue();
        assertThat(contentType.matchesType("*/*")).isTrue();
    }

    @Test
    public void testMimeTypeYamlReturnTrue() throws Exception {
        // Given...
        MimeType contentType = MimeType.APPLICATION_YAML;

        // Then...
        assertThat(contentType.matchesType("application/yaml")).isTrue();
        assertThat(contentType.matchesType("application/*")).isTrue();
        assertThat(contentType.matchesType("*/*")).isTrue();
    }

    @Test
    public void testMimeTypeTextReturnTrue() throws Exception {
        // Given...
        MimeType contentType = MimeType.TEXT_PLAIN;

        // Then...
        assertThat(contentType.matchesType("text/plain")).isTrue();
        assertThat(contentType.matchesType("text/plain   ")).isTrue();
        assertThat(contentType.matchesType(" text/plain   ")).isTrue();
        assertThat(contentType.matchesType("text/*")).isTrue();
        assertThat(contentType.matchesType("*/*")).isTrue();
    }

    @Test
    public void testJsonMimeTypeWithInvalidTypeReturnFalse() throws Exception {
        assertThat(MimeType.APPLICATION_JSON.matchesType("application/  json")).isFalse();
        assertThat(MimeType.APPLICATION_JSON.matchesType("application/json/not/good!")).isFalse();
        assertThat(MimeType.APPLICATION_JSON.matchesType("application/*/not/good!")).isFalse();
        assertThat(MimeType.APPLICATION_JSON.matchesType("json/application")).isFalse();
    }

    @Test
    public void testYamlMimeTypeWithInvalidTypeReturnFalse() throws Exception {
        assertThat(MimeType.APPLICATION_YAML.matchesType("application/  yaml")).isFalse();
        assertThat(MimeType.APPLICATION_YAML.matchesType("application/yaml/not/good!")).isFalse();
        assertThat(MimeType.APPLICATION_YAML.matchesType("application/*/not/good!")).isFalse();
        assertThat(MimeType.APPLICATION_YAML.matchesType("yaml/application")).isFalse();
    }

    @Test
    public void testTextMimeTypeWithInvalidTypeReturnFalse() throws Exception {
        assertThat(MimeType.TEXT_PLAIN.matchesType("text/ plain")).isFalse();
        assertThat(MimeType.TEXT_PLAIN.matchesType("text/plain/not/good!")).isFalse();
        assertThat(MimeType.TEXT_PLAIN.matchesType("text/*//")).isFalse();
        assertThat(MimeType.TEXT_PLAIN.matchesType("plain/text")).isFalse();
    }
}
