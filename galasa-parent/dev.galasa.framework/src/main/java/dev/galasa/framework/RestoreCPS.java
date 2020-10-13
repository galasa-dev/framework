/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.framework;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.FrameworkException;

@Component(service = { RestoreCPS.class })
public class RestoreCPS {
    
    private Log             logger  =  LogFactory.getLog(this.getClass());
    
    /**
     * <p>Restores CPS properties from a specified file</p>
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @param filePath
     * @return
     * @throws FrameworkException
     */
    public void restore(Properties bootstrapProperties, Properties overrideProperties, String filePath) throws FrameworkException {
        logger.debug("Message from RestoreCPS.restore(): Hello World");
    }
}