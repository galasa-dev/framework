/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class GalasaGson {

    static final Gson gson = GalasaGsonBuilder.build();

    public String toJson(Object obj){
        return gson.toJson(obj);
    }

    public <T> T fromJson(String json, Class<T> classOfT)  {
        return gson.fromJson(json,  classOfT);   
    }
    
    public <T> T fromJson(InputStreamReader inputStreamReqader, Class<T> classOfT)  {
        return gson.fromJson(inputStreamReqader,  classOfT);   
    }
    
    public <T> T fromJson(JsonObject jsonObject, Class<T> classOfT)  {
        return gson.fromJson(jsonObject,  classOfT);   
    }
    
    public <T> T fromJson(JsonReader jsonReader, Class<T> classOfT)  {
        return gson.fromJson(jsonReader,  classOfT);   
    }
    
    public JsonObject fromJson(BufferedReader bufferedReader, Class<JsonObject> classOfT)  {
        return gson.fromJson(bufferedReader,  classOfT);   
    }

    public JsonElement toJsonTree(Object obj){
        return gson.toJsonTree(obj);
    }

    public JsonReader newJsonReader(StringReader stringReader) {
        return gson.newJsonReader(stringReader);
    }
}
