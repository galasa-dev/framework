package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RunResultRas {

    private IFramework framework;
   
    private final Gson gson = GalasaGsonBuilder.build();

    private static final Pattern pattern = Pattern.compile("(?!.*\\/).+");
    
    public RunResultRas(IFramework framework) {
       this.framework = framework;
    }

    public RasRunResult getRun(String url) throws UnsupportedEncodingException, ResultArchiveStoreException {
       
       
        String runId = "";

        Matcher matcher = pattern.matcher(url);
       
        String json = "";
        
        RasRunResult run = null;
        
        if (matcher.find()) {
           runId = matcher.group();
         
           String decoded = URLDecoder.decode(runId, "UTF-8");
         
           run = getRunFromFramework(decoded);
         
           //Check to see if a run came back with that id
           if(run != null) {
              return run;
           
           }
            
        }
        
        return run;
    }

    private RasRunResult getRunFromFramework(String id) throws ResultArchiveStoreException {
       
       IRunResult run = null;
       
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
           
           run = directoryService.getRunById(id);
           if(run != null) {
              break;
           }
           
        }
        
        if(run == null) {
           return null;
        }
        
       return RunResultUtility.toRunResult(run, false);
    }
    
    

}
    
