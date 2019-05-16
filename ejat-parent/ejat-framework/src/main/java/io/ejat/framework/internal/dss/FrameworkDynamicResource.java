package io.ejat.framework.internal.dss;

import io.ejat.framework.spi.IDynamicResource;
import io.ejat.framework.spi.IDynamicStatusStore;

public class FrameworkDynamicResource extends FrameworkDynamicStoreKeyAccess implements IDynamicResource {
	
    public FrameworkDynamicResource(IDynamicStatusStore dssStore, String prefix) {
    	super(dssStore, prefix);
    }

}