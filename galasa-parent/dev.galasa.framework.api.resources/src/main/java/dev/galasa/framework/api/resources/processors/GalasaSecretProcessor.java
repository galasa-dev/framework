/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import java.util.List;

import com.google.gson.JsonObject;

public class GalasaSecretProcessor implements IGalasaResourceProcessor {

    @Override
    public List<String> processResource(JsonObject resource, String action) {
        throw new UnsupportedOperationException("Unimplemented method 'processResource'");
    }   
}
