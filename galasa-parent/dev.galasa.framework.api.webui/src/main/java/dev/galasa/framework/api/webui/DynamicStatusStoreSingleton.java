package dev.galasa.framework.api.webui;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IDynamicStatusStoreService;

@Component(service = DynamicStatusStoreSingleton.class, immediate = true)
public class DynamicStatusStoreSingleton {
	
	private static DynamicStatusStoreSingleton  INSTANCE;

    private IDynamicStatusStoreService dss;
    
    @Activate
    public void activate() {
        INSTANCE = this;
    }

    @Deactivate
    public void deacivate() {
        INSTANCE = null;
    }
    
    public static IDynamicStatusStoreService dss() throws Exception {
        if (INSTANCE != null) {
            return INSTANCE.dss;
        }

        throw new Exception("Attempt to access DSS before it has been initialised");
    }

    public static void setDss(IDynamicStatusStoreService dss) throws Exception {
        if (INSTANCE != null) {
            INSTANCE.dss = dss;
            return;
        }

        throw new Exception("Attempt to set DSS before instance created");
    }

}
