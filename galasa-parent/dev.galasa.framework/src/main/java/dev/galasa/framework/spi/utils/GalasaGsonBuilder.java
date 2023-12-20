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

    public static Gson build() {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
        builder.registerTypeAdapter(Instant.class, new GsonInstantTypeAdapater());
        return builder.setPrettyPrinting().create();
    }

}
