/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework;

import dev.galasa.ICredentials;
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
import dev.galasa.framework.spi.*;
import dev.galasa.framework.spi.creds.ICredentialsStoreRegistration;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.osgi.framework.Bundle;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

public class TestFrameworkInitialisation {

    @Test
    public void testFrameworkCreatedDefaultPathOk() throws Exception {
        createFrameworkInit();
    }

    public FrameworkInitialisation createFrameworkInit() throws Exception {
        // Given...
        Properties bootstrapProperties = new Properties();
        Properties overrideProperties = new Properties();
        boolean isTestrun = true ;
        Log logger = new MockLog();

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("user.home","/myuser/home");

        // A fake OSGi service registry...
        Map<String,MockServiceReference<?>> services = new HashMap<String,MockServiceReference<?>>();

        Bundle bundle = null;

        // We want a framework service
        MockFramework mockFramework = new MockFramework();
        MockServiceReference<IFramework> mockFrameworkRef = new MockServiceReference<IFramework>(mockFramework, bundle );
        services.put(IFramework.class.getName(),mockFrameworkRef);

        // We want a CPS service...
        Map<String,String> cpsProperties = new HashMap<String,String>();

        cpsProperties.put("framework.run.testbundleclass","myTestBundle/myTestClass");
        
        // framework.run.name sets the run-name explicitly.
        // cpsProperties.put("framework.run.name","myRunName");

        MockCPSStore mockCPSStore = new MockCPSStore(cpsProperties);
        MockCPSRegistration mockCPSRegistration = new MockCPSRegistration(mockCPSStore);
        MockServiceReference<IConfigurationPropertyStoreRegistration> mockCPSRef = new MockServiceReference<IConfigurationPropertyStoreRegistration>(mockCPSRegistration, bundle );
        services.put(IConfigurationPropertyStoreRegistration.class.getName(),mockCPSRef);

        // We want a DSS service...
        Map<String,String> dssProps = new HashMap<String,String>();

        MockDSSStore mockDSSStore = new MockDSSStore(dssProps);
        MockDSSRegistration mockDSSRegistration = new MockDSSRegistration(mockDSSStore);
        MockServiceReference<IDynamicStatusStoreRegistration> mockDSSRef = new MockServiceReference<IDynamicStatusStoreRegistration>(mockDSSRegistration, bundle );
        services.put(IDynamicStatusStoreRegistration.class.getName(),mockDSSRef);

        MockBundleContext bundleContext = new MockBundleContext(services);

        MockFileSystem mockFileSystem = new MockFileSystem();

        // We need a RAS store service also...
        Map<String,String> rasProps = new HashMap<String,String>();
        MockRASStoreService mockRASStoreService = new MockRASStoreService(rasProps);
        MockRASRegistration mockRASRegistration = new MockRASRegistration(mockRASStoreService);
        MockServiceReference<IResultArchiveStoreRegistration> mockRASRef = new MockServiceReference<IResultArchiveStoreRegistration>(mockRASRegistration, bundle );
        services.put(IResultArchiveStoreRegistration.class.getName(),mockRASRef);

        // We need a credentials service also...
        Map<String,ICredentials> credsProps = new HashMap<String,ICredentials>();
        MockCredentialsStore mockCredentialsStore = new MockCredentialsStore(credsProps);
        MockCredentialsStoreRegistration mockCredentialsStoreRegistration = new MockCredentialsStoreRegistration(mockCredentialsStore);
        MockServiceReference<ICredentialsStoreRegistration> mockCredsRegRef = new MockServiceReference<ICredentialsStoreRegistration>(mockCredentialsStoreRegistration, bundle );
        services.put(ICredentialsStoreRegistration.class.getName(),mockCredsRegRef);

        // We need a confidential text service also...
        Map<String,String> confidentialTextProps = new HashMap<String,String>();
        MockConfidentialTextStore mockConfidentialTextStore = new MockConfidentialTextStore(confidentialTextProps);
        MockConfidentialTextStoreRegistration mockConfidentialTextStoreRegistration = new MockConfidentialTextStoreRegistration(mockConfidentialTextStore);
        MockServiceReference<IConfidentialTextServiceRegistration> mockConfidentialTextServiceRegRef = new MockServiceReference<IConfidentialTextServiceRegistration>(mockConfidentialTextStoreRegistration, bundle );
        services.put(IConfidentialTextServiceRegistration.class.getName(),mockConfidentialTextServiceRegRef);
        
        // When...
        FrameworkInitialisation frameworkInitUnderTest = new FrameworkInitialisation( 
            bootstrapProperties,  
            overrideProperties, 
            isTestrun,
            logger, 
            mockEnv,
            bundleContext,
            mockFileSystem);

        // Then...
        assertThat(mockFramework.getConfidentialTextService()).isEqualTo(mockConfidentialTextStore);
        assertThat(mockFramework.getCredentialsStore()).isEqualTo(mockCredentialsStore);
        assertThat(mockFramework.getConfidentialTextService()).isEqualTo(mockConfidentialTextStore);
        assertThat(mockFramework.getDynamicStatusStore()).isEqualTo(mockDSSStore);
        assertThat(mockFramework.getCredentialsStore()).isEqualTo(mockCredentialsStore);
        assertThat(mockFramework.getResultArchiveStore()).isEqualTo(mockRASStoreService);

        assertThat(frameworkInitUnderTest.getBootstrapConfigurationPropertyStore().getPath()).isEqualTo("/myuser/home/.galasa/cps.properties");
        assertThat(frameworkInitUnderTest.getDynamicStatusStoreUri().getPath()).isEqualTo("/myuser/home/.galasa/dss.properties");

        List<URI> rasUriList = frameworkInitUnderTest.getResultArchiveStoreUris();
        
        assertThat(rasUriList).hasSize(1);
        assertThat(rasUriList.get(0).getPath()).isEqualTo("/myuser/home/.galasa/ras");
        assertThat(frameworkInitUnderTest.getCredentialsStoreUri().getPath()).isEqualTo("/myuser/home/.galasa/credentials.properties");

        assertThat(bootstrapProperties).isEmpty();
        assertThat(overrideProperties).isEmpty();

        return frameworkInitUnderTest;
    }


