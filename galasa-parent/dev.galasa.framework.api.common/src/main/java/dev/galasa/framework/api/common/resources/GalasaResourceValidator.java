/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.IBeanValidator;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;

/**
 * An abstract class containing the base methods used to validate Galasa resources.
 */
public abstract class GalasaResourceValidator<T> extends AbstractValidator implements IBeanValidator<T> {

    public static final String DEFAULT_API_VERSION = "galasa-dev/v1alpha1";

    protected List<String> validationErrors = new ArrayList<>();
    protected ResourceAction action;

    public GalasaResourceValidator() {}

    public GalasaResourceValidator(ResourceAction action) {
        this.action = action;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    private List<String> getRequiredResourceFields() {
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("apiVersion");
        requiredFields.add("metadata");
        if (action != DELETE) {
            requiredFields.add("data");
        }
        return requiredFields;
    }

    protected List<String> getMissingResourceFields(JsonObject resourceJson, List<String> requiredFields) {
        List<String> missingFields = new ArrayList<>();
        for (String field : requiredFields) {
            if (!resourceJson.has(field)) {
                missingFields.add(field);
            }
        }
        return missingFields;
    }

    protected void checkResourceHasRequiredFields(
        JsonObject resourceJson,
        String expectedApiVersion
    ) throws InternalServletException {
        List<String> requiredFields = getRequiredResourceFields();
        List<String> missingFields = getMissingResourceFields(resourceJson, requiredFields);
        if (!missingFields.isEmpty()) {
            ServletError error = new ServletError(GAL5069_MISSING_REQUIRED_FIELDS, String.join(", ", missingFields));
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        String apiVersion = resourceJson.get("apiVersion").getAsString();
        if (!apiVersion.equals(expectedApiVersion)) {
            ServletError error = new ServletError(GAL5027_UNSUPPORTED_API_VERSION, expectedApiVersion);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
