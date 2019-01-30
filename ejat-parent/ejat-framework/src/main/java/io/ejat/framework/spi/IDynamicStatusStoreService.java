package io.ejat.framework.spi;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * <p>Used by the eJAT Framework to initialise the various Dynamic Status Stores that may exist within the OSGi instance.  Only 1 DSS maybe enabled during the lifetime of 
 * a eJAT test run or server instance.</p>
 * 
 * <p>The DSS should request from the framework the URI that is defined in the DSS.  It should examine the returned URI to 
 * determine if it is this DSS that is required to be initialised.  If the DSS should be initialised, the DSS should do so 
 * and then register itself in the Framework.
 *  
 * @author Michael Baylis
 *
 */
public interface IDynamicStatusStoreService {
	
	/**
	 * <p>This method is called to selectively initialise the DSS.  If this DSS is to be initialise, 
	 * it should register the DSS with @{link {@link io.ejat.framework.spi.IFrameworkInitialisation#registerDynamicStatusStore(IDynamicStatusStore)}</p> 
	 * 
	 * <p>If there is any problem initialising the sole DSS, then an exception will be thrown that will effectively terminate the Framework</p>
	 * 
	 * @param frameworkInitialisation - Initialisation object containing access to various initialisation methods
	 * @throws DynamicStatusStoreException
	 */
	@Null
	void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws DynamicStatusStoreException;

}
