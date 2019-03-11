package io.ejat.boot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.maven.plugins.annotations.Mojo;

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
@Mojo(name = "Launcher")
public class Launcher {

	private static final Logger logger = LogManager.getLogger(Launcher.class);
	
	private final String logLevel = System.getProperty("log.level");

	private List<String> bundleRepositories = new ArrayList<>();
	private String testBundleName;
	private String testClassName;

	private FelixFramework felixFramework;
	
	
	/**
	 * Launcher main method
	 */
	public static void main(String[] args) throws Exception
	{
		Launcher launcher = new Launcher();
		System.exit((launcher.launch(args)? 0 : -1));
	}


	/**
	 * Process supplied command line arguments and run the test
	 * 
	 * @param args test run parameters
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	protected boolean launch(String[] args) throws LauncherException, InterruptedException {
		
		if (logLevel != null) {
			if (Level.getLevel(logLevel) == null) {
				logger.error("Invalid log level \"" + logLevel + "\" supplied. The default of \"" + logger.getLevel().name() + "\" will be used");
			} else {
				Configurator.setLevel("io.ejat", Level.getLevel(logLevel));
			}
		}
		
		felixFramework = new FelixFramework(logger);
		
		// Build Felix framework and install required bundles
    	try {
			processCommandLine(args);
		} catch (ParseException e) {
			throw new LauncherException("Unable to parse command line arguments", e);
		}
    	logger.debug("OBR Repository Files: " + bundleRepositories);
    	logger.debug("Test Bundle: " + testBundleName);
    	logger.debug("Test Class: " + testClassName);
    	logger.debug("Launching Framework...");
    	
    	boolean testPassed = false;
    	try {
    		// Build the Framework
    		buildFramework();

    		// Run test class
    		testPassed = felixFramework.runTest(testBundleName, testClassName);
    		
    	} catch (LauncherException e) {
        	logger.error("Unable run test class", e);
   		} finally {
			if (felixFramework != null) {
				felixFramework.stopFramework();
			}
   		}
    	
   		logger.info("Test run complete. Test " + (testPassed? "PASSED" : "FAILED"));
   		return testPassed;
	}


	private void buildFramework() throws LauncherException {
		logger.debug("Launching Framework...");
		try {
			felixFramework.buildFramework(bundleRepositories);
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
        logger.info(messageBuffer.toString());
		
		Options options = new Options();
		String obrOption = "obr";
		options.addOption(null, obrOption, true, "Felix OBR Repository File name");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = null;
		commandLine = parser.parse(options, args);
		
		if (commandLine.hasOption(obrOption)) {
			for (Option option : commandLine.getOptions()) {
				bundleRepositories.add(option.getValue());
			}
		} else {
			commandLineError("Error: --obr OBR Repository File(s) must be supplied");
		}
		String[] otherArgs = commandLine.getArgs();
	    if (otherArgs.length == 0 || otherArgs.length > 1) {
	    	commandLineError("Error: A single test method must be supplied");
	    }
		if (otherArgs[0].indexOf('/') == -1 || otherArgs[0].indexOf('/') != otherArgs[0].lastIndexOf('/')) {
			commandLineError("Error: Invalid test name format");
		}
		
		String[] test = otherArgs[0].split("/");
		testBundleName = test[0];
	    testClassName = test[1];

	    logger.trace("Test Bundle: " + testBundleName);
	    logger.trace("Test Class:  " + testClassName);
		
	}

	/**
	 * Issue command line options error and exit
	 */
	private void commandLineError(String message) {
		if (message != null) {
			logger.error(message);
		}
		logger.error("Example command line arguments: --obr infra.obr --obr test.obr test.bundle/test.package.TestClass");
		System.exit(-1);
	}
}