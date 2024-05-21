/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunManagers;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.teststructure.TestGherkinMethod;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class GherkinTest {

    private Log logger = LogFactory.getLog(GherkinTest.class);

    private final static Pattern featurePattern = Pattern.compile("Feature:(.*)");
    private final static Pattern scenarioPattern = Pattern.compile("Scenario:(.*)");
    private final static Pattern examplesPattern = Pattern.compile("Examples:");
    private final static Pattern scenarioOutlinePattern = Pattern.compile("Scenario Outline:(.*)");

    private List<GherkinMethod> methods;
    private URI gherkinUri;
    private TestStructure testStructure;
    private Result result;
    private GherkinVariables variables;

    private String testName;


    // Logger statics
    public static final String  LOG_STARTING   = "Starting";
    public static final String  LOG_ENDING     = "Ending";
    public static final String  LOG_START_LINE = "\n" + StringUtils.repeat("-", 23) + " ";
    public static final String  LOG_ASTERS     = StringUtils.repeat("*", 100);

    enum Section {
        SCENARIO_OUTLINE,
        FEATURE,
        SCENARIO,
        EXAMPLE,
        ;
    }

    public GherkinTest(IRun run, TestStructure testStructure) throws TestRunException {
        this(run,testStructure, new GherkinFileReader() );
    }

    protected GherkinTest(IRun run, TestStructure testStructure, IGherkinFileReader fileReader) throws TestRunException {
        this.methods = new ArrayList<>();
        this.variables = new GherkinVariables();
        this.testStructure = testStructure;

        List<String> lines = getGherkinFeatureTextLines(run,fileReader);

        parseGherkinSyntax(lines);

        List<TestGherkinMethod> structureMethods = new ArrayList<TestGherkinMethod>(this.methods.size());
        for(GherkinMethod method : this.methods) {
            structureMethods.add(method.getStructure());
        }

        this.testStructure.setGherkinMethods(structureMethods);
    }

    private void parseGherkinSyntax(List<String> lines) throws TestRunException {
        GherkinMethod currentMethod = null;
        Section currentSection = Section.FEATURE;
        boolean exampleHeaderLineProcessed = false;

        for(String line : lines) {

            line = line.trim();
            if(line.isEmpty()) {
                // Ignore blank lines.
                continue;
            }

            if (line.startsWith("#")) {
                // Ignore comment lines.
                continue;
            }

            //handle features
            Matcher featureMatch = featurePattern.matcher(line);
            if (featureMatch.matches()) {
                endSection(currentMethod, currentSection);
                currentSection = Section.FEATURE;
                this.testName = featureMatch.group(1).trim();
                continue;
            }

            //handle scenarios
            Matcher scenarioMatch = scenarioPattern.matcher(line);
            if (scenarioMatch.matches()) {
                endSection(currentMethod, currentSection);                
                currentSection = Section.SCENARIO;
                currentMethod = new GherkinMethod(scenarioMatch.group(1).trim(), testName);
                continue;
            }

            // handle scenario outlines
            Matcher scenarioOutlineMatch = scenarioOutlinePattern.matcher(line);
            if(scenarioOutlineMatch.matches()) {
                endSection(currentMethod, currentSection);                
                currentSection = Section.SCENARIO_OUTLINE;
                currentMethod = new GherkinMethod(scenarioOutlineMatch.group(1).trim(), testName);
                continue;
            }

            //handle examples
            Matcher exampleMatch = examplesPattern.matcher(line);
            if(exampleMatch.matches()) {
                if (currentSection != Section.SCENARIO_OUTLINE) {
                    throw new TestRunException("Example specified without being inside a 'Scenario Outline:'");
                }
                currentSection = Section.EXAMPLE;
                continue;
            }

            //We are not in a heading, so work out where we are and process the line
            //scenario lines
            switch(currentSection) {
                case SCENARIO:
                case SCENARIO_OUTLINE:
                    exampleHeaderLineProcessed = false;
                    currentMethod.addStatement(line);
                    break;

                case EXAMPLE:
                    if(exampleHeaderLineProcessed) {
                        // We've already processed the header line.
                        variables.processDataLine(line);
                    } else {
                        // Not yet processed the header line.
                        variables.processHeaderLine(line);
                        exampleHeaderLineProcessed = true;
                    }
                    break;

                case FEATURE:
                    exampleHeaderLineProcessed = false;
                    break;

                default:
                    // Should never be able to reach this code. But just in case someone adds to the enum list and doesn't cope with it here.
                    throw new TestRunException("Programming error. Unexpected section "+currentSection.name());
            }
        }

        // We might be in the middle of an example... so our method hasn't been added-in yet.
        endSection(currentMethod, currentSection);

        this.testStructure.setTestShortName(this.testName);
    }

    private void endSection(GherkinMethod method, Section sectionEnding) throws TestRunException {
        if (sectionEnding == Section.SCENARIO_OUTLINE) {
            // If the outline section is ending, then we know the Examples section is missing.
            throw new TestRunException("Badly formed Gherkin feature: 'Scenario Outline:' used without an 'Examples:' section.");
        }
        addMethod(method);
    }

    private void addMethod(GherkinMethod methodToAdd) {
        if(methodToAdd != null) {
            methods.add(methodToAdd);
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
            if(this.variables.getNumberOfInstances() >= 1){
                method.invoke(managers, this.variables.getVariableInstance(0));
            } else{
                method.invoke(managers, this.variables.getVariablesOriginal());
            }
            
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
            Result newResult = managers.endOfTestClass(this.result, null); 
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

        managers.testClassResult(this.result, null);

        String postReport = this.testStructure.gherkinReport(LOG_START_LINE);
        logger.trace("Finishing Test Class structure:-" + postReport);
    }


    private List<String> getGherkinFeatureTextLines(IRun run, IGherkinFileReader fileReader) throws TestRunException {

        String gherkinUriString = run.getGherkin();
        if (gherkinUriString == null) {
            throw new TestRunException("Gherkin URI is not set");
        }

        try {
            gherkinUri = new URI(gherkinUriString);
        } catch (URISyntaxException e) {
            throw new TestRunException("Unable to parse gherkin test URI", e);
        }

        String schema = gherkinUri.getScheme();
        if (schema == null) {
            throw new TestRunException("Gherkin URI " + gherkinUri + " does not contain a schema");
        }

        if (!"file".equals(schema)) {
            throw new TestRunException("Gherkin URI scheme " + schema + " is not supported");
        }

        List<String> lines =  fileReader.readLines(gherkinUri);
        
        return lines ;
    }
}