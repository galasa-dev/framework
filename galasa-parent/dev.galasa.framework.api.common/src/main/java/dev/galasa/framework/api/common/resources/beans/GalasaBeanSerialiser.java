package dev.galasa.framework.api.common.resources.beans;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class GalasaBeanSerialiser {

    static final Gson gson = GalasaGsonBuilder.build();

    public GalasaBeanSerialiser() {
    }

    public GalasaProperty getPropertyFromJsonString( String jsonString) throws IOException, InternalServletException{
        GalasaProperty property;
        try {
            property = gson.fromJson(jsonString, GalasaProperty.class);
        }catch (Exception e){
            ServletError error = new ServletError(GAL5023_UNABLE_TO_CAST_TO_GALASAPROPERTY, jsonString);  
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return property;
    }
}
