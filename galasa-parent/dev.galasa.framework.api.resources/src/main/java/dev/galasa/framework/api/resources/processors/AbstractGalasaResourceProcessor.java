/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.utils.GalasaGson;

public abstract class AbstractGalasaResourceProcessor {
    protected static final Set<String> updateActions = Collections.unmodifiableSet(Set.of("apply","update"));
    protected static final GalasaGson gson = new GalasaGson();

    protected void checkResourceHasRequiredFields(JsonObject resourceJson, String expectedApiVersion) throws InternalServletException {
        if (!resourceJson.has("apiVersion")
                || !resourceJson.has("metadata")
                || !resourceJson.has("data")) {
            // Caused by bad Key Names in the JSON object i.e. apiversion instead of apiVersion
            ServletError error = new ServletError(GAL5069_MISSING_REQUIRED_FIELDS);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        String apiVersion = resourceJson.get("apiVersion").getAsString();
        if (!apiVersion.equals(expectedApiVersion)) {
            ServletError error = new ServletError(GAL5027_UNSUPPORTED_API_VERSION, apiVersion, expectedApiVersion);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
