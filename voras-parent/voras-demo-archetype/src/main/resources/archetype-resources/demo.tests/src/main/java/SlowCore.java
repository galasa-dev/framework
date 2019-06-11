package demo;

import org.apache.commons.logging.Log;

import io.ejat.Test;
import io.ejat.core.CoreManager;
import io.ejat.core.ICoreManager;
import io.ejat.core.Logger;
import io.ejat.core.RunName;

public class SlowCore {
    
    @Logger
    public Log logger;
    
    @Test
    public void delay() throws Exception {
        logger.info("Delaying for 1 minute");
        
        Thread.sleep(60000);
        
        logger.info("Delay finished");
    }

}
