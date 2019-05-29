package io.ejat.boot.felix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

import io.ejat.boot.BootLogger;
import io.ejat.boot.LauncherException;

/**
 * Felix framework run test class
 */
public class FelixFramework {

	private BootLogger logger = new BootLogger();

	private boolean loadConsole = Boolean.parseBoolean(System.getProperty("jat.core.load.console", "false")); 

	private Framework framework;

	private Bundle obrBundle;

	private RepositoryAdmin repositoryAdmin;


	/**
	 * Initialise and start the Felix framework. Install required bundles and the OBRs. Install the eJAT framework bundle
	 * 
	 * @param bundleRepositories the supplied OBRs
	 * @param localMavenRepo 
	 * @param remoteMavenRepos 
	 * @throws LauncherException if there is a problem initialising the framework
	 */
	public void buildFramework(List<String> bundleRepositories, Properties boostrapProperties, URL localMavenRepo, List<URL> remoteMavenRepos) throws LauncherException {
		logger.debug("Building Felix Framework...");

		String felixCacheDirectory = System.getProperty("java.io.tmpdir");
		File felixCache = new File(felixCacheDirectory, "felix-cache");
		try {
			FileUtils.deleteDirectory(felixCache);

			FrameworkFactory frameworkFactory = new FrameworkFactory();

			HashMap<String, Object> frameworkProperties = new HashMap<>();
			//			frameworkProperties.put("felix.log.level", "4");
			//			frameworkProperties.put("ds.showtrace", "true");
			frameworkProperties.put(Constants.FRAMEWORK_STORAGE, felixCache.getAbsolutePath());
			frameworkProperties.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.	FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
			frameworkProperties.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "org.apache.felix.bundlerepository; version=2.1, io.ejat.framework, sun.misc, com.sun.net.httpserver, com.sun.management, org.xml.sax" 
					);
			framework = frameworkFactory.newFramework(frameworkProperties);
			logger.debug("Initializing Felix Framework");
			framework.init();
			logger.debug("Starting Felix Framework");
			framework.start();
			logger.debug("Felix Framework started");

			logger.debug("Installing required OSGi bundles");
			//*** Load dependencies for the maven repo url handler 
			installBundle("org.apache.felix.scr-2.1.14.jar",true);
			installBundle("log4j-1.2.17.jar",true);
			installBundle("commons-logging-1.2.jar",true);
			installBundle("cirillo-maven-repository-spi-0.3.0-SNAPSHOT.jar", true);
			installBundle("cirillo-maven-repository-0.3.0-SNAPSHOT.jar", true);
			loadMavenRepositories(localMavenRepo, remoteMavenRepos);

			// Install and start the Felix OBR bundle
			obrBundle = installBundle("org.apache.felix.bundlerepository-2.0.2.jar", true);

			// Load the OSGi Bundle Repositories
			loadBundleRepositories(bundleRepositories);

			// Install and start the Felix OSGi console if required
			if (loadConsole) {
				loadBundle("org.apache.felix.gogo.runtime");
				loadBundle("org.apache.felix.gogo.command");
				loadBundle("org.apache.felix.gogo.shell");
			}

			// Load the ejat-framework bundle
			logger.debug("installing Framework bundle");
			loadBundle("io.ejat.framework");

			// Load extra bundles from the bootstrap
			String extraBundles = boostrapProperties.getProperty("framework.extra.bundles");
			if (extraBundles != null) {
				String[] ebs = extraBundles.split(",");
				for (String eb : ebs) {
					eb = eb.trim();
					if (!eb.isEmpty()) {
						loadBundle(eb);
					}
				}
			}
		} catch(IOException | BundleException e) {
			throw new LauncherException("Unable to initialise the Felix framework", e);
		}
	}

	private void loadMavenRepositories(URL localMavenRepo, List<URL> remoteMavenRepos) throws LauncherException {

		// Get the framework bundle
		Bundle frameWorkBundle = getBundle("dev.cirillo.maven.repository");

		// Get the io.ejat.framework.TestRunner class service
		String classString = "dev.cirillo.maven.repository.spi.IMavenRepository";
		ServiceReference<?>[] serviceReferences;
		try {
			serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, null);
		} catch (InvalidSyntaxException e) {
			throw new LauncherException("Unable to get framework service reference", e);
		}
		if (serviceReferences == null || serviceReferences.length != 1) {
			throw new LauncherException("Unable to get single reference to CirilloMavenRepository service: " + ((serviceReferences == null) ? 0: serviceReferences.length)  + " service(s) returned");
		}
		Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
		if (service == null) {
			throw new LauncherException("Unable to get CirilloMavenRepository service");
		}

		// Get the  CirilloMavenRepositoryr#setRepositories() method
		Method runTestMethod;
		try {
			runTestMethod = service.getClass().getMethod("setRepositories", URL.class, List.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new LauncherException("Unable to get Framework Maven Repository method", e);
		}

		// Invoke the setRepositories method
		try {
			runTestMethod.invoke(service, localMavenRepo, remoteMavenRepos);
		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			throw new LauncherException(e.getCause());
		}
	}

	/**
	 * Run the supplied test
	 * 
	 * @param testBundleName the test bundle name
	 * @param testClassName the test class name
	 * @param boostrapProperties the bootstrap properties
	 * @param overridesProperties the override properties
	 * @throws LauncherException 
	 */
	public void runTest(Properties boostrapProperties, Properties overridesProperties) throws LauncherException {

		// Get the framework bundle
		Bundle frameWorkBundle = getBundle("io.ejat.framework");

		// Get the io.ejat.framework.TestRunner class service
		String classString = "io.ejat.framework.TestRunner";
		String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
		ServiceReference<?>[] serviceReferences;
		try {
			serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
		} catch (InvalidSyntaxException e) {
			throw new LauncherException("Unable to get framework service reference", e);
		}
		if (serviceReferences == null || serviceReferences.length != 1) {
			throw new LauncherException("Unable to get single reference to TestRunner service: " + ((serviceReferences == null) ? 0: serviceReferences.length)  + " service(s) returned");
		}
		Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
		if (service == null) {
			throw new LauncherException("Unable to get TestRunner service");
		}

		// Get the  io.ejat.framework.TestRunner#runTest(String testBundleName, String testClassName) method
		Method runTestMethod;
		try {
			runTestMethod = service.getClass().getMethod("runTest", Properties.class, Properties.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new LauncherException("Unable to get Framework test runner method", e);
		}

		// Invoke the runTest method
		logger.debug("Invoking runTest()");
		try {
			runTestMethod.invoke(service, boostrapProperties, overridesProperties);
		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			throw new LauncherException(e.getCause());
		}

		return;
	}


	/**
	 * Run the Resource Management Server
	 * 
	 * @param boostrapProperties the bootstrap properties
	 * @param overridesProperties the override properties
	 * @param bundles 
	 * @param health 
	 * @param metrics 
	 * @throws LauncherException 
	 */
	public void runResourceManagement(Properties boostrapProperties, Properties overridesProperties, List<String> bundles, Integer metrics, Integer health) throws LauncherException {

		// Get the framework bundle
		Bundle frameWorkBundle = getBundle("io.ejat.framework");

		if (!bundles.isEmpty()) {
			//*** Load extra bundles
			for(String bundle : bundles) {
				try {
					loadBundle(bundle);
				} catch(Exception e) {
					throw new LauncherException("Failed to load extra bundle " + bundle, e);
				}
			}
		} else {
			//*** Load all bundles that have IResourceManagementProvider service
			HashSet<String> bundlesToLoad = new HashSet<>();

			for(Repository repository : repositoryAdmin.listRepositories()) {
				if (repository.getResources() != null) {
					resourceSearch:
						for(Resource resource : repository.getResources()) {
							if (resource.getCapabilities() != null) {
								for(Capability capability : resource.getCapabilities()) {
									if ("service".equals(capability.getName())) {
										Map<String, Object> properties = capability.getPropertiesAsMap();
										String services = (String)properties.get("objectClass:List<String>");
										if (services != null) {
											String[] split = services.split(",");

											for(String service : split) {
												if ("io.ejat.framework.spi.IResourceManagementProvider".equals(service)) {
													bundlesToLoad.add(resource.getSymbolicName());
													continue resourceSearch;
												}
											}
										}
									}
								}
							}
						}
				}
			}

			for(String bundle : bundlesToLoad) {
				if (!isBundleActive(bundle)) {
					loadBundle(bundle);
				}
			}
		}

		//*** Set up ports if present
		if (metrics != null) {
			overridesProperties.put("framework.resource.management.metrics.port", metrics.toString());
		}
		if (health != null) {
			overridesProperties.put("framework.resource.management.health.port", health.toString());
		}

		// Get the io.ejat.framework.TestRunner class service
		String classString = "dev.cirillo.framework.resource.management.internal.ResourceManagement";
		String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
		ServiceReference<?>[] serviceReferences;
		try {
			serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
		} catch (InvalidSyntaxException e) {
			throw new LauncherException("Unable to get framework service reference", e);
		}
		if (serviceReferences == null || serviceReferences.length != 1) {
			throw new LauncherException("Unable to get single reference to ResourceManagement service: " + ((serviceReferences == null) ? 0: serviceReferences.length)  + " service(s) returned");
		}
		Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
		if (service == null) {
			throw new LauncherException("Unable to get ResourceManagement service");
		}

		// Get the  io.ejat.framework.TestRunner#runTest(String testBundleName, String testClassName) method
		Method runTestMethod;
		try {
			runTestMethod = service.getClass().getMethod("run", Properties.class, Properties.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new LauncherException("Unable to get Framework resource management run method", e);
		}

		// Invoke the runTest method
		logger.debug("Invoking resource management run()");
		try {
			runTestMethod.invoke(service, boostrapProperties, overridesProperties);
		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			throw new LauncherException(e.getCause());
		}

	}
	/**
	 * Run the Kubernetes Controller Server
	 * 
	 * @param boostrapProperties the bootstrap properties
	 * @param overridesProperties the override properties
	 * @param bundles 
	 * @param health 
	 * @param metrics 
	 * @throws LauncherException 
	 */
	public void runK8sController(Properties boostrapProperties, Properties overridesProperties, List<String> bundles, Integer metrics, Integer health) throws LauncherException {

		// Get the framework bundle
		Bundle frameWorkBundle = getBundle("io.ejat.framework");
		loadBundle("dev.cirillo.k8s.controller");

		//*** Set up ports if present
		if (metrics != null) {
			overridesProperties.put("framework.controller.metrics.port", metrics.toString());
		}
		if (health != null) {
			overridesProperties.put("framework.controller.health.port", health.toString());
		}

		// Get the io.ejat.framework.TestRunner class service
		String classString = "dev.cirillo.k8s.controller.K8sController";
		String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
		ServiceReference<?>[] serviceReferences;
		try {
			serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
		} catch (InvalidSyntaxException e) {
			throw new LauncherException("Unable to get framework service reference", e);
		}
		if (serviceReferences == null || serviceReferences.length != 1) {
			throw new LauncherException("Unable to get single reference to K8sController service: " + ((serviceReferences == null) ? 0: serviceReferences.length)  + " service(s) returned");
		}
		Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
		if (service == null) {
			throw new LauncherException("Unable to get K8sController service");
		}

		// Get the  run method
		Method runTestMethod;
		try {
			runTestMethod = service.getClass().getMethod("run", Properties.class, Properties.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new LauncherException("Unable to get K8sController run method", e);
		}

		// Invoke the runTest method
		logger.debug("Invoking k8s controller run()");
		try {
			runTestMethod.invoke(service, boostrapProperties, overridesProperties);
		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			throw new LauncherException(e.getCause());
		}

	}


	/**
	 * Stop the Felix framework
	 * 
	 * @throws LauncherException
	 * @throws InterruptedException 
	 */
	public void stopFramework() throws LauncherException, InterruptedException {
		logger.debug("Stopping Felix framework");
		try {
			framework.stop();
		} catch (BundleException e) {
			throw new LauncherException("Unable to stop the Felix framework", e);
		}

		framework.waitForStop(30000);
		logger.debug("Felix framework stopped");
	}


	/**
	 * Load the supplied OBRs
	 * 
	 * @param bundleRepositories
	 * @throws LauncherException
	 */
	private void loadBundleRepositories(List<String> bundleRepositories) throws LauncherException {
		// Get the OBR RepositoryAdmin service and methods
		ServiceReference<?> serviceReference = obrBundle.getBundleContext().getServiceReference(RepositoryAdmin.class.getName());
		if ((serviceReference != null)) {
			repositoryAdmin = (RepositoryAdmin) obrBundle.getBundleContext().getService(serviceReference);
		} else {
			throw new LauncherException("Unable to get OBR RepositoryAdmin service");
		}

		for (String bundleRepository: bundleRepositories) {
			logger.trace("Loading OBR OSGi Bundle Repository " + bundleRepository);
			Repository repository;
			try {
				repository = repositoryAdmin.addRepository(bundleRepository);
			} catch (Exception e) {
				throw new LauncherException("Unable to load repository " + bundleRepository, e);
			}

			if (logger.isTraceEnabled()) {
				// Print repository content
				logger.trace("Loaded repository " + repository.getName() + " " + repository.getURI());
				Resource[] resources = repository.getResources();
				String sp3 = "   ";
				for (Resource resource : resources) {
					logger.trace(sp3 + resource.getId());
					logger.trace(sp3 + sp3 + resource.getURI());
					logger.trace(sp3 + sp3 + resource.getSymbolicName());
					logger.trace(sp3 + sp3 + resource.getPresentationName());
					logger.trace(sp3 + sp3 + "requirements:");
					Requirement[] requirements = resource.getRequirements();
					for (Requirement requirement : requirements) {
						logger.trace("        " + requirement.getFilter() + " optional=" + requirement.isOptional());
					}
				}
			}
		}
	}


	/**
	 * Load a bundle from the OSGi Bundle Repository
	 * 
	 * @param bundleSymbolicName
	 * @throws LauncherException 
	 */
	private void loadBundle(String bundleSymbolicName) throws LauncherException {

		logger.trace("Installing bundle " + bundleSymbolicName);
		Resolver resolver = repositoryAdmin.resolver();
		String filterString = "(symbolicname=" + bundleSymbolicName + ")";
		Resource[] resources = null;
		try {
			resources = repositoryAdmin.discoverResources(filterString);
		} catch (InvalidSyntaxException e) {
			throw new LauncherException("Unable to discover repoistory resources", e);
		}
		try {
			if (resources.length == 0) {
				throw new LauncherException("Unable to locate bundle \"" + bundleSymbolicName + "\" in OBR repository");
			}
			for (Resource resource : resources) {
				addResource(bundleSymbolicName, resolver, resource);
			}
		} catch (LauncherException e) {
			throw new LauncherException("Unable to install bundle \"" + bundleSymbolicName + "\" from OBR repository", e);
		}
	}


	/**
	 * Add the Resource to the Resolver and resolve
	 * 
	 * @param bundleSymbolicName
	 * @param resolver
	 * @param resource
	 * @throws LauncherException
	 */
	private void addResource(String bundleSymbolicName, Resolver resolver, Resource resource) throws LauncherException {
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

			if (!isBundleActive(bundleSymbolicName)) {
				throw new LauncherException("Bundle failed to install and activate");
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
			throw new LauncherException("Unable to resolve bundle " + bundleSymbolicName);
		}

	}


	/**
	 * Return the installed Bundle object for the bundle symbolic name
	 * 
	 * @param bundleSymbolicName
	 * @return The bundle object
	 * @throws LauncherException
	 */
	private Bundle getBundle(String bundleSymbolicName) throws LauncherException {
		Bundle[] bundles = framework.getBundleContext().getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().contentEquals(bundleSymbolicName)) {
				return bundle;
			}
		}

		throw new LauncherException("Unable to find bundle with Bundle-SymbolicName=" + bundleSymbolicName);
	}


	/**
	 * Install a bundle from class path and optionally start it
	 * 
	 * @param bundleJar
	 * @param start
	 * @return The installed bundle
	 * @throws BundleException 
	 * @throws LauncherException 
	 */
	private Bundle installBundle(String bundleJar, boolean start) throws LauncherException, BundleException {

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
	 * @return true or false
	 * @throws LauncherException
	 */
	private boolean isJar() throws LauncherException {
		URL resourceURL = this.getClass().getClassLoader().getResource("");
		if (resourceURL == null) {
			resourceURL = this.getClass().getResource("");
		}
		if (resourceURL == null) {
			throw new LauncherException("Unable to determine if running from a jar file");
		}
		logger.trace("isJar resource URL protocol: " + resourceURL.getProtocol());
		return Objects.equals(resourceURL.getProtocol(), "jar");
	}

	/**
	 * Is the supplied active in the OSGi framework
	 * 
	 * @param bundleSymbolicName
	 * @return true or false
	 */
	private boolean isBundleActive(String bundleSymbolicName) {
		Bundle[] bundles = framework.getBundleContext().getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().equals(bundleSymbolicName) && bundle.getState() == Bundle.ACTIVE) {
				return true;
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
		messageBuffer.append("Bundle status:");

		for (Bundle bundle : bundles) {
			String bundleId = String.valueOf(bundle.getBundleId());
			messageBuffer.append("\n").
			append(String.format("%5s", bundleId)).
			append("|").
			append(String.format("%-11s", getBundleStateLabel(bundle))).
			append("|     |").
			append(bundle.getSymbolicName()).
			append(" (").
			append(bundle.getVersion()).
			append(")");
		}

		logger.trace(messageBuffer.toString());
	}

	/**
	 * Convert bundle state to string
	 * @param bundle
	 * @return The bundle state
	 */
	private String getBundleStateLabel(Bundle bundle) {
		switch (bundle.getState()) {
		case Bundle.UNINSTALLED: return "Uninstalled";
		case Bundle.INSTALLED: return "Installed";
		case Bundle.RESOLVED: return "Resolved";
		case Bundle.STARTING: return "Starting";
		case Bundle.STOPPING: return "Stopping";
		case Bundle.ACTIVE: return "Active";
		default: return "<Unknown (" + bundle.getState() + ")>";
		}
	}

}
