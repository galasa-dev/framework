/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

@Component(service = { SetupEcosystem.class })
public class SetupEcosystem {

    private Log logger = LogFactory.getLog(this.getClass());

    private static final String DSS_ENV_VAR   = "GALASA_DYNAMICSTATUS_STORE";
    private static final String RAS_ENV_VAR   = "GALASA_RESULTARCHIVE_STORE";
    private static final String CREDS_ENV_VAR = "GALASA_CREDENTIALS_STORE";
    private static final String AUTH_ENV_VAR  = "GALASA_AUTH_STORE";

    private IFramework framework;

    /**
     * <p>Setup the Ecosystem for remote usage</p>
     *
     * @param bootstrapProperties
     * @param overrideProperties
     * @throws FrameworkException
     */
    public void setup(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {

        logger.info("Initialising Setup Ecosystem Service");

        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }

        framework = frameworkInitialisation.getFramework();
        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("framework");

        // Check for System Environment variables to see what we need to set in the CPS

        // DSS value

        String dss = System.getenv(DSS_ENV_VAR);
        if (dss != null && !dss.trim().isEmpty()) {
            dss = dss.trim();

            cps.setProperty("dynamicstatus.store", dss);

            logger.info("framework.dynamicstatus.store has been set to : " + dss);
        } else {
            logger.info("Not setting framework.dynamicstatus.store");
        }

        // RAS value

        String ras = System.getenv(RAS_ENV_VAR);
        if (ras != null && !ras.trim().isEmpty()) {
            ras = ras.trim();

            cps.setProperty("resultarchive.store", ras);

            logger.info("framework.resultarchive.store has been set to : " + ras);
        } else {
            logger.info("Not setting framework.resultarchive.store");
        }

        // CREDS value

        String creds = System.getenv(CREDS_ENV_VAR);
        if (creds != null && !creds.trim().isEmpty()) {
            creds = creds.trim();

            cps.setProperty("credentials.store", creds);

            logger.info("framework.credentials.store has been set to : " + creds);
        } else {
            logger.info("Not setting framework.credentials.store");
        }

        // Auth store value

        String authStore = System.getenv(AUTH_ENV_VAR);
        if (authStore != null && !authStore.trim().isEmpty()) {
            authStore = authStore.trim();

            cps.setProperty("auth.store", authStore);

            logger.info("framework.auth.store has been set to : " + authStore);
        } else {
            logger.info("Not setting framework.auth.store");
        }

        logger.info("Ending Setup Ecosystem Service");

        frameworkInitialisation.shutdownFramework();

    }

}