/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GalasaGsonBuilder {

    private GsonBuilder builder;
    /**
     * Creates a Gson builder to transform objects to JSON for use on the API endpoints.
     * HTMLEscaping is disabled as when it is enabled it will tranform special characters to their unicode character references
     * @return GsonBuilder
     */
    public GalasaGsonBuilder () {
        builder = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(Instant.class, new GsonInstantTypeAdapater())
            .setPrettyPrinting();
    }

    public void registerTypeAdapter(Type type, Object object){
        builder.registerTypeAdapter(type, object);
    }
    
    public GsonBuilder getBuilder(){
        return builder;
    }
    public Gson getGson() {
        return builder.create();
    }

}
