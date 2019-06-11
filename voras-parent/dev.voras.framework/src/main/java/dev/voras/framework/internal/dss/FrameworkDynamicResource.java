package dev.voras.framework.internal.dss;

import dev.voras.framework.spi.IDynamicResource;
import dev.voras.framework.spi.IDynamicStatusStore;

public class FrameworkDynamicResource extends FrameworkDynamicStoreKeyAccess implements IDynamicResource {
	
    public FrameworkDynamicResource(IDynamicStatusStore dssStore, String prefix) {
    	super(dssStore, prefix);
    }

}