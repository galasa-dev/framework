package io.ejat.framework;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Run the supplied test class
 */
@Component(service={TestRunner.class})
public class TestRunner {


	private Logger logger = LogManager.getLogger(TestRunner.class);
	
	private BundleContext bundleContext;

	@Reference
	private RepositoryAdmin repositoryAdmin;
	
	
	public boolean runTest(String testBundleName, String testClassName) throws TestRunException  {	

		logger.info("Run test: " + testBundleName + "/" + testClassName);
		Class<?> testClass = getTestClass(testBundleName, testClassName);
		
		TestClassWrapper testClassWrapper = new TestClassWrapper(testClass);
		
		testClassWrapper.parseTestClass();
		
		testClassWrapper.instantiateTestClass();
		
		return testClassWrapper.runTestMethods();
	}
	

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
