/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GalasaGsonBuilder {

    /**
     * Creates a Gson builder to transform objects to JSON for use on the API endpoints.
     * HTMLEscaping is disabled as when it is enabled it will tranform special characters to their unicode character references
     * @return GsonBuilder
     */
    public static Gson build() {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
        builder.registerTypeAdapter(Instant.class, new GsonInstantTypeAdapater());
        return builder.setPrettyPrinting().create();
    }

}