    // When no framework service has been found... should be an error.
    @Test
    public void testFrameworkCreatedNoFrameworkServiceFails() throws Exception {

        // Given...
        Properties bootstrapProperties = new Properties();
        Properties overrideProperties = new Properties();
        boolean isTestrun = true ;
        Log logger = new MockLog();
        MockEnvironment mockEnv = new MockEnvironment();

        Map<String,MockServiceReference<?>> services = new HashMap<String,MockServiceReference<?>>();
        
        // Note: The framework service isn't added as a service reference ! This should cause an error.

        MockBundleContext bundleContext = new MockBundleContext(services);
        
        MockFileSystem mockFileSystem = new MockFileSystem();

        // When...
        try {
            new FrameworkInitialisation( 
                bootstrapProperties,  
                overrideProperties, 
                isTestrun,
                logger, 
                mockEnv,
                bundleContext,
                mockFileSystem);
            fail("There is no CPS service configured on purpose, there should have been an error thrown!");
        } catch( Exception ex ) {
            assertThat(ex)
                .hasMessage("The framework service is missing")
                .isInstanceOf(FrameworkException.class)
                ;
        }
    }


    // When no Cps service reference can be found
    @Test
    public void testFrameworkCreatedNoCPSServiceFails() throws Exception {

        // Given...
        Properties bootstrapProperties = new Properties();
        Properties overrideProperties = new Properties();
        boolean isTestrun = true ;
        Log logger = new MockLog();
        MockEnvironment mockEnv = new MockEnvironment();

        Map<String,MockServiceReference<?>> services = new HashMap<String,MockServiceReference<?>>();

        MockFramework mockFramework = new MockFramework();

        Bundle bundle = null;
        MockServiceReference<IFramework> mockFrameworkRef = new MockServiceReference<IFramework>(mockFramework, bundle );
        services.put(IFramework.class.getName(),mockFrameworkRef);

        // Note: The CPS framework service isn't added as a service reference !

        MockBundleContext bundleContext = new MockBundleContext(services);
        
        MockFileSystem mockFileSystem = new MockFileSystem();

        // When...
        FrameworkInitialisation frameworkInitUnderTest = null;
        try {
            frameworkInitUnderTest = new FrameworkInitialisation( 
                bootstrapProperties,  
                overrideProperties, 
                isTestrun,
                logger, 
                mockEnv,
                bundleContext,
                mockFileSystem);
            fail("There is no CPS service configured on purpose, there should have been an error thrown!");
        } catch( Exception ex ) {
            assertThat(ex)
                .hasMessage("No Configuration Property Store Services have been found")
                .isInstanceOf(FrameworkException.class)
                ;
        }
        assertThat(frameworkInitUnderTest).isNull();
    }

