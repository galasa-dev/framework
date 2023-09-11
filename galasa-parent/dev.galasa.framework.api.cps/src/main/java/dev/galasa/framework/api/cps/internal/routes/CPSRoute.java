/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import dev.galasa.framework.api.cps.internal.verycommon.BaseRoute;
import dev.galasa.framework.api.cps.internal.verycommon.ResponseBuilder;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;


/**
 * An abstract route used by all the Run-related routes.
 */
public abstract class CPSRoute extends BaseRoute {

    static final Gson gson = GalasaGsonBuilder.build();

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };

    private IFramework framework;

    /*******************************
    This section will probably move to the super class
    START
    ********************************/

      protected final static Set<String> hiddenNameSpaces = new HashSet<>();
    static {
        hiddenNameSpaces.add("dss");
    }

    /**
     * Some namespaces are able to be set, but cannot be queried.
     *
     * When they are queried, the values are redacted
     */
    private final static Set<String> writeOnlyNameSpaces = new HashSet<>();
    static {
        writeOnlyNameSpaces.add("secure");
    }

    /*******************************
    This section will probably move to the super class
    END
    ********************************/

    public CPSRoute(ResponseBuilder responseBuilder, String path , IFramework framework ) {
        super(responseBuilder, path);
        this.framework = framework;
    }

    protected IFramework getFramework() {
        return this.framework;
    }

}