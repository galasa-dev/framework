package io.ejat.boot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import io.ejat.boot.felix.FelixFramework;

/**
 * eJAT command line launcher.<br>
 * 
 * Required parameters:<br>
 * &nbsp;&nbsp;<code>--obr infra.obr --obr test.obr test.bundle/test.package.TestClass</code><br>
 * 
 * <ol>
 * <li>Start Felix OSGi framework</li>
 * <li>Loads required bundles</li>
 * <li>Loads OSGi Bundle repositories from one or more <code>--obr</code> parameters</li>
 * <li>Loads test bundle <code>test.bundle</code> from OBR</li>
 * <li>Loads and runs test class <code>test.package.TestClass</code></li>
 * </ol> 
 *
 */
public class Launcher {

	private static final String BOOTSTRAP_OPTION          = "bootstrap";
	private static final String OVERRIDES_OPTION          = "overrides";
	private static final String RESOURCEMANAGEMENT_OPTION = "resourcemanagement";
	private static final String TESTRUN_OPTION            = "testrun";
	private static final String BUNDLE_OPTION             = "bundle";
	private static final String METRICS_OPTION            = "metrics";
	private static final String HEALTH_OPTION             = "health";

	private static final BootLogger logger = new BootLogger();

	private List<String> bundleRepositories = new ArrayList<>();
	private String testBundleName;
	private String testClassName;

	private FelixFramework felixFramework;

	private Properties boostrapProperties;
	private Properties overridesProperties;

	private boolean testRun;
	private boolean resourceManagement;
	
	private Integer metrics;
	private Integer health;

	private List<String> bundles = new ArrayList<>();    

	/**
	 * Launcher main method
	 */
	public static void main(String[] args) throws Exception
	{
		Launcher launcher = new Launcher();
		launcher.launch(args);
		System.exit(0);
	}


	/**
	 * Process supplied command line arguments and run the test
	 * 
	 * @param args test run parameters
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	protected void launch(String[] args) throws LauncherException, InterruptedException {

		felixFramework = new FelixFramework();

		// Build Felix framework and install required bundles
		try {
			processCommandLine(args);
		} catch (ParseException e) {
			throw new LauncherException("Unable to parse command line arguments", e);
		}
		logger.debug("OBR Repository Files: " + bundleRepositories);
		logger.debug("Launching Framework...");

		try {
			// Build the Framework
			buildFramework();

			if (testRun) {
				// Run test class
				logger.debug("Test Bundle: " + testBundleName);
				logger.debug("Test Class: " + testClassName);
				felixFramework.runTest(testBundleName, testClassName, boostrapProperties, overridesProperties);
			} else if (resourceManagement) {
				logger.debug("Resource Management");
				felixFramework.runResourceManagement(boostrapProperties, overridesProperties, bundles, metrics, health);
			}
		} catch (LauncherException e) {
			logger.error("Unable run test class", e);
		} finally {
			if (felixFramework != null) {
				felixFramework.stopFramework();
			}
		}

		logger.info("Boot complete");
		return;
	}


	private void buildFramework() throws LauncherException {
		logger.debug("Launching Framework...");
		try {
			felixFramework.buildFramework(bundleRepositories, testBundleName, this.boostrapProperties);
		} catch (Exception e) {
			throw new LauncherException("Unable to create and initialize Felix framework", e);
		}
	}


	/**
	 * Process the supplied command line augments 
	 * 
	 * @param args supplied arguments
	 * @throws ParseException
	 */
	private void processCommandLine(String[] args) throws ParseException  {

		StringBuilder messageBuffer = new StringBuilder();		
		messageBuffer.append("Supplied command line arguments: ");
		for (String arg : args) {
			messageBuffer.append(arg + " ");
		}
		logger.debug(messageBuffer.toString());

		Options options = new Options();
		String obrOption = "obr";
		options.addOption(null, obrOption, true, "Felix OBR Repository File name");
		options.addOption(null, BOOTSTRAP_OPTION, true, "Bootstrap properties file url");
		options.addOption(null, OVERRIDES_OPTION, true, "Overrides properties file url");
		options.addOption(null, RESOURCEMANAGEMENT_OPTION, false, "A Resource Management server");
		options.addOption(null, TESTRUN_OPTION, true, "A Test Run");
		options.addOption(null, BUNDLE_OPTION, true, "Extra bundles to load");
		options.addOption(null, METRICS_OPTION, true, "The port the metrics server will open, 0 to disable");
		options.addOption(null, HEALTH_OPTION, true, "The port the health server will open, 0 to disable");

		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = null;
		commandLine = parser.parse(options, args);

		if (commandLine.hasOption(obrOption)) {
			for (String option : commandLine.getOptionValues(obrOption)) {
				bundleRepositories.add(option);
			}
		} else {
			commandLineError("Error: --obr OBR Repository File(s) must be supplied");
		}

		checkForBoostrap(commandLine);
		checkForOverrides(commandLine);
		checkForBundles(commandLine);
		checkForMetricsPort(commandLine);
		checkForHealthPort(commandLine);

		testRun = commandLine.hasOption(TESTRUN_OPTION);
		resourceManagement = commandLine.hasOption(RESOURCEMANAGEMENT_OPTION);

		if (testRun && resourceManagement) {
			commandLineError("Error: Either select --testrun or --resourcemanagement, but not both");
		}

		if (testRun) {
			String test = commandLine.getOptionValue(TESTRUN_OPTION);
			if (test == null) {
				commandLineError("Error: A single test method must be supplied");
			}

			String[] bundleclass = test.split("/");
			if (bundleclass.length != 2) {
				commandLineError("Error: Invalid test name format");
			}

			testBundleName = bundleclass[0];
			testClassName = bundleclass[1];
			if (testBundleName.isEmpty() || testClassName.isEmpty()) {
				commandLineError("Error: Invalid test name format");
			}
			return;
		}


		if (resourceManagement) {
			return;
		}


		commandLineError("Error: Must select either --testrun or --resourcemanagement");
	}

