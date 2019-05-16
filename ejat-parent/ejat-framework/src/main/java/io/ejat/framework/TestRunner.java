package io.ejat.framework;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.ejat.framework.spi.FrameworkException;

/**
 * Run the supplied test class
 */
@Component(service={TestRunner.class})
public class TestRunner {


    private Log logger = LogFactory.getLog(TestRunner.class);

    private BundleContext bundleContext;

    @Reference
    private RepositoryAdmin repositoryAdmin;

    /**
     * Run the supplied test class
     * 
     * @param testBundleName
     * @param testClassName
     * @return
     * @throws TestRunException
     */
    public void runTest(String testBundleName, String testClassName, Properties bootstrapProperties, Properties overrideProperties) throws TestRunException  {

        //*** Initialise the framework services
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new TestRunException("Unable to initialise the Framework Services", e);
        }

        logger.info("Run test: " + testBundleName + "/" + testClassName);
        Class<?> testClass = getTestClass(testBundleName, testClassName);

        //*** Initialise the Managers ready for the test run
        TestRunManagers managers = null;
        try {
            managers = new TestRunManagers(frameworkInitialisation.getFramework(), testClass);
        } catch (FrameworkException e) {
            throw new TestRunException("Problem initialising the Managers for a test run", e);
        }

        try {
            if (managers.anyReasonTestClassShouldBeIgnored()) {
                return; //TODO handle ignored classes
            }
        } catch (FrameworkException e) {
            throw new TestRunException("Problem asking Managers for an ignore reason", e);
        }
     
        
        TestClassWrapper testClassWrapper = new TestClassWrapper(testBundleName, testClass);

        testClassWrapper.parseTestClass();

        testClassWrapper.instantiateTestClass();
        

        
        try {
            managers.provisionGenerate();
        } catch(Exception e) {
            throw new TestRunException("Unable to provision generate", e);
        }

        try {
            managers.provisionBuild();
        } catch(Exception e) {
            managers.provisionDiscard();
            throw new TestRunException("Unable to provision build", e);
        }

        try {
            managers.provisionStart();
        } catch(Exception e) {
            managers.provisionStop();
            managers.provisionDiscard();
            throw new TestRunException("Unable to provision start", e);
        }

        testClassWrapper.runTestMethods(managers);

        managers.provisionStop();
        managers.provisionDiscard();
        managers.endOfTestRun();
        
        return;
    }


    /**
     * Get the test class from the supplied bundle
     * 
     * @param testBundleName
     * @param testClassName
     * @return
     * @throws TestRunException
     */
    private Class<?> getTestClass(String testBundleName, String testClassName) throws TestRunException {
        Class<?> testClazz = null;
        Bundle[] bundles = bundleContext.getBundles();
        boolean bundleFound = false;
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(testBundleName)) {
                bundleFound = true;
                logger.trace("Found Bundle: " + testBundleName);
                try {
                    testClazz = bundle.loadClass(testClassName);
                } catch (ClassNotFoundException e) {
                    throw new TestRunException("Unable to load test class " + testClassName, e);
                }
                logger.trace("Found test class: " + testClazz.getName());

                break;
            }
        }
        if (!bundleFound) {
            throw new TestRunException("Unable to find test bundle " + testBundleName);
        }
        return testClazz;		
    }


    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }

}
