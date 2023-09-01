/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.verycommon;

import java.text.MessageFormat;

import com.google.gson.*;

import dev.galasa.framework.spi.FrameworkException;

public class ServletError extends FrameworkException {

    String [] params ;
    ServletErrorMessage template ;

    public ServletError( ServletErrorMessage template , String... params ) {

        this.template = template;
        this.params = params;
    }

    public String toString() {

        String templateString = template.toString();
        int templateNumber = template.getTemplateNumber();
        String message = MessageFormat.format(templateString, (Object[])params);

        JsonObject obj = new JsonObject();
        obj.addProperty("error_code", templateNumber);
		obj.addProperty("error_message", message);

        String renderedJsonMessage = obj.toString();

        return renderedJsonMessage ;
    }
    
}