	private void checkForMetricsPort(CommandLine commandLine) {
		if (commandLine.hasOption(METRICS_OPTION)) {
			String port = commandLine.getOptionValue(METRICS_OPTION);
			metrics = Integer.parseInt(port);
		}
	}


	private void checkForHealthPort(CommandLine commandLine) {
		if (commandLine.hasOption(HEALTH_OPTION)) {
			String port = commandLine.getOptionValue(HEALTH_OPTION);
			health = Integer.parseInt(port);
		}
	}


	private void checkForBundles(CommandLine commandLine) {
		if (commandLine.hasOption(BUNDLE_OPTION)) {
			for (String option : commandLine.getOptionValues(BUNDLE_OPTION)) {
				bundles.add(option);
			}
		}
	}


	/**
	 * Retrieve the bootstrap URI from the command line and load the properties file.
	 * If the the option is not provided, the default is ~/.ejat/boostrap.properties which will 
	 * be created if it does not exist.
	 * 
	 * @param commandLine - The command line instance
	 */
	private void checkForBoostrap(CommandLine commandLine) {
		URI bootstrapUri = null;
		if (commandLine.hasOption(BOOTSTRAP_OPTION)) {
			String uri = commandLine.getOptionValue(BOOTSTRAP_OPTION);
			try {
				bootstrapUri = new URI(uri);
			} catch(URISyntaxException e) {
				logger.error("Invalid bootstrap URI provided", e);
				commandLineError(null);
			}
		} else {
			Path path = Paths.get(System.getProperty("user.home"), ".ejat", "bootstrap.properties");
			try {
				if (!Files.exists(path)) {
					Files.createFile(path);
				}
				bootstrapUri = path.toUri();
			} catch(IOException e) {
				logger.error("Unable to create empty default bootstrap file " + path.toUri().toString(), e);
				commandLineError(null);
			}
		}

		try(InputStream is = bootstrapUri.toURL().openStream()) {
			boostrapProperties = new Properties();
			boostrapProperties.load(is);
		} catch(IOException e) {
			logger.error("Unable to load bootstrap properties", e);
			commandLineError(null);
		}
	}


	/**
	 * Retrieve the bootsrap URI from the command line and load the properties file.
	 * If the the option is not provided, the default is ~/.ejat/boostrap.properties.  It will not 
	 * be created if it does not exist
	 * 
	 * @param commandLine - The command line instance
	 */
	private void checkForOverrides(CommandLine commandLine) {
		URI overridesUri = null;
		if (commandLine.hasOption(OVERRIDES_OPTION)) {
			String uri = commandLine.getOptionValue(OVERRIDES_OPTION);
			try {
				overridesUri = new URI(uri);
			} catch(URISyntaxException e) {
				logger.error("Invalid overrides URI provided", e);
				commandLineError(null);
			}
		} else {
			Path path = Paths.get(System.getProperty("user.home"), ".ejat", "overrides.properties");
			if (!Files.exists(path)) {
				this.overridesProperties = new Properties();
				return;
			}
			overridesUri = path.toUri();
		}

		try(InputStream is = overridesUri.toURL().openStream()) {
			overridesProperties = new Properties();
			overridesProperties.load(is);
		} catch(IOException e) {
			logger.error("Unable to load overrides properties", e);
			commandLineError(null);
		}
	}


	/**
	 * Issue command line options error and exit
	 */
	private void commandLineError(String message) {
		if (message != null) {
			logger.error(message);
		}

		logger.error("\nExample test run arguments: --obr infra.obr --obr test.obr --testrun test.bundle/test.package.TestClass\n" +
				"Example Resource Management arguments: --obr infra.obr --obr test.obr --resourcemanagement");
		System.exit(-1);
	}
}