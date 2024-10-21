 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import java.util.List;

public enum GalasaSecretType {
    USERNAME_PASSWORD("UsernamePassword", "username", "password"),
    USERNAME_TOKEN("UsernameToken", "username", "token"),
    USERNAME("Username", "username"),
    TOKEN("Token", "token");

    public static final String DEFAULT_API_VERSION = "galasa-dev/v1alpha1";
    public static final List<String> SUPPORTED_ENCODING_SCHEMES = List.of("base64");

    private String name;
    private String[] requiredDataFields;

    private GalasaSecretType(String type, String... requiredDataFields) {
        this.name = type;
        this.requiredDataFields = requiredDataFields;
    }

    public static GalasaSecretType getFromString(String typeAsString) {
        GalasaSecretType match = null;
        for (GalasaSecretType resource : values()) {
            if (resource.toString().equalsIgnoreCase(typeAsString.trim())) {
                match = resource;
                break;
            }
        }
        return match;
    }

    @Override
    public String toString() {
        return name;
    }

    public String[] getRequiredDataFields() {
        return requiredDataFields;
    }
}