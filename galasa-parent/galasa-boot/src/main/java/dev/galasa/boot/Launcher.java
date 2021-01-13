/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019, 2020.
 */
package dev.galasa.boot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dev.galasa.boot.BootLogger.Level;
import dev.galasa.boot.felix.FelixFramework;

/**
 * Galasa command line launcher.<br>
 * 
 * Required parameters:<br>
 * &nbsp;&nbsp;<code>--obr infra.obr --obr test.obr test.bundle/test.package.TestClass</code><br>
 * 
 * <ol>
 * <li>Start Felix OSGi framework</li>
 * <li>Loads required bundles</li>
 * <li>Loads OSGi Bundle repositories from one or more <code>--obr</code>
 * parameters</li>
 * <li>Loads test bundle <code>test.bundle</code> from OBR</li>
 * <li>Loads and runs test class <code>test.package.TestClass</code></li>
 * </ol>
 *
 */
public class Launcher {

    private static final String     OBR_OPTION                = "obr";
    private static final String     BOOTSTRAP_OPTION          = "bootstrap";
    private static final String     OVERRIDES_OPTION          = "overrides";
    private static final String     RESOURCEMANAGEMENT_OPTION = "resourcemanagement";
    private static final String     K8SCONTROLLER_OPTION      = "k8scontroller";
    private static final String     API_OPTION                = "api";
    private static final String     DOCKERCONTROLLER_OPTION   = "dockercontroller";
    private static final String     METRICSERVER_OPTION       = "metricserver";
    private static final String     TEST_OPTION               = "test";
    private static final String     RUN_OPTION                = "run";
    private static final String     GHERKIN_OPTION            = "gherkin";
    private static final String     BUNDLE_OPTION             = "bundle";
    private static final String     METRICS_OPTION            = "metrics";
    private static final String     HEALTH_OPTION             = "health";
    private static final String     LOCALMAVEN_OPTION         = "localmaven";
    private static final String     REMOTEMAVEN_OPTION        = "remotemaven";
    private static final String     TRACE_OPTION              = "trace";
    private static final String     BACKUPCPS_OPTION          = "backupcps";
    private static final String     RESTORECPS_OPTION         = "restorecps";
    private static final String     FILE_OPTION               = "f";
    private static final String     FILE_OPTION_LONG          = "file";
    private static final String     DRY_RUN_OPTION    		  = "dryrun";
    


    private static final String     USER_HOME                 = "user.home";

    private static final BootLogger logger                    = new BootLogger();

    private List<String>            bundleRepositories        = new ArrayList<>();
    private String                  testName;
    private String                  testBundleName;
    private String                  testClassName;
    private String                  runName;
    private String                  gherkinName;
    private String                  filePath;

    private FelixFramework          felixFramework;

    private Properties              boostrapProperties;
    private Properties              overridesProperties;

    private boolean                 testRun;
    private boolean                 resourceManagement;
    private boolean                 k8sController;
    private boolean                 dockerController;
    private boolean                 metricsServer;
    private boolean                 api;
    private boolean                 backupCPS;
    private boolean                 restoreCPS;
    private boolean 				dryRun;

    private Integer                 metrics;
    private Integer                 health;

    private List<String>            bundles                   = new ArrayList<>();

    private URL                     localMavenRepo;
    private List<URL>               remoteMavenRepos          = new ArrayList<>();

