package io.ejat.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * <p>IFramework provides access to the services the Framework specifically controls, although will be provided by 
 * other OSGi bundles.  Examples being the Configuration Properties Store service,  authentication services etc.</p>
 * <p>Access to the IFramework object will be via the initialisation methods of the services and managers</p>
 * <p>There must only be 1 provider of the IFramework service and the Framework bundle will always be started before any other 
 * eJAT bundle.</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IFramework {
	
	/**
	 * <p>Retrieve the Configuration Property Store service from the framework.  This will allow you to access the 
	 * configuration properties.</p>
	 * 
	 * <p>The namespace is used to departmentalise the configuration properties to prevent managers from
	 * directly accessing another manager's properties.</p>
	 * 
	 * <p>the namespace can be alphanumeric, but no '.', cannot be an empty string and cannot be null</p>
	 * 
	 * <p>As an example,  the zOS Batch Manager would have a namespace of 'zosbatch'.  The zOS Manager with be 'zos'.</p>
	 * 
	 * @param namespace - The string used to identify the manager/service to the configuration store
	 * @return A {@link IConfigurationPropertyStore},   cannot be null
	 * @throws ConfigurationPropertyStoreException - If an invalid namespace is given
	 */
	@NotNull
	IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace) throws ConfigurationPropertyStoreException;
	
	/**
	 * <p>Retrieve the Dynamic Status Store service from the framework.  This will allow you to access the 
	 * dynamic status store.</p>
	 * 
	 * <p>The namespace is used to departmentalise the status properties to prevent managers from
	 * directly accessing another manager's properties.</p>
	 * 
	 * <p>the namespace can be alphanumeric, but no '.', cannot be an empty string and cannot be null</p>
	 * 
	 * <p>As an example,  the zOS Batch Manager would have a namespace of 'zosbatch'.  The zOS Manager with be 'zos'.</p>
	 * 
	 * @param namespace - The string used to identify the manager/service to the dynamic status store
	 * @return A {@link IDynamicStatusStore},   cannot be null
	 * @throws ConfigurationPropertyStoreException - If an invalid namespace is given
	 */
	@NotNull
	IDynamicStatusStore getDynamicStatusStore(@NotNull String namespace) throws DynamicStatusStoreException;
	
	/**
	 * <p>Retrieve the Result Archive Store from the framework.</p>
	 * 
	 * @return A {@link IResultArchiveStore},   cannot be null
	 */
	@NotNull
	IResultArchiveStore getResultArchiveStore();
	
	/**
	 * <p>Provide access to the Resource Pooling Service</p>
	 * 
	 * @return {@link IResourcePoolingService} The Resource Pooling Service
	 */
	@NotNull
	IResourcePoolingService getResourcePoolingService();
	
	
	/**
	 * <p>Provide access to the Confidential Text Service</p>
	 * 
	 * @return {@link io.ejat.IConfidentialTextService} The Confidential Text Service
	 */
	@NotNull
	IConfidentialTextService getConfidentialTextService();
	
	
	/**
	 * Retrieve the test run name.  Maybe null for non test runs
	 * 
	 * @return - The test run name
	 */
	String getTestRunName();
	
}
