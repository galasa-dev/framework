package dev.galasa.framework.internal.dss;

import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicStatusStore;

public class FrameworkDynamicResource extends FrameworkDynamicStoreKeyAccess implements IDynamicResource {
	
    public FrameworkDynamicResource(IDynamicStatusStore dssStore, String prefix) {
    	super(dssStore, prefix);
    }

}