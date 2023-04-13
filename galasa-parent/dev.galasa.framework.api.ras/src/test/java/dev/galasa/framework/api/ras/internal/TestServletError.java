/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.Test;
import static dev.galasa.framework.api.ras.internal.mocks.MockErrorMessage.*;

public class TestServletError{

    @Test
    public void TemplateImportsWithoutParamaters (){
        String message = new ServletError(ERROR_TEST_000).toString();
    }
}