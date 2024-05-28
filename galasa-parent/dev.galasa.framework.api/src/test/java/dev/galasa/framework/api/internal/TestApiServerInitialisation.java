/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.internal;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.mocks.MockAuthStore;
import dev.galasa.framework.api.mocks.MockAuthStoreRegistration;
import dev.galasa.framework.mocks.MockBundleContext;
import dev.galasa.framework.mocks.MockCPSRegistration;
import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockConfidentialTextStore;
import dev.galasa.framework.mocks.MockConfidentialTextStoreRegistration;
import dev.galasa.framework.mocks.MockCredentialsStore;
import dev.galasa.framework.mocks.MockCredentialsStoreRegistration;
import dev.galasa.framework.mocks.MockDSSRegistration;
import dev.galasa.framework.mocks.MockDSSStore;
import dev.galasa.framework.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockLog;
import dev.galasa.framework.mocks.MockRASRegistration;
import dev.galasa.framework.mocks.MockRASStoreService;
import dev.galasa.framework.mocks.MockServiceReference;
import dev.galasa.framework.spi.IConfidentialTextServiceRegistration;
import dev.galasa.framework.spi.IConfigurationPropertyStoreRegistration;
import dev.galasa.framework.spi.IDynamicStatusStoreRegistration;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreRegistration;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreRegistration;
import dev.galasa.framework.spi.creds.ICredentialsStoreRegistration;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.osgi.framework.Bundle;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.*;

public class TestApiServerInitialisation {

    private ApiServerInitialisation createApiServerInit(Properties bootstrapProps) throws Exception {
        return createApiServerInit(bootstrapProps, new MockEnvironment());
    }

    private ApiServerInitialisation createApiServerInit(Properties bootstrapProps, MockEnvironment mockEnv)
            throws Exception {
        // Given...
        Properties bootstrapProperties = bootstrapProps;
        Properties overrideProperties = new Properties();
        Log logger = new MockLog();

        // A fake OSGi service registry...
        Map<String, MockServiceReference<?>> services = new HashMap<String, MockServiceReference<?>>();

        Bundle bundle = null;

        MockFramework mockFramework = addMockFrameworkToMockServiceRegistry(services, bundle);

        Map<String, String> cpsProperties = new HashMap<String, String>();

        addMockCPSToMockServiceRegistry(services, cpsProperties, bundle);
        addMockConfidentialTextServiceToMockServiceRegistry(services, bundle);
        addMockDSSToMockServiceRegistry(services, bundle);
        addMockRASToMockServiceRegistry(services, bundle);
        addMockCredentialsStoreToMockServiceRegistry(services, bundle);

        MockAuthStore mockAuthStore = addMockAuthStoreToMockServiceRegistry(services, bundle);

        MockBundleContext bundleContext = new MockBundleContext(services);
        MockFileSystem mockFileSystem = new MockFileSystem();

        // When...
        ApiServerInitialisation frameworkInitUnderTest = new ApiServerInitialisation(bootstrapProperties,
                overrideProperties, logger, bundleContext, mockFileSystem, mockEnv);

        // Then...
        assertThat(mockFramework.getAuthStore()).isEqualTo(mockAuthStore);

        return frameworkInitUnderTest;
    }

    private MockAuthStore addMockAuthStoreToMockServiceRegistry(Map<String, MockServiceReference<?>> services,
            Bundle bundle) {
        MockAuthStore mockAuthStore = new MockAuthStore();
        MockAuthStoreRegistration mockAuthStoreRegistration = new MockAuthStoreRegistration(mockAuthStore);
        MockServiceReference<IAuthStoreRegistration> mockAuthStoreRef = new MockServiceReference<IAuthStoreRegistration>(
                mockAuthStoreRegistration, bundle);
        services.put(IAuthStoreRegistration.class.getName(), mockAuthStoreRef);
        return mockAuthStore;
    }

