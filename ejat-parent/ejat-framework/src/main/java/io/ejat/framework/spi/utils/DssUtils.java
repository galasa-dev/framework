package io.ejat.framework.spi.utils;

import io.ejat.framework.spi.AbstractManager;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IDynamicStatusStoreService;

public class DssUtils {
	
	public static void incrementProperty(IDynamicStatusStoreService dss, String property) throws DynamicStatusStoreException {
		
		while(true) {
			long oldValue = 0;
			String sOldValue = AbstractManager.nulled(dss.get(property));
			if (sOldValue != null) {
				oldValue = Long.parseLong(sOldValue);
			}
			
			oldValue++;
			
			if (dss.putSwap(property, sOldValue, Long.toString(oldValue))) {
				return;
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new DynamicStatusStoreException("Swap wait interrupted", e);
			}			
		}

		
	}

}
