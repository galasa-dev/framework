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

@Component(service = { ValidateEcosystem.class })
public class ValidateEcosystem {
    
    private Log             logger  =  LogFactory.getLog(this.getClass());
    
    private IFramework framework;
    
    /**
     * <p>Validate the Ecosystem will work for remote access</p>
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @return
     * @throws FrameworkException
     */
    public void setup(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {
        
        logger.info("Initialising Validate Ecosystem Service");
        
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }
        
        framework = frameworkInitialisation.getFramework();
                
        logger.info("Ending Validate Ecosystem Service");
        
        frameworkInitialisation.shutdownFramework();
        
    }
    
}
