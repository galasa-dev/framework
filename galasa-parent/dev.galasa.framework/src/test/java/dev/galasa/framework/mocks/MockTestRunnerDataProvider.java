package dev.galasa.framework.mocks;

import java.util.Properties;

import dev.galasa.framework.IAnnotationExtractor;
import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.ITestRunManagers;
import dev.galasa.framework.ITestRunnerDataProvider;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.IShutableFramework;
import dev.galasa.framework.spi.language.GalasaTest;

public class MockTestRunnerDataProvider implements ITestRunnerDataProvider {

    private IConfigurationPropertyStoreService cps;
    private IDynamicStatusStoreService         dss;
    private IResultArchiveStore                ras;
    private IRun                               run;
    private IShutableFramework                 framework;
    private Properties                         overrideProperties;

    private IAnnotationExtractor annotationExtractor;
    private IBundleManager bundleManager;
    private ITestRunManagers testRunManagers;
    private IFileSystem fileSystem;

    public MockTestRunnerDataProvider(
        IConfigurationPropertyStoreService cps,
        IDynamicStatusStoreService         dss,
        IResultArchiveStore                ras,
        IRun                               run,
        IShutableFramework                 framework,
        Properties                         overrideProperties,
        IAnnotationExtractor annotationExtractor,
        IBundleManager bundleManager,
        ITestRunManagers testRunManagers,
        IFileSystem fileSystem
    ) {
        this.cps = cps;
        this.dss = dss;
        this.ras = ras;
        this.framework = framework;
        this.run = run;
        this.overrideProperties = overrideProperties;
        this.bundleManager = bundleManager ;
        this.annotationExtractor = annotationExtractor;
        this.testRunManagers = testRunManagers ;
        this.fileSystem = fileSystem;
    }

    @Override
    public IRun getRun() {
        return run ;
    }
    @Override
    public IConfigurationPropertyStoreService getCPS() {
        return cps;
    }
    @Override
    public IDynamicStatusStoreService getDSS() {
        return dss;
    }
    @Override
    public IResultArchiveStore getRAS() {
        return ras;
    }
    @Override
    public IShutableFramework getFramework() {
        return framework;
    }
    @Override
    public IBundleManager getBundleManager() {
        return bundleManager;
    }
    @Override
    public IAnnotationExtractor getAnnotationExtractor() {
        return annotationExtractor;
    }
    @Override
    public Properties getOverrideProperties() {
        return overrideProperties;
    }

    @Override
    public ITestRunManagers createTestRunManagers(GalasaTest galasaTest) throws TestRunException {
        return testRunManagers ;
    }

    @Override
    public IFileSystem getFileSystem() {
        return this.fileSystem;
    }
}
