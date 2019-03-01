package io.ejat.boot.felix;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

public class FelixFramework {
	
	private Logger logger;
	
	//TODO - property name??
	private boolean loadConsole = Boolean.parseBoolean(System.getProperty("jat.core.load.console", "false")); 

	private Framework framework;

	private Bundle obrBundle;

	private RepositoryAdmin repositoryAdmin;

	public FelixFramework(Logger logger) {
		this.logger = logger;
	}
	
	
	/**
	 * Initialise and start the Felix framework. Install required bundles and the OBRs. Install the eJAT framework bundle
	 * 
	 * @param bundleRepositories the supplied OBRs
	 * @throws Exception
	 */
	public void buildFramework(List<String> bundleRepositories) throws Exception {
		logger.debug("Building Felix Framework...");
		
		String felixCacheDirectory = System.getProperty("java.io.tmpdir");
		File felixCache = new File(felixCacheDirectory , "felix-cache");
		try {
			FileUtils.deleteDirectory(felixCache);
			
			FrameworkFactory frameworkFactory = new FrameworkFactory();
			
			HashMap<String, Object> frameworkProperties = new HashMap<String, Object>();
			frameworkProperties.put(Constants.FRAMEWORK_STORAGE, felixCache.getAbsolutePath());
			frameworkProperties.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.	FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
			frameworkProperties.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "org.apache.felix.bundlerepository; version=2.1, io.ejat.framework");
			framework = frameworkFactory.newFramework(frameworkProperties);
			logger.debug("Initializing Felix Framework");
			framework.init();
			logger.debug("Starting Felix Framework");
			framework.start();
			logger.debug("Felix Framework started");
			
			logger.debug("Installing required OSGi bundles");
			
			// Install and start the Felix OBR bundle
			obrBundle = installBundle("org.apache.felix.bundlerepository-2.0.2.jar", true);

			// Install and start the log4j bundles
			installBundle("log4j-api-2.11.2.jar", true);
			installBundle("log4j-core-2.11.2.jar", true);

			// Load the OSGi Bundle Repositories
			loadBundleRepositories(bundleRepositories);

			// Install and start the Felix OSGi console if required
			if (loadConsole) {
				loadBundle("org.apache.felix.gogo.runtime");
				loadBundle("org.apache.felix.gogo.command");
				loadBundle("org.apache.felix.gogo.shell");
			}

			// Load the OSGi Service Component Runtime bundle 
			loadBundle("org.apache.felix.scr");

			// Load the ejat-framework bundle
			loadBundle("io.ejat.framework");
			
		} catch(Exception e) {
			throw new Exception("Unable to initialise the Felix framework", e);
		}
	}

	/**
	 * Run the supplied test
	 * 
	 * @param testBundleName
	 * @param testClassName
	 * @return
	 * @throws Throwable
	 */
	public boolean runTest(String testBundleName, String testClassName) throws Throwable {
		
		boolean testPassed = false;
		
		// Load the test bundle
		loadBundle(testBundleName);
		
		// Get the framework bundle
		Bundle frameWorkBundle = getBundle("io.ejat.framework");
		
		// Get the io.ejat.framework.TestRunner class service
		String classString = "io.ejat.framework.TestRunner";
		String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
		ServiceReference<?>[] serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
		if (serviceReferences == null || serviceReferences.length != 1) {
			throw new Exception("Unable to get single reference to TestRunner service: " + ((serviceReferences == null) ? 0: serviceReferences.length)  + " service(s) returned");
		}
		Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
		if (service == null) {
			throw new Exception("Unable to get TestRunner service");
		}
		
		// Get the  io.ejat.framework.TestRunner#runTest(String testBundleName, String testClassName) method
		Method runTestMethod;
		try {
			runTestMethod = service.getClass().getMethod("runTest", String.class, String.class);
		} catch (Exception e) {
			throw new Exception("Unable to get Framework test runner method", e);
		}
		
		// Invoke the runTest method
    	try {
			testPassed = (boolean) runTestMethod.invoke(service, testBundleName, testClassName);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
    	
    	return testPassed;
	}

	/**
	 * Return the installed Bundle object for a bundle symbolic name
	 * 
	 * @param bundleSymbolicName
	 * @return
	 * @throws Exception
	 */
	private Bundle getBundle(String bundleSymbolicName) throws Exception {
		Bundle[] bundles = framework.getBundleContext().getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().contentEquals(bundleSymbolicName)) {
				return bundle;
			}
		}
		
		throw new Exception("Unable to find bundle with Bundle-SymbolicName=" + bundleSymbolicName);
	}


	/**
	 * Stop the Felix framework
	 * 
	 * @throws Exception
	 */
	public void stopFramework() throws Exception {
		logger.debug("Stopping Felix framework");
		FrameworkEvent event = null;
		try {
			framework.stop();
			event = framework.waitForStop(30000);
			logger.debug("Felix framework stopped");
		} catch (BundleException e) {
			throw new Exception("Unable to stop the Felix framework" + (event != null? " " +event.getType(): ""), e);
		}
	}


	/**
	 * Load a bundle from the OSGi Bundle Repository
	 * 
	 * @param bundleSymbolicName
	 * @throws Exception
	 */
	private void loadBundle(String bundleSymbolicName) throws Exception {
		
		logger.trace("Installing bundle " + bundleSymbolicName);
		try {
			Resolver resolver = repositoryAdmin.resolver();
			String filterString = "(symbolicname=" + bundleSymbolicName + ")";
			Resource[] resources = repositoryAdmin.discoverResources(filterString);
			if (resources.length == 0) {
				throw new Exception("Unable to locate bundle \"" + bundleSymbolicName + "\" in OBR repository");
			}
			for (Resource resource : resources) {
				logger.trace("Resouce: " + resource);
				resolver.add(resource);
				if (resolver.resolve())
				{
					if (logger.isTraceEnabled()) {
						Resource[] requiredResources = resolver.getRequiredResources();
						for (Resource requiredResource : requiredResources) {
							logger.trace("  RequiredResource: " + requiredResource.getSymbolicName());
						}
						Resource[] optionalResources = resolver.getOptionalResources();
						for (Resource optionalResource : optionalResources) {
							logger.trace("  OptionalResource: " + optionalResource.getSymbolicName());
						}
					}
				    
					resolver.deploy(Resolver.START);
				    
				    if (!bundleIsActive(bundleSymbolicName)) {
				    	throw new Exception("Bundle failed to install and activate");
				    }
				    
				    printBundles();
				}
				else
				{
				    Reason[] unsatisfiedRequirements = resolver.getUnsatisfiedRequirements();
				    for (Reason reason : unsatisfiedRequirements)
				    {
				    	logger.error(resource.toString() + ": Unable to resolve: " + reason.getRequirement());
				    }
					throw new Exception("Unable to resolve bundle \"" + bundleSymbolicName);
				}
			}
		} catch (Exception e) {
			throw new Exception("Unable to install bundle \"" + bundleSymbolicName + "\" from OBR repository", e);
		}
	}


	/**
	 * Load the supplied OBRs
	 * 
	 * @param bundleRepositories
	 * @throws Exception
	 */
	private void loadBundleRepositories(List<String> bundleRepositories) throws Exception {
		// Get the OBR RepositoryAdmin service and methods
		ServiceReference<?> serviceReference = obrBundle.getBundleContext().getServiceReference(RepositoryAdmin.class.getName());
    	if ((serviceReference != null)) {
    		repositoryAdmin = (RepositoryAdmin) obrBundle.getBundleContext().getService(serviceReference);
    	} else {
    		throw new Exception("Unable to get OBR RepositoryAdmin service");
    	}

		for (String bundleRepository: bundleRepositories) {
			logger.trace("Loading OBR OSGi Bundle Repository " + bundleRepository);
			Repository repository = repositoryAdmin.addRepository(bundleRepository);
			
			if (logger.isTraceEnabled()) {
				// Print repository content
				logger.trace("Loaded repository " + repository.getName() + " " + repository.getURI());
				Resource[] resources = repository.getResources();
				for (Resource resource : resources) {
					logger.trace("  " + resource.getId());
					logger.trace("      " + resource.getURI());
					logger.trace("      " + resource.getSymbolicName());
					logger.trace("      " + resource.getPresentationName());
					logger.trace("      requirements:");
					Requirement[] requirements = resource.getRequirements();
					for (Requirement requirement : requirements) {
						logger.trace("        " + requirement.getFilter() + " optional=" + requirement.isOptional());
					}
				}
			}
		}
	}


	/**
	 * Install a bundle from class path and optionally start it
	 * 
	 * @param bundleJar
	 * @param start
	 * @return
	 * @throws Exception
	 */
	private Bundle installBundle(String bundleJar, boolean start) throws Exception {
		
		String bundleLocation = null;
		// Bundle location different when running from jar or IDE
		if(isJar()) {
			bundleLocation = this.getClass().getClassLoader().getResource("bundle/" + bundleJar).toExternalForm();
		} else {
			bundleLocation = this.getClass().getClassLoader().getResource("").toString() + "../bundle/" + bundleJar;	
		}
		logger.trace("bundleLocation: " + bundleLocation);
		Bundle bundle = framework.getBundleContext().installBundle(bundleLocation);
		if (start) {
			bundle.start();
		}
		
		printBundles();
		
		return bundle;
	}


	/**
	 * Determine if this class is running from a jar file
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean isJar() throws Exception {
		URL resourceURL = this.getClass().getClassLoader().getResource("");
		if (resourceURL == null) {
			resourceURL = this.getClass().getResource("");
		}
		if (resourceURL == null) {
			throw new Exception("Unable to determine if running from a jar file");
		}
		logger.trace("isJar resource URL protocol: " + resourceURL.getProtocol());
		return Objects.equals(resourceURL.getProtocol(), "jar");
	}

	/**
	 * Is the supplied active in the OSGi framework
	 * 
	 * @param bundleSymbolicName
	 * @return
	 */
	private boolean bundleIsActive(String bundleSymbolicName) {
		Bundle[] bundles = framework.getBundleContext().getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().equals(bundleSymbolicName)) {
				if (bundle.getState() != Bundle.ACTIVE) {
					return false;
				} else {
					return true;
				}
			}
		}
		
		return false;
	}


	/** 
	 * Print the currently installed bundles and their state 
	 */
	private void printBundles() {
		if (!logger.isTraceEnabled()) {
			return;
		}
		// Get the bundles
		Bundle[] bundles = framework.getBundleContext().getBundles();
		// Format and print the bundle Id, State, Symbolic name and Version.
		StringBuilder messageBuffer = new StringBuilder(2048);
		int idLength = 8;
		int stateLength = 13;
		String spaces = "                    ";
		messageBuffer.append("Bundle status:\n").
		  append(("id" + spaces).substring(0,idLength)).
		  append(("State" + spaces).substring(0,stateLength)).
		  append("Bundle");
		
		for (Bundle bundle : bundles) {
			String bundleId = String.valueOf(bundle.getBundleId());
			messageBuffer.append(("\n" + bundleId + spaces).substring(0,idLength)).
			  append((getBundleStateLabel(bundle) + spaces).substring(0,stateLength)).
			  append(bundle.getSymbolicName()).
			  append("_").
			  append(bundle.getVersion()).
			  append(" ");
		}
		
		logger.trace(messageBuffer.toString());
	}
	
	/**
	 * Convert bundle state to string
	 * @param bundle
	 * @return
	 */
    private String getBundleStateLabel(Bundle bundle) {
    	switch (bundle.getState()) {
    		case Bundle.UNINSTALLED: return "Uninstalled";
    		case Bundle.INSTALLED: return "Installed";
    		case Bundle.RESOLVED: return "Resolved";
    		case Bundle.STARTING:
    			// Check if it has lazy activation
    			if (isLazyActivationPolicy(bundle)) {
    				return "<<Lazy>>";
    			}
    			return "Starting";
    		case Bundle.STOPPING: return "Stopping";
    		case Bundle.ACTIVE: return "Active";
    		default: return "<Unknown (" + bundle.getState() + ")>";
        }
    }
    
    /**
     * Is the bundle <code>Bundle-ActivationPolicy: lazy</code>
     * @param bundle
     * @return
     */
    private boolean isLazyActivationPolicy(Bundle bundle) {
        // Get the bundle manifest headers
        Dictionary<String, String> bundleHeaders = bundle.getHeaders();
        // Get the activation policy property and check if it's lazy
        String bundleActivationPolicy = bundleHeaders.get(org.osgi.framework.Constants.BUNDLE_ACTIVATIONPOLICY);
        boolean lazyActivationPolicy = false;
        if (bundleActivationPolicy != null && bundleActivationPolicy.equals(org.osgi.framework.Constants.ACTIVATION_LAZY)) {
            lazyActivationPolicy = true;
        }
        return lazyActivationPolicy;
    }
}
