/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi.utils;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GalasaGsonBuilder {

    public static Gson build() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Instant.class, new GsonInstantTypeAdapater());
        return builder.setPrettyPrinting().create();
    }

}
