/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
public class RunLogRas{
   
   private IFramework framework;
   
   public RunLogRas(IFramework framework) {
      this.framework = framework;
   }
   
   public String getRunlog(String runId) throws ResultArchiveStoreException, InternalServletException {
      
      IRunResult run = null;
      String runLog = null;
      
      for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
         
         run = directoryService.getRunById(runId);
         if(run != null) {
            break;
         }
         
      }
      
      if(run != null) {
         runLog = run.getLog();
      }
      
      return runLog;
   }
   
}
