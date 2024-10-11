/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.spi.creds.ICredentialsService;

/**
 * Processor class to handle creating, updating, and deleting GalasaSecret resources
 */
public class GalasaSecretProcessor extends AbstractGalasaResourceProcessor implements IGalasaResourceProcessor {
    
    private static final String DEFAULT_API_VERSION = "galasa-dev/v1alpha1";
    private static final List<String> SUPPORTED_ENCODING_SCHEMES = List.of("base64");

    private ICredentialsService credentialsService;

    public GalasaSecretProcessor(ICredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @Override
    public List<String> processResource(JsonObject resource, String action) throws InternalServletException {
        List<String> errors = checkGalasaSecretJsonStructure(resource);
        return errors;
    }


    private List<String> checkGalasaSecretJsonStructure(JsonObject secretJson) throws InternalServletException {
        checkResourceHasRequiredFields(secretJson, DEFAULT_API_VERSION);
        
        List<String> validationErrors = new ArrayList<>();
        validateSecretMetadata(secretJson, validationErrors);
        validateSecretData(secretJson, validationErrors);
        return validationErrors;
    }

    private void validateSecretMetadata(JsonObject secretJson, List<String> validationErrors) {
        JsonObject metadata = secretJson.get("metadata").getAsJsonObject();
        if (!metadata.has("name") || !metadata.has("type")) {
            ServletError error = new ServletError(GAL5070_INVALID_GALASA_SECRET_MISSING_FIELDS, "metadata", "name, type");
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        } else {
            if (metadata.has("encoding") && !SUPPORTED_ENCODING_SCHEMES.contains(metadata.get("encoding").getAsString())) {
                ServletError error = new ServletError(GAL5073_UNSUPPORTED_GALASA_SECRET_ENCODING, String.join(", ", SUPPORTED_ENCODING_SCHEMES));
                validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
            }
        }
    }

    private void validateSecretData(JsonObject secretJson, List<String> validationErrors) {
        JsonObject metadata = secretJson.get("metadata").getAsJsonObject();
        GalasaSecretType secretType = GalasaSecretType.getFromString(metadata.get("type").getAsString());
        if (secretType == null) {
            String supportedSecretTypes = Arrays.stream(GalasaSecretType.values())
                .map(GalasaSecretType::toString)
                .collect(Collectors.joining(", "));

            ServletError error = new ServletError(GAL5073_UNKNOWN_GALASA_SECRET_TYPE, supportedSecretTypes);
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        } else {
            JsonObject data = secretJson.get("data").getAsJsonObject();
            validateSecretTypeFieldsPresent(data, secretType, validationErrors);
        }
    }

    private void validateSecretTypeFieldsPresent(JsonObject data, GalasaSecretType secretType, List<String> validationErrors) {
        String[] requiredTypeFields = secretType.getRequiredDataFields();

        List<String> missingFields = new ArrayList<>();
        for (String requiredField : requiredTypeFields) {
            if (!data.has(requiredField)) {
                missingFields.add(requiredField);
            }
        }

        if (!missingFields.isEmpty()) {
            ServletError error = new ServletError(GAL5070_INVALID_GALASA_SECRET_MISSING_FIELDS, "data", String.join(", ", missingFields));
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }
}