    /**
     * Launcher main method
     */
    public static void main(String[] args) throws Exception {
        Launcher launcher = new Launcher();
        try {
            launcher.launch(args);
        } catch(LauncherException e) {
            logger.error("Exiting launcher due to exception",e);
            System.exit(16);
        }
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
                if (testBundleName != null && testClassName != null) {
                    // Run test class
                    logger.debug("Test Bundle: " + testBundleName);
                    logger.debug("Test Class: " + testClassName);
                    overridesProperties.setProperty("framework.run.testbundleclass", this.testName);
                } else if (runName != null) {
                    logger.debug("Test Run: " + runName);
                    overridesProperties.setProperty("framework.run.name", this.runName);
                } else {
                    logger.debug("Gherkin Run: " + gherkinName.toString());
                    overridesProperties.setProperty("framework.run.gherkintest", this.gherkinName);
                }

                felixFramework.runTest(boostrapProperties, overridesProperties);
            } else if (resourceManagement) {
                logger.debug("Resource Management");
                felixFramework.runResourceManagement(boostrapProperties, overridesProperties, bundles, metrics, health);
            } else if (k8sController) {
                logger.debug("Kubernetes Controller");
                felixFramework.runK8sController(boostrapProperties, overridesProperties, bundles, metrics, health);
            } else if (dockerController) {
                logger.debug("Docker Controller");
                felixFramework.runDockerController(boostrapProperties, overridesProperties, bundles, metrics, health);
            } else if (metricsServer) {
                logger.debug("Metrics Server");
                felixFramework.runMetricsServer(boostrapProperties, overridesProperties, bundles, metrics, health);
            } else if (api) {
                logger.debug("Web API Server");
                felixFramework.runWebApiServer(boostrapProperties, overridesProperties, bundles, metrics, health);
            } else if (backupCPS) {
                logger.debug("Back Up CPS Properties");
                felixFramework.runBackupCPS(boostrapProperties, overridesProperties, filePath);
            } else if (restoreCPS) {
                felixFramework.runRestoreCPS(boostrapProperties, overridesProperties, filePath, dryRun);
            }

        } catch (LauncherException e) {
            logger.error("Unable to run test class", e);
            throw e;
        } finally {
            if (felixFramework != null) {
                felixFramework.stopFramework();
            }
        }

        logger.info("Boot complete");
    }

    private void buildFramework() throws LauncherException {
        logger.debug("Launching Framework...");
        try {
            felixFramework.buildFramework(bundleRepositories, this.boostrapProperties, localMavenRepo,
                    remoteMavenRepos);
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
    private void processCommandLine(String[] args) throws ParseException {

        StringBuilder messageBuffer = new StringBuilder();
        messageBuffer.append("Supplied command line arguments: ");
        for (String arg : args) {
            messageBuffer.append(arg + " ");
        }
        logger.debug(messageBuffer.toString());

        Options options = new Options();
        Option propertyOption = Option.builder().longOpt("D").argName("property=value").hasArgs().valueSeparator()
                .numberOfArgs(2).desc("use value for given properties").build();
        options.addOption(propertyOption);
        options.addOption(null, OBR_OPTION, true, "Felix OBR Repository File name");
        options.addOption(null, BOOTSTRAP_OPTION, true, "Bootstrap properties file url");
        options.addOption(null, OVERRIDES_OPTION, true, "Overrides properties file url");
        options.addOption(null, RESOURCEMANAGEMENT_OPTION, false, "A Resource Management server");
        options.addOption(null, K8SCONTROLLER_OPTION, false, "A k8s Controller server");
        options.addOption(null, API_OPTION, false, "A Web App server, list bundles to load or ALL");
        options.addOption(null, DOCKERCONTROLLER_OPTION, false, "A Docker Controller server");
        options.addOption(null, METRICSERVER_OPTION, false, "A Metrics server");
        options.addOption(null, TEST_OPTION, true, "The test to run");
        options.addOption(null, RUN_OPTION, true, "The run name");
        options.addOption(null, GHERKIN_OPTION, true, "The gherkin test to run");
        options.addOption(null, BUNDLE_OPTION, true, "Extra bundles to load");
        options.addOption(null, METRICS_OPTION, true, "The port the metrics server will open, 0 to disable");
        options.addOption(null, HEALTH_OPTION, true, "The port the health server will open, 0 to disable");
        options.addOption(null, LOCALMAVEN_OPTION, true, "The local maven repository, defaults to ~/.m2/repository");
        options.addOption(null, REMOTEMAVEN_OPTION, true, "The remote maven repositories, defaults to central");
        options.addOption(null, TRACE_OPTION, false, "Enable TRACE logging");
        options.addOption(null, BACKUPCPS_OPTION, false, "Back up CPS properties to file");
        options.addOption(null, RESTORECPS_OPTION, false, "Restore CPS properties from file");
        options.addOption(FILE_OPTION, FILE_OPTION_LONG, true, "File for data input/output");
        options.addOption(null, DRY_RUN_OPTION, false, "Perform a dry-run of the specified actions. Can be combined with \"" + FILE_OPTION_LONG + "\"");
        

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        commandLine = parser.parse(options, args);

        if (commandLine.hasOption("D")) {
            Properties commandLinePropety = commandLine.getOptionProperties("D");
            Set<Object> keys = commandLinePropety.keySet();
            for (Object key : keys) {
                System.setProperty((String) key, commandLinePropety.getProperty((String) key));
            }
        }

        if (commandLine.hasOption(TRACE_OPTION)) {
            logger.setLevel(Level.TRACE);
            System.setProperty("log4j.configuration", "trace-log4j.properties");
        }

        // *** Add any OBRs if coded
        if (commandLine.hasOption(OBR_OPTION)) {
            for (String option : commandLine.getOptionValues(OBR_OPTION)) {
                bundleRepositories.add(option);
            }
        }

        checkForBoostrap(commandLine);
        checkForOverrides(commandLine);
        checkForBundles(commandLine);
        checkForMetricsPort(commandLine);
        checkForHealthPort(commandLine);
        checkForLocalMaven(commandLine);
        checkForRemoteMaven(commandLine);

        testRun = commandLine.hasOption(TEST_OPTION) || commandLine.hasOption(RUN_OPTION) || commandLine.hasOption(GHERKIN_OPTION);
        resourceManagement = commandLine.hasOption(RESOURCEMANAGEMENT_OPTION);
        k8sController = commandLine.hasOption(K8SCONTROLLER_OPTION);
        dockerController = commandLine.hasOption(DOCKERCONTROLLER_OPTION);
        metricsServer = commandLine.hasOption(METRICSERVER_OPTION);
        api = commandLine.hasOption(API_OPTION);
        backupCPS = commandLine.hasOption(BACKUPCPS_OPTION);
        restoreCPS = commandLine.hasOption(RESTORECPS_OPTION);
        dryRun = commandLine.hasOption(DRY_RUN_OPTION);

        if (testRun) {
            runName = commandLine.getOptionValue(RUN_OPTION);
            testName = commandLine.getOptionValue(TEST_OPTION);
            gherkinName = commandLine.getOptionValue(GHERKIN_OPTION);

            if (runName != null) {
                runName = runName.toUpperCase();
            } else if (testName != null) {
                String[] bundleclass = testName.split("/");
                if (bundleclass.length != 2) {
                    commandLineError("Error: Invalid test name format");
                }

                testBundleName = bundleclass[0];
                testClassName = bundleclass[1];
                if (testBundleName.isEmpty() || testClassName.isEmpty()) {
                    commandLineError("Error: Invalid test name format");
                }
            } else if (gherkinName != null) {
                return;
            } else {
                commandLineError("Error: A valid run, java test or gherkin test must be supplied");
            }
            return;
        }

        if (resourceManagement) {
            return;
        }

        if (k8sController) {
            return;
        }

        if (dockerController) {
            return;
        }

        if (metricsServer) {
            return;
        }
        
        if (api) {
            return;
        }

        if (backupCPS || restoreCPS) {
            if (backupCPS && restoreCPS) {
                commandLineError("Cannot use options \"" + BACKUPCPS_OPTION + "\" and \"" + RESTORECPS_OPTION + "\" together.");
            } else {
                filePath = commandLine.getOptionValue(FILE_OPTION);
                if (filePath == null) {
                    if (backupCPS) { 
                        filePathError(BACKUPCPS_OPTION);
                    } else {
                        filePathError(RESTORECPS_OPTION);
                    }
                }
            }
            return;
        }
        
        if (dryRun) {
        	commandLineError(
        			"Must be combined with \"" + RESTORECPS_OPTION + "\"");
        	return;
        }

        commandLineError(
                "Error: Must select either --" + TEST_OPTION
                		+ ", --" + RUN_OPTION
                		+ ", --" + GHERKIN_OPTION
                		+ ", --" + K8SCONTROLLER_OPTION
                		+ ", --" + METRICSERVER_OPTION
                		+ ", --" + RESOURCEMANAGEMENT_OPTION
                		+ ", --" + BUNDLE_OPTION
                		+ ", --" + BACKUPCPS_OPTION
                		+ ", or --" + RESTORECPS_OPTION);
    }
    
    private void filePathError(String option) {

        commandLineError("The option \"" + option + "\" requires an output file (specify with --" + FILE_OPTION_LONG + " <path> or -" + FILE_OPTION + " <path>)");

    }

    private void checkForRemoteMaven(CommandLine commandLine) {

        try {
            if (commandLine.hasOption(REMOTEMAVEN_OPTION)) {
                for (String option : commandLine.getOptionValues(REMOTEMAVEN_OPTION)) {
                    this.remoteMavenRepos.add(new URL(option.replaceFirst("/*$", "")));
                }
            }
            this.remoteMavenRepos.add(new URL("https://repo.maven.apache.org/maven2"));
        } catch (MalformedURLException e) {
            logger.error("invalid remote maven urls", e);
            commandLineError(null);
        }

    }

    private void checkForLocalMaven(CommandLine commandLine) {
        if (commandLine.hasOption(LOCALMAVEN_OPTION)) {
            String repo = commandLine.getOptionValue(LOCALMAVEN_OPTION);
            if (repo != null) { // *** Allowed with no parameter so that we can disable the local repository
                if ("disable".equals(repo)) {
                    this.localMavenRepo = null;
                } else {
                    try {
                        this.localMavenRepo = new URL(repo.replaceFirst("/*$", ""));
                    } catch (MalformedURLException e) {
                        logger.error("--localmaven has an invalid URL", e);
                        commandLineError(null);
                    }
                    if (!"file".equals(this.localMavenRepo.getProtocol())) {
                        commandLineError("--localmaven must be a file: URL");
                    }
                }
            }
        } else {
            try {
                this.localMavenRepo = new File(
                        System.getProperty(USER_HOME) + File.separator + ".m2" + File.separator + "repository").toURI()
                                .toURL();
            } catch (MalformedURLException e) {
                logger.error("internal error", e);
                commandLineError(null);
            }
        }
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
     * Retrieve the bootstrap URI from the command line and load the properties
     * file. If the the option is not provided, the default is
     * ~/.galasa/boostrap.properties which will be created if it does not exist.
     * 
     * @param commandLine - The command line instance
     */
    private void checkForBoostrap(CommandLine commandLine) {
        URI bootstrapUri = null;
        if (commandLine.hasOption(BOOTSTRAP_OPTION)) {
            String uri = commandLine.getOptionValue(BOOTSTRAP_OPTION);
            try {
                bootstrapUri = new URI(uri);
            } catch (URISyntaxException e) {
                logger.error("Invalid bootstrap URI provided", e);
                commandLineError(null);
            }
        } else {
            Path path = Paths.get(System.getProperty(USER_HOME), ".galasa", "bootstrap.properties");
            try {
                if (!path.toFile().exists()) {
                    if (!path.getParent().toFile().exists()) {
                        Files.createDirectories(path.getParent());
                    }
                    Files.createFile(path);
                }
                bootstrapUri = path.toUri();
            } catch (IOException e) {
                logger.error("Unable to create empty default bootstrap file " + path.toUri().toString(), e);
                commandLineError(null);
            }
        }

        try (InputStream is = bootstrapUri.toURL().openStream()) {
            boostrapProperties = new Properties();
            boostrapProperties.load(is);
        } catch (IOException e) {
            logger.error("Unable to load bootstrap properties", e);
            commandLineError(null);
        }
    }

    /**
     * Retrieve the bootsrap URI from the command line and load the properties file.
     * If the the option is not provided, the default is
     * ~/.galasa/boostrap.properties. It will not be created if it does not exist
     * 
     * @param commandLine - The command line instance
     */
    private void checkForOverrides(CommandLine commandLine) {
        URI overridesUri = null;
        if (commandLine.hasOption(OVERRIDES_OPTION)) {
            String uri = commandLine.getOptionValue(OVERRIDES_OPTION);
            try {
                overridesUri = new URI(uri);
            } catch (URISyntaxException e) {
                logger.error("Invalid overrides URI provided", e);
                commandLineError(null);
            }
        } else {
            Path path = Paths.get(System.getProperty(USER_HOME), ".galasa", "overrides.properties");
            if (!path.toFile().exists()) {
                this.overridesProperties = new Properties();
                return;
            }
            overridesUri = path.toUri();
        }

        try (InputStream is = overridesUri.toURL().openStream()) {
            overridesProperties = new Properties();
            overridesProperties.load(is);
        } catch (IOException e) {
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

        logger.error(
                "\nExample test run arguments: --obr infra.obr --obr test.obr --test test.bundle/test.package.TestClass\n"
                        + "Example Resource Management arguments: --obr infra.obr --obr test.obr --resourcemanagement");
        System.exit(-1);
    }
}
