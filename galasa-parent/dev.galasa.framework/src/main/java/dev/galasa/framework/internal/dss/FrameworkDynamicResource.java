/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019, 2020.
 */
package dev.galasa.framework.internal.dss;

import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicStatusStore;

public class FrameworkDynamicResource extends FrameworkDynamicStoreKeyAccess implements IDynamicResource {

    public FrameworkDynamicResource(IDynamicStatusStore dssStore, String prefix, String namespace) {
        super(dssStore, prefix, namespace);
    }

}
