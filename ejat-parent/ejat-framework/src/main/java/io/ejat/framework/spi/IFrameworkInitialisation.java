package io.ejat.framework.spi;

import java.net.URI;
import java.util.List;
import io.ejat.framework.spi.creds.*;

import javax.validation.constraints.NotNull;

/**
 * <p>IFrameworkInitialisation provides access to the framework routines that should only be called during
 * test run and server initialisation.</p>  
 * 
 * @author Michael Baylis
 *
 */
public interface IFrameworkInitialisation {

	/**
	 * Retrieves the Configuration Property Store that was set in the bootstrap 
	 * 
	 * @return {@link java.net.URI}
	 */
	@NotNull
	URI getBootstrapConfigurationPropertyStore(); 
	
	
	URI getDynamicStatusStoreUri();
	
	/**
	 * Retrieves a list of Result Archive URIs that need to be initialised
	 * 
	 * @return A list of URIs describing the RASs to be activated
	 */
	@NotNull
	List<URI> getResultArchiveStoreUris();
	
	/**
	 * <p>Register the active Configuration Property StoreService.  This can only be called once per test run or service instance
	 * and will be one of the very first things done during initialisation.
	 * If a second CPS attempts register itself, {@link ConfigurationPropertyStoreException} will be thrown.</p>
	 * 
	 * @param configurationPropertyStoreService - the configuration property store service chosen to be active
	 * @throws ConfigurationPropertyStoreException - Only if a 2nd attempt to register a CPS was performed
	 */
	void registerConfigurationPropertyStoreService(@NotNull IConfigurationPropertyStoreService configurationPropertyStoreService) throws ConfigurationPropertyStoreException;
	
	/**
	 * <p>Register the active Dynamic Status Store Service.  This can only be called once per test run or service instance
	 * and will be one of the first things done during initialisation.
	 * If a second DSS attempts register itself, {@link DynamicStatusStoreException} will be thrown.</p>
	 * 
	 * @param dynamicStatusStoreService - the dynamic status store service chosen to be active
	 * @throws DynamicStatusStoreException - Only if a 2nd attempt to register a DSS was performed
	 */
	void registerDynamicStatusStoreService(@NotNull IDynamicStatusStoreService dynamicStatusStoreService) throws DynamicStatusStoreException;
	
	/**
	 * <p>Register a Result Archive Store Service.  Multiple Result Archive stores can be registered per test run or service instance
	 * and will be one of the first things done during initialisation.
	 * 
	 * @param resultArchiveStoreService - the result archive store service to be registered
	 * @throws ResultArchiveStoreException If there is a problem registering the service
	 */
	void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService) throws ResultArchiveStoreException;
	
	void registerConfidentialTextService(@NotNull IConfidentialTextService confidentialTextService) throws ConfidentialTextException;

	void registerCredentialsStore(@NotNull ICredentialsRegistration credentialsRegistration) throws CredentialsStoreException;

	/**
	 * <p>Retrieve the IFramework object.  Not all the methods will be valid during the initialisation period.
	 * Review the Framework Lifecycle to determine when parts of the Framework is initialised</p>
	 * 
	 * @return {@link io.ejat.framework.spi.IFramework}
	 */
	@NotNull
	IFramework getFramework();

}
