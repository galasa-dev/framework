package io.ejat.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * <p>Used by the eJAT Framework to initialise the various Result Archive Stores that may exist within the OSGi instance.  The 
 * framework can run with zero or more Result Archive Stores·</p>
 * 
 * <p>The RASs should use @{link io.ejat.framework.spi.IFrameworkInitialisation#getResultArchiveStoreUris} to obtain a list of active
 * URIs which the framework wants initialised.   The RAS should examine this and determine if it is required.
 * It is up to the RAS if it wants to support multiple URIs of it's own implementation,  eg
 * file:///dir1, file:///dir2</p>
 * 
 *  
 * @author Michael Baylis
 *
 */
public interface IResultArchiveStoreService extends IResultArchiveStore {
	
	/**
	 * <p>This method is called to selectively initialise the RAS.  If this RAS is to be initialise, 
	 * it should register the RAS with {@link io.ejat.framework.spi.IFrameworkInitialisation#registerResultArchiveStoreService(IResultArchiveStoreService)}</p> 
	 * 
	 * <p>If there is any problem initialising the sole CPS, then an exception will be thrown that will effectively terminate the Framework</p>
	 * 
	 * @param frameworkInitialisation - Initialisation object containing access to various initialisation methods
	 * @throws ResultArchiveStoreException - If there is a problem initialising the underlying store
	 */
	void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws ConfigurationPropertyStoreException;
}