    @Test
    public void testLocateDynamicStatusStoreDefaultsToUserHome() throws Exception {

        // Given...

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();

        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("user.home","/myuser2/home");

        // When...
        URI uri = frameworkInit.locateDynamicStatusStore(
            logger, mockEnv, frameworkInit.getFramework().getConfigurationPropertyService("framework"), fs);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri.getPath()).isEqualTo("/myuser2/home/.galasa/dss.properties" );

        // The empty file system should now have a blank file also.
        Path expectedPath = Path.of("/myuser2","home",".galasa","dss.properties");
        assertThat(fs.exists(expectedPath)).isTrue();
    }

    @Test
    public void testLocateDynamicStatusStoreNoticesGALASA_HOME() throws Exception {

        // Given...

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
        MockEnvironment mockEnv = new MockEnvironment();
        // The user home... which should be ignored if GALASA_HOME is set.
        mockEnv.setProperty("user.home","/myuser2/home");
        // The value we expect to be used...
        mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");

        // When...
        URI uri = frameworkInit.locateDynamicStatusStore(
            logger, mockEnv, frameworkInit.getFramework().getConfigurationPropertyService("framework"), fs);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri.getPath()).isEqualTo("/myoverriddenhome/dss.properties" );

        // The empty file system should now have a blank file also.
        Path expectedPath = Path.of("/myoverriddenhome","dss.properties");
        assertThat(fs.exists(expectedPath)).isTrue();
    }

    @Test
    public void testLocateCredentialsStoreUsesUserHome() throws Exception {

        // Given...

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
        MockEnvironment mockEnv = new MockEnvironment();
        // The user home... which should be ignored if GALASA_HOME is set.
        mockEnv.setProperty("user.home","/myuser2/home");

        // When...
        URI uri = frameworkInit.locateCredentialsStore(
            logger, mockEnv, frameworkInit.getFramework().getConfigurationPropertyService("framework"), fs);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri.getPath()).isEqualTo("/myuser2/home/.galasa/credentials.properties" );

        // The empty file system should now have a blank file also.
        Path expectedPath = Path.of("/myuser2","home",".galasa","credentials.properties");
        assertThat(fs.exists(expectedPath)).isTrue();
    }

    @Test
    public void testLocateCredentialsStoreNoticesGALASA_HOME() throws Exception {

        // Given...

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
        MockEnvironment mockEnv = new MockEnvironment();
        // The user home... which should be ignored if GALASA_HOME is set.
        mockEnv.setProperty("user.home","/myuser2/home");
        // The value we expect to be used...
        mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");

        // When...
        URI uri = frameworkInit.locateCredentialsStore(
            logger, mockEnv, frameworkInit.getFramework().getConfigurationPropertyService("framework"), fs);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri.getPath()).isEqualTo("/myoverriddenhome/credentials.properties" );

        // The empty file system should now have a blank file also.
        Path expectedPath = Path.of("/myoverriddenhome","credentials.properties");
        assertThat(fs.exists(expectedPath)).isTrue();
    }

    @Test
    public void testCreatingRASUriListUsesGALASA_HOMEoverride() throws Exception {

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        MockEnvironment mockEnv = new MockEnvironment();
        // The user home... which should be ignored if GALASA_HOME is set.
        mockEnv.setProperty("user.home","/myuser2/home");
        // The value we expect to be used...
        mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");

        Map<String,String> cpsProps = new HashMap<String,String>();
        MockCPSStore mockCPS = new MockCPSStore(cpsProps);

        // When...
        List<URI> uriList = frameworkInit.createUriResultArchiveStores(mockEnv,mockCPS);
        assertThat(uriList).contains(URI.create("file:///myoverriddenhome/ras"));
    }

    @Test
    public void testCreatingRASUriListDefaultsToUserHome() throws Exception {

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        MockEnvironment mockEnv = new MockEnvironment();
        // The user home... which should be ignored if GALASA_HOME is set.
        mockEnv.setProperty("user.home","/myuser2/home");
        // GALASA_HOME is not set...
        // mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");

        Map<String,String> cpsProps = new HashMap<String,String>();
        MockCPSStore mockCPS = new MockCPSStore(cpsProps);

        // When...
        List<URI> uriList = frameworkInit.createUriResultArchiveStores(mockEnv,mockCPS);
        assertThat(uriList).contains(URI.create("file:///myuser2/home/.galasa/ras"));
    }

    @Test
    public void testLocateBootstrapDefaultsToUserHome() throws Exception {

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
        MockEnvironment mockEnv = new MockEnvironment();
        // The user home... which should be ignored if GALASA_HOME is set.
        mockEnv.setProperty("user.home","/myuser2/home");
        // GALASA_HOME is not set...
        // mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");


        Properties bootstrapProperties = new Properties();

        URI bootstrapUri = frameworkInit.locateConfigurationPropertyStore(logger,mockEnv,bootstrapProperties,fs);

        assertThat(bootstrapUri.getPath().toString()).isEqualTo("/myuser2/home/.galasa/cps.properties");
    }

    @Test
    public void testLocateBootstrapUsesGALASA_HOME() throws Exception {

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
        MockEnvironment mockEnv = new MockEnvironment();
        // The user home... which should be ignored if GALASA_HOME is set.
        mockEnv.setProperty("user.home","/myuser2/home");

        // GALASA_HOME is set... we expect it to be used.
        mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");


        Properties bootstrapProperties = new Properties();

        URI bootstrapUri = frameworkInit.locateConfigurationPropertyStore(logger,mockEnv,bootstrapProperties,fs);

        assertThat(bootstrapUri.getPath().toString()).isEqualTo("/myoverriddenhome/cps.properties");
        assertThat(fs.exists(Path.of("/myoverriddenhome/cps.properties"))).isTrue();
    }

    @Test
    public void testLocateBootstrapUsesGALASA_HOMEsystemProInPreferenceToGALASA_HOMEenvVar() throws Exception {

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
        MockEnvironment mockEnv = new MockEnvironment();
        // The user home... which should be ignored if GALASA_HOME is set.
        mockEnv.setProperty("user.home","/myuser2/home");

        // GALASA_HOME is set... we expect it to be used.
        mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");

        // Set the system env... we don't expect this one to over-ride the 
        // system property.
        mockEnv.setenv("GALASA_HOME","/myoverriddenhome2");

        Properties bootstrapProperties = new Properties();

        URI bootstrapUri = frameworkInit.locateConfigurationPropertyStore(logger,mockEnv,bootstrapProperties,fs);

        assertThat(bootstrapUri.getPath().toString()).isEqualTo("/myoverriddenhome/cps.properties");
        assertThat(fs.exists(Path.of("/myoverriddenhome/cps.properties"))).isTrue();
    }

    @Test
    public void testLocateBootstrapUsesGALASA_HOMEenvVar() throws Exception {

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
        MockEnvironment mockEnv = new MockEnvironment();
        // The user home... which should be ignored if GALASA_HOME is set.
        mockEnv.setProperty("user.home","/myuser2/home");

        // GALASA_HOME system property is not set... 
        // mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");

        // Set the system env... we still expect this one to over-ride the 
        // user home.
        mockEnv.setenv("GALASA_HOME","/myoverriddenhome");

        Properties bootstrapProperties = new Properties();

        URI bootstrapUri = frameworkInit.locateConfigurationPropertyStore(logger,mockEnv,bootstrapProperties,fs);

        assertThat(bootstrapUri.getPath().toString()).isEqualTo("/myoverriddenhome/cps.properties");
        assertThat(fs.exists(Path.of("/myoverriddenhome/cps.properties"))).isTrue();
    }

}
