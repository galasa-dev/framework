/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.spi.utils.GalasaGson;

public abstract class AbstractGalasaResourceProcessor {
    protected static final Set<ResourceAction> updateActions = Set.of(APPLY, UPDATE);
    protected static final GalasaGson gson = new GalasaGson();

    protected List<String> checkGalasaResourceJsonStructure(GalasaResourceValidator<JsonObject> validator, JsonObject propertyJson) throws InternalServletException {
        validator.validate(propertyJson);

        List<String> validationErrors = validator.getValidationErrors();
        return validationErrors;
    }
}
