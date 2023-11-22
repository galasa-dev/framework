/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import org.junit.Test;

import com.google.gson.*;

import java.net.HttpURLConnection;
import static org.assertj.core.api.Assertions.*;

public class TestInternalServletException {

    private InternalServletException createException() {
        ServletErrorMessage template = ServletErrorMessage.GAL5015_INTERNAL_CPS_ERROR ;
        ServletError error = new ServletError(template);
        int code = HttpURLConnection.HTTP_INTERNAL_ERROR;
        Exception cause = new Exception("Original simulated cause");
        InternalServletException ex = new InternalServletException(error, code, cause);
        return ex;
    }

    @Test
    public void TestCanCreateExceptionWithCause() {
        InternalServletException ex = createException();
        assertThat(ex).isNotNull();
    }

    // @Test
    // public void TestSerialisedServletExceptionJsonIsAsExpected() {
    //     Gson gson = new Gson();
    //     InternalServletException ex = createException();
    //     JsonElement jsonElement = gson.toJsonTree(ex);
    //     JsonObject jsonObject = jsonElement.getAsJsonObject();
    //     assertThat(jsonObject.isJsonObject()).isTrue();
    //     JsonPrimitive errorField = jsonObject.getAsJsonPrimitive("error");
    //     assertThat(errorField).isNotNull();
    // }
}
 