    private MockFramework addMockFrameworkToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        MockFramework mockFramework = new MockFramework();
        MockServiceReference<IFramework> mockFrameworkRef = new MockServiceReference<IFramework>(mockFramework, bundle);
        services.put(IFramework.class.getName(), mockFrameworkRef);
        return mockFramework;
    }

    private void addMockCPSToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Map<String,String> cpsProperties, Bundle bundle) {
        MockCPSStore mockCPSStore = new MockCPSStore(cpsProperties);
        MockCPSRegistration mockCPSRegistration = new MockCPSRegistration(mockCPSStore);
        MockServiceReference<IConfigurationPropertyStoreRegistration> mockCPSRef = new MockServiceReference<IConfigurationPropertyStoreRegistration>(mockCPSRegistration, bundle);
        services.put(IConfigurationPropertyStoreRegistration.class.getName(), mockCPSRef);
    }

    private void addMockDSSToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        Map<String,String> dssProps = new HashMap<String,String>();
        MockDSSStore mockDSSStore = new MockDSSStore(dssProps);
        MockDSSRegistration mockDSSRegistration = new MockDSSRegistration(mockDSSStore);
        MockServiceReference<IDynamicStatusStoreRegistration> mockDSSRef = 
            new MockServiceReference<IDynamicStatusStoreRegistration>(mockDSSRegistration, bundle);
        services.put(IDynamicStatusStoreRegistration.class.getName(), mockDSSRef);
    }

    private void addMockRASToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        Map<String,String> rasProps = new HashMap<String,String>();
        MockRASStoreService mockRASStoreService = new MockRASStoreService(rasProps);
        MockRASRegistration mockRASRegistration = new MockRASRegistration(mockRASStoreService);
        MockServiceReference<IResultArchiveStoreRegistration> mockRASRef = 
            new MockServiceReference<IResultArchiveStoreRegistration>(mockRASRegistration, bundle);
        services.put(IResultArchiveStoreRegistration.class.getName(), mockRASRef);
    }

    private void addMockCredentialsStoreToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        Map<String,ICredentials> credsProps = new HashMap<String,ICredentials>();
        MockCredentialsStore mockCredentialsStore = new MockCredentialsStore(credsProps);
        MockCredentialsStoreRegistration mockCredentialsStoreRegistration = new MockCredentialsStoreRegistration(mockCredentialsStore);
        MockServiceReference<ICredentialsStoreRegistration> mockCredsRegRef = 
            new MockServiceReference<ICredentialsStoreRegistration>(mockCredentialsStoreRegistration, bundle);
        services.put(ICredentialsStoreRegistration.class.getName(),mockCredsRegRef);
    }

    private void addMockConfidentialTextServiceToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        Map<String,String> confidentialTextProps = new HashMap<String,String>();
        MockConfidentialTextStore mockConfidentialTextStore = new MockConfidentialTextStore(confidentialTextProps);
        MockConfidentialTextStoreRegistration mockConfidentialTextStoreRegistration = new MockConfidentialTextStoreRegistration(mockConfidentialTextStore);
        MockServiceReference<IConfidentialTextServiceRegistration> mockConfidentialTextServiceRegRef = 
            new MockServiceReference<IConfidentialTextServiceRegistration>(mockConfidentialTextStoreRegistration, bundle);
        services.put(IConfidentialTextServiceRegistration.class.getName(), mockConfidentialTextServiceRegRef);
    }

    @Test
    public void testLocateAuthStoreDefaultsToNull() throws Exception {

        // Given...
        Properties bootstrap = new Properties();

        // The framework.auth.store property hasn't been set, so there is no auth store to use.
        ApiServerInitialisation frameworkInit = createApiServerInit(bootstrap);

        Log logger = new MockLog();

        // When...
        URI uri = frameworkInit.locateAuthStore(logger, bootstrap);

        // Then...
        assertThat(uri).isNull();
    }

    @Test
    public void testLocateAuthStoreGetsFrameworkAuthStoreUri() throws Exception {

        // Given...
        Properties bootstrap = new Properties();

        URI authStoreUri = URI.create("couchdb:http://my-user-store");

        bootstrap.setProperty("framework.auth.store", authStoreUri.toString());
        ApiServerInitialisation frameworkInit = createApiServerInit(bootstrap);

        Log logger = new MockLog();

        // When...
        URI uri = frameworkInit.locateAuthStore(logger, bootstrap);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri).isEqualTo(authStoreUri);
    }

    @Test
    public void testInitialiseAuthStoreSetsFrameworkAuthStore() throws Exception {

        // Given...
        Properties bootstrap = new Properties();

        URI authStoreUri = URI.create("couchdb:http://my-user-store");

        bootstrap.setProperty("framework.auth.store", authStoreUri.toString());
        ApiServerInitialisation frameworkInit = createApiServerInit(bootstrap);

        // When...
        IAuthStore authStore = frameworkInit.getFramework().getAuthStore();

        // Then...
        assertThat(authStore).isNotNull();
        assertThat(authStore.getClass().getName()).isEqualTo(MockAuthStore.class.getName());
    }
}