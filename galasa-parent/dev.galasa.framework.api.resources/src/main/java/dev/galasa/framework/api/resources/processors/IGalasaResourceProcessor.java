/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import java.util.List;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;

public interface IGalasaResourceProcessor {
    List<String> processResource(JsonObject json, String action) throws InternalServletException;
}
