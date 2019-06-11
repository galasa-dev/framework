package dev.voras.framework.spi.utils;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CirilloGsonBuilder {

	public static Gson build() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Instant.class, new GsonInstantTypeAdapater());
		return builder.setPrettyPrinting().create();
	}
	
}
