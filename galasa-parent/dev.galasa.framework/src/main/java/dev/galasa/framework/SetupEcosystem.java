/*
 * Copyright contributors to the Galasa project 
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
    
    private Log             logger  =  LogFactory.getLog(this.getClass());
    
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
        
        String dss = System.getenv("GALASA_EXTERNAL_DYNAMICSTATUS_STORE");
        if (dss != null && !dss.trim().isEmpty()) {
            dss = dss.trim();
            
            cps.setProperty("dynamicstatus.store", dss);
            
            logger.info("framework.dynamicstatus.store has been set to : " + dss);
        } else {
            logger.info("Not setting framework.dynamicstatus.store");
        }
        
        // RAS value
        
        String ras = System.getenv("GALASA_EXTERNAL_RESULTARCHIVE_STORE");
        if (ras != null && !ras.trim().isEmpty()) {
            ras = ras.trim();
            
            cps.setProperty("resultarchive.store", ras);
            
            logger.info("framework.resultarchive.store has been set to : " + ras);
        } else {
            logger.info("Not setting framework.resultarchive.store");
        }
        
        // CREDS value
        
        String creds = System.getenv("GALASA_EXTERNAL_CREDENTIALS_STORE");
        if (creds != null && !creds.trim().isEmpty()) {
            creds = creds.trim();
            
            cps.setProperty("credentials.store", creds);
            
            logger.info("framework.credentials.store has been set to : " + creds);
        } else {
            logger.info("Not setting framework.credentials.store");
        }
                
        logger.info("Ending Setup Ecosystem Service");
        
        frameworkInitialisation.shutdownFramework();
        
    }
    
}
