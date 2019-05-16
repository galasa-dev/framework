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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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

    private static final String BOOTSTRAP_OPTION = "bootstrap";
    private static final String OVERRIDES_OPTION = "overrides";

    private static final BootLogger logger = new BootLogger();

    private List<String> bundleRepositories = new ArrayList<>();
    private String testBundleName;
    private String testClassName;

    private FelixFramework felixFramework;

    private Properties boostrapProperties;
    private Properties overridesProperties;


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
        logger.debug("Test Bundle: " + testBundleName);
        logger.debug("Test Class: " + testClassName);
        logger.debug("Launching Framework...");

        try {
            // Build the Framework
            buildFramework();

            // Run test class
            felixFramework.runTest(testBundleName, testClassName, boostrapProperties, overridesProperties);

        } catch (LauncherException e) {
            logger.error("Unable run test class", e);
        } finally {
            if (felixFramework != null) {
                felixFramework.stopFramework();
            }
        }

        logger.info("Test run complete");
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

        checkForBoostrap(commandLine);
        checkForOverrides(commandLine);


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
        logger.error("Example command line arguments: --obr infra.obr --obr test.obr test.bundle/test.package.TestClass");
        System.exit(-1);
    }
}