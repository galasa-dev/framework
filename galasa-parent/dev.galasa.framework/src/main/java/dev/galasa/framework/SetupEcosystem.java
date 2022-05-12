/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.FrameworkException;
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
     * @return
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
                
        logger.info("Ending Setup Ecosystem Service");
        
        frameworkInitialisation.shutdownFramework();
        
    }
    
}
