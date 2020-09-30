/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.framework.cps.backup;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IBackupCPS;

@Component(service = { BackupCPS.class })
public class BackupCPS implements IBackupCPS {
    
    private Log  logger  =  LogFactory.getLog(this.getClass());
    
    public void backup(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {

        logger.info("Starting Backup Service");

        // *** Initialise the framework services
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Services", e);
        }
        IFramework framework = frameworkInitialisation.getFramework();

        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("framework");
        
        logger.info("CPS accessed");
        
    }

}
