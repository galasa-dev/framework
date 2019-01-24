package io.ejat.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * <p>IFramework provides access to the services the Framework specifically controls, although will be provided by 
 * other OSGi bundles.  Examples being the Configuration Properties Store service,  authentication services etc.</p>
 * <p>An implement object of IFramework can be obtained using org.osgi.framework.BundleContext.getServiceReferences(IFramework.class.getName(), null)<br>
 * or<br>
 * By using the provided abstract BundleActivator class EjatBundleActivator.getFramework().</p>
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
	 * @throws ConfigurationPropertyStoreException
	 */
	@NotNull
	IConfigurationPropertyStore getConfigurationPropertyStore(@NotNull String namespace) throws ConfigurationPropertyStoreException;

}
