/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

public enum AcceptContentType {
    APPLICATION_JSON("application/json"),
    APPLICATION_YAML("application/yaml"),
    TEXT_PLAIN("text/plain"),
    ALL("*/*")
    ;

    private String type;
    private String anySubtype;

    AcceptContentType(String value) {
        this.type = value;
        this.anySubtype = value.split("/")[0] + "/*";
    }

    public boolean isInHeader(String headerValue) {
        String header = headerValue.trim().toLowerCase();
        return header.equals(this.type) || header.equals(this.anySubtype) || header.equals(ALL.type);
    }

    public static AcceptContentType getFromString(String typeAsString) {
        AcceptContentType match = null;
        for (AcceptContentType possibleMatch : values()) {
            if (possibleMatch.toString().equalsIgnoreCase(typeAsString)) {
                match = possibleMatch;
            }
        }
        return match;
    }

    @Override
    public String toString(){
        return type;
    }
}
