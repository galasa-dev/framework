/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.validators;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.api.common.resources.SecretValidator;

public class GalasaSecretValidator extends SecretValidator<JsonObject> {

    public GalasaSecretValidator(ResourceAction action) {
        super(action);
    }

    @Override
    public void validate(JsonObject secretJson) throws InternalServletException {
        checkResourceHasRequiredFields(secretJson, DEFAULT_API_VERSION);
        
        validateSecretMetadata(secretJson);

        // Delete operations shouldn't require a 'data' section, just the metadata to identify
        // the credentials entry to delete
        if (validationErrors.isEmpty() && action != DELETE) {
            validateSecretData(secretJson);
        }
    }

    private void validateSecretMetadata(JsonObject secretJson) {
        JsonObject metadata = secretJson.get("metadata").getAsJsonObject();

        // Check if the secret has a name and a type
        if (!metadata.has("name") || !metadata.has("type")) {
            ServletError error = new ServletError(GAL5070_INVALID_GALASA_SECRET_MISSING_FIELDS, "metadata", "name, type");
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }

        if (metadata.has("name")) {
            try {
                validateSecretName(metadata.get("name").getAsString());
            } catch (InternalServletException e) {
                validationErrors.add(e.getMessage());
            }
        }

        // If a description is provided, check that it is valid
        if (metadata.has("description")) {
            try {
                validateDescription(metadata.get("description").getAsString());
            } catch (InternalServletException e) {
                validationErrors.add(e.getMessage());
            }
        }

        // Check if the given secret type is a valid type
        if (metadata.has("type")) {
            GalasaSecretType secretType = GalasaSecretType.getFromString(metadata.get("type").getAsString());
            if (secretType == null) {
                String supportedSecretTypes = Arrays.stream(GalasaSecretType.values())
                    .map(GalasaSecretType::toString)
                    .collect(Collectors.joining(", "));
    
                ServletError error = new ServletError(GAL5074_UNKNOWN_GALASA_SECRET_TYPE, supportedSecretTypes);
                validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
            }
        }

        // Check if the given encoding scheme is supported
        if (metadata.has("encoding") && !SUPPORTED_ENCODING_SCHEMES.contains(metadata.get("encoding").getAsString())) {
            ServletError error = new ServletError(GAL5073_UNSUPPORTED_GALASA_SECRET_ENCODING, String.join(", ", SUPPORTED_ENCODING_SCHEMES));
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }

    private void validateSecretData(JsonObject secretJson) {
        JsonObject metadata = secretJson.get("metadata").getAsJsonObject();
        JsonObject data = secretJson.get("data").getAsJsonObject();

        GalasaSecretType secretType = GalasaSecretType.getFromString(metadata.get("type").getAsString());
        String[] requiredTypeFields = secretType.getRequiredDataFields();
        List<String> missingFields = getMissingResourceFields(data, Arrays.asList(requiredTypeFields));

        if (!missingFields.isEmpty()) {
            ServletError error = new ServletError(GAL5072_INVALID_GALASA_SECRET_MISSING_TYPE_DATA, secretType.toString(), String.join(", ", missingFields));
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }
}
