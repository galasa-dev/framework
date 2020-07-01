/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi.language.gherkin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.IGherkinExecutable;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunManagers;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class GherkinTest {

    private Log logger = LogFactory.getLog(GherkinTest.class);

    private final static Pattern featurePattern = Pattern.compile("Feature:(.*)");
    private final static Pattern scenarioPattern = Pattern.compile("Scenario:(.*)");

    private List<GherkinMethod> methods;
    private URI gherkinUri;
    private TestStructure testStructure;
    private Result result;
    private Map<String, Object> variables;

    private String testName;
    private List<String> comments;
    
    // Logger statics
    public static final String  LOG_STARTING   = "Starting";
    public static final String  LOG_ENDING     = "Ending";
    public static final String  LOG_START_LINE = "\n" + StringUtils.repeat("-", 23) + " ";
    public static final String  LOG_ASTERS     = StringUtils.repeat("*", 100);

    public GherkinTest(IRun run, TestStructure testStructure) throws TestRunException {
        this.methods = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.variables = new HashMap<>();
        this.testStructure = testStructure;

        try {
            gherkinUri = new URI(run.getGherkin());

            if (gherkinUri.getScheme().equals("file")) {
                File gherkinFile = new File(gherkinUri);
                List<String> lines = IOUtils.readLines(new FileReader(gherkinFile));
                GherkinMethod currentMethod = null;

                for(String line : lines) {
                    line = line.trim();
                    if(line.isEmpty()) {
                        continue;
                    }

                    Matcher featureMatch = featurePattern.matcher(line);
                    if (featureMatch.matches()) {
                        this.testName = featureMatch.group(1).trim();
                        continue;
                    }
                    Matcher scenarioMatch = scenarioPattern.matcher(line);
                    if (scenarioMatch.matches()) {
                        if(currentMethod != null) {
                            methods.add(currentMethod);
                        }
                        currentMethod = new GherkinMethod(scenarioMatch.group(1).trim(), testName);
                        continue;
                    }
                    if(currentMethod != null) {
                        currentMethod.addStatement(line);
                    } else {
                        this.comments.add(line);
                    }

                }
                if(currentMethod != null) {
                    methods.add(currentMethod);
                }
                this.testStructure.setTestShortName(this.testName);
                this.testStructure.setGherkinMethods(this.methods);
            } else {
                throw new TestRunException("Gherkin URI scheme " + gherkinUri.getScheme() + "is not supported");
            }
        } catch (URISyntaxException e) {
            throw new TestRunException("Unable to parse gherkin test URI", e);
        } catch (FileNotFoundException e) {
            throw new TestRunException("Unable to find gherkin test file", e);
        } catch (IOException e) {
            throw new TestRunException("Error reading gherkin test file", e);
        }
    }

    public String getName() {
        return this.testName;
    }

    public List<GherkinMethod> getMethods() {
        return this.methods;
    }

    public List<IGherkinExecutable> getAllExecutables() {
        List<IGherkinExecutable> allExecutables = new ArrayList<>();
        for(GherkinMethod method : this.methods) {
            allExecutables.addAll(method.getExecutables());
        }
        return allExecutables;
    }

    public Boolean allMethodsRegistered() {
        Boolean allRegistered = true;
        for(IGherkinExecutable executable : getAllExecutables()) {
            if(executable.getRegisteredManager() == null) {
                allRegistered = false;
            }
        }
        return allRegistered;
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void runTestMethods(TestRunManagers managers) throws TestRunException {
        String report = this.testStructure.gherkinReport(LOG_START_LINE);
        logger.trace("Test Class structure:-" + report);

        logger.info(LOG_STARTING + LOG_START_LINE + LOG_ASTERS + LOG_START_LINE + "*** Start of feature file: "
                + this.testName + LOG_START_LINE + LOG_ASTERS);

        try {
            managers.startOfTestClass();
        } catch (FrameworkException e) {
            throw new TestRunException("Unable to inform managers of start of test class", e);
        }

        for (GherkinMethod method : this.methods) {
            method.invoke(managers, this.variables);
            if(method.fullStop()) {
                break;
            }
        }

        for (GherkinMethod method : this.methods) {
            Result methodResult = method.getResult();
            if (methodResult != null && methodResult.isFailed()) {
                this.result = Result.failed("A Test failed");
                break;
            }
        }

        if (this.result == null) {
            this.result = Result.passed();
        }

        try {
            Result newResult = managers.endOfTestClass(this.result, null); // TODO pass the class level exception
            if (newResult != null) {
                logger.info("Result of test run overridden to " + newResult);
                this.result = newResult;
            }
        } catch (FrameworkException e) {
            throw new TestRunException("Problem with end of test class", e);
        }

        // Test result
        logger.info(LOG_ENDING + LOG_START_LINE + LOG_ASTERS + LOG_START_LINE + "*** " + this.result.getName()
                + " - Test class " + this.testName +  LOG_START_LINE + LOG_ASTERS);

        this.testStructure.setResult(this.result.getName());

        try {
            managers.testClassResult(this.result, null);
        } catch (FrameworkException e) {
            throw new TestRunException("Problem with test class result", e);
        }

        String postReport = this.testStructure.gherkinReport(LOG_START_LINE);
        logger.trace("Finishing Test Class structure:-" + postReport);
    }
}