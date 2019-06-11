package ${package};

import org.apache.commons.logging.Log;

import io.ejat.Test;
import io.ejat.core.CoreManager;
import io.ejat.core.ICoreManager;
import io.ejat.core.Logger;
import io.ejat.core.RunName;

public class FastCore {
    
    @Logger
    public Log logger;
    
    @RunName
    public String runName;
    
    @CoreManager
    public ICoreManager coreManager;
    
    @Test
    public void checkLogger() throws Exception {
        if (logger == null) {
            throw new Exception("Logger field is null, should have been filled by the Core Manager");
        }
        logger.info("The Logger field has been correctly initialised");
    }

    @Test
    public void checkRunName() throws Exception {
        if (runName == null || runName.trim().isEmpty()) {
            throw new Exception("Run Name field is null, should have been filled by the Core Manager");
        }
        logger.info("The Run Name  field has been correctly initialised = '" + runName + "'");
    }

    @Test
    public void checkCoreManager() throws Exception {
        if (coreManager == null) {
            throw new Exception("Core Manager field is null, should have been filled by the Core Manager");
        }
        logger.info("The Core Manager field has been correctly initialised");
    }

}
