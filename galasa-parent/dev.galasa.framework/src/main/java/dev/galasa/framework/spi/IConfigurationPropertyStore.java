/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.spi;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * <p>
 * Used by the Galasa Framework to initialise the various Configuration Property
 * Stores that may exist within the OSGi instance. Only 1 CPS maybe enabled
 * during the lifetime of a Galasa test run or server instance.
 * </p>
 * 
 * <p>
 * The CPS should request from the framework the URI that is defined in the
 * bootstrap. It should examine the returned URI to determine if it is this CPS
 * that is required to be initialised. If the CPS should be initialised, the CPS
 * should do so and then register itself in the Framework.
 * </p>
 * 
 * @author Michael Baylis
 * @author Matthew Chivers
 *
 */
public interface IConfigurationPropertyStore {

    /**
     * <p>
     * Retrieve the property from the underlying configuration property store.
     * </p>
     * 
     * <p>
     * The framework will prefix with the appropriate namespace and apply the
     * infixes before calling this method
     * </p>
     * 
     * @param key - The key of the property to retrieve
     * @return - The value of the property, or null if it does not exist
     * @throws ConfigurationPropertyStoreException - If there is a problem accessing
     *                                             the underlying store
     */
    @Null
    String getProperty(@NotNull String key) throws ConfigurationPropertyStoreException;
    
    /**
     * Retrieve prefixed properties from the underlying configuration property store.
     * 
     * The framework will prefix with the appropriate namespace before calling this method
     * 
     * @param prefix - The prefix to use
     * @return A map of found properties with includes the namesapce
     * @throws ConfigurationPropertyStoreException - if there is a error in the underlying cps
     */
    @NotNull
    Map<String, String> getPrefixedProperties(@NotNull String prefix) throws ConfigurationPropertyStoreException;

    /**
     * <p>
     * Set the property from the underlying configuration property store.
     * </p>
     * 
     * <p>
     * The framework will prefix with the appropriate namespace and apply the
     * infixes before calling this method
     * </p>
     * 
     * @param key - The key of the property to retrieve
     * @param value - The value of the property to retrieve
     * @throws ConfigurationPropertyStoreException - If there is a problem accessing
     *                                             the underlying store
     */
    @Null
    void setProperty(@NotNull String key, @NotNull String value) throws ConfigurationPropertyStoreException;

    /**
     * <p>
     * Delete the property from the underlying configuration property store.
     * </p>
     * 
     * <p>
     * The framework will prefix with the appropriate namespace and apply the
     * infixes before calling this method
     * </p>
     * 
     * @param key
     * @throws ConfigurationPropertyStoreException
     */
    void deleteProperty(@NotNull String key) throws ConfigurationPropertyStoreException;
    
    /**
     * <p>
     * Retrieves all possible different properties set from a given namespace
     * </p>
     * @param namespace The namespace for which properties will be gathered.
     * @return Map of names and values of all properties
     */
    Map<String,String> getPropertiesFromNamespace(String namespace);

    /**
     * <p>
     * Return all namespaces which have properties set
     * </p>
     * 
     * @return List all namespaces with properties set
     */
    List<String> getNamespaces();

    void shutdown() throws ConfigurationPropertyStoreException;

}
