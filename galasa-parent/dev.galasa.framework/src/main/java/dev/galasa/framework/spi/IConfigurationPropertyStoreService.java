/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * <p>
 * Used to gain access to properties in the Configuration Property Store
 * </p>
 * 
 * <p>
 * The framework will be configured with a single Configuration Property Store
 * where all the global properties are kept. However, for test runs, an override
 * property store will also be used to provide run specific properties.
 * </p>
 * 
 * <p>
 * etcd3 is the preferred property store for Galasa
 * </p>
 * 
 * <p>
 * An {@link IConfigurationPropertyStore} can be obtained from
 * {@link IFramework#getConfigurationPropertyStore(String)}.
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public interface IConfigurationPropertyStoreService {

    /**
     * <p>
     * Retrieves a string property from the Configuration Property Store within the
     * namespace for this object.
     * </p>
     * 
     * <p>
     * getProperty will search the Override Configuration Store first per property
     * iteration and then the standard Configuration Property Store.
     * </p>
     * 
     * <p>
     * As an example, if we called getProperty("image", "credentialid", "PLEXMA",
     * "MVMA") within the zos namespace, then the following properties will be
     * searched for:-<br>
     * zos.image.PLEXMA.MVMA.credentialid in the OCPS <br>
     * zos.image.PLEXMA.MVMA.credentialid in the CPS<br>
     * zos.image.PLEXMA.credentialid in the OCPS<br>
     * zos.image.PLEXMA.credentialid in the CPS<br>
     * zos.image.credentialid in the OCPS<br>
     * zos.image.credentialid in the CPS
     * </p>
     * 
     * <p>
     * If a property is not found, null will be returned.
     * </p>
     * 
     * <p>
     * Retrieved properties and their values will be saved in the Result Archive for
     * diagnostic purposes to understand how the properties should be configured for
     * Managers
     * </p>
     * 
     * @param prefix  The prefix of the property name within the namespace.
     * @param suffix  The suffix of the property name.
     * @param infixes Any optional infixes of the property name.
     * @return
     * @throws ConfigurationPropertyStoreException
     */
    @Null
    String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException;

    /**
     * <p>
     * Retireives all possible different property variations that would be searched,
     * in the search order.
     * </p>
     * 
     * <p>
     * If a manager cant get a property, it can report all the properties you could
     * set to get a resolve the problem
     * </p>
     * 
     * @param prefix  - The prefix of the property name within the namespace.
     * @param suffix  - The suffix of the property name.
     * @param infixes - Any optional infixes of the property name.
     * @return array of property names
     */
    String[] reportPropertyVariants(@NotNull String prefix, @NotNull String suffix, String... infixes);

    /**
     * <p>
     * Retireives all possible different property variations that would be searched,
     * in the search order.
     * </p>
     * 
     * <p>
     * If a manager cant get a property, it can report all the properties you could
     * set to get a resolve the problem
     * </p>
     * 
     * @param prefix  - The prefix of the property name within the namespace.
     * @param suffix  - The suffix of the property name.
     * @param infixes - Any optional infixes of the property name.
     * @return comma separated property names
     */
    String reportPropertyVariantsString(@NotNull String prefix, @NotNull String suffix, String... infixes);

}
