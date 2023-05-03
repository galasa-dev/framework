package dev.galasa.framework.api.ras.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
public class RunLogRas{
   
   private IFramework framework;
   
   public RunLogRas(IFramework framework) {
      this.framework = framework;
   }
   
   public String getRunlog(String url) throws ResultArchiveStoreException, UnsupportedEncodingException, InternalServletException {

      String runId = "";

      String[] parts = url.split("/");
      String log = null;
      
      runId = parts[2];
     
      if(runId != null) {
        String decoded = URLDecoder.decode(runId, "UTF-8");
        log = getRunLogFromFramework(decoded);
      }
      
      return log;
  
   }
   
   public String getRunLogFromFramework(String id) throws ResultArchiveStoreException, InternalServletException {
      
      IRunResult run = null;
      String runLog = null;
      
      for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
         
         run = directoryService.getRunById(id);
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
