/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi.language.gherkin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.framework.IGherkinExecutable;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunManagers;
import dev.galasa.framework.spi.IGherkinManager;
import dev.galasa.framework.spi.Result;

public class GherkinMethod {

    private Log logger  = LogFactory.getLog(GherkinMethod.class);

    private String name;
    private transient List<IGherkinExecutable> executables;
    private String status;
    private String testName;

    private Result result;
    private Instant startTime;
    private Instant endTime;
    private String exception;

    public GherkinMethod(String name, String testName) {
        this.name = name;
        this.executables = new ArrayList<>();
        this.testName = testName;
    }

    public void addStatement(String statement) {
        this.executables.add(GherkinStatement.get(statement));
    }

    public String getName() {
        return this.name;
    }

    public List<IGherkinExecutable> getExecutables() {
        return this.executables;
    }

    public void report(String prefix, StringBuilder sb) {
        String actualStatus = this.status;
        if (actualStatus == null) {
            actualStatus = "Unknown";
        }

        sb.append(prefix);
        sb.append("Test Method ");
        sb.append(testName);
        sb.append(".");
        sb.append(name);
        sb.append(", status=");
        sb.append(actualStatus);
    }

    public void invoke(TestRunManagers managers, Map<String, Object> testVariables) throws TestRunException {
        //TODO Galasa standard wrapper passed to managers
        //managers.startOfTestMethod(new GalasaMethod(this));

        logger.info(GherkinTest.LOG_STARTING + GherkinTest.LOG_START_LINE + GherkinTest.LOG_ASTERS
                + GherkinTest.LOG_START_LINE + "*** Start of test method " + this.testName + "#"
                + this.name + GherkinTest.LOG_START_LINE
                + GherkinTest.LOG_ASTERS);
        this.startTime = Instant.now();
        this.status = "started";

        for(IGherkinExecutable executable : this.executables) {
            IGherkinManager manager = executable.getRegisteredManager();
            try {
                logger.info("Executing Statement: " + executable.getText());
                manager.executeGherkin(executable, testVariables);
            } catch (ManagerException e) {
                this.result = Result.failed(e);
            }
        }

        if(this.result == null) {
            this.result = Result.passed();
        }

        //TODO Galasa standard wrapper passed to managers
        // Result overrideResult = managers.endOfTestMethod(new GalasaMethod(this), this.result, this.result.getThrowable());
        // if (overrideResult != null) {
        //     this.result = overrideResult;
        // }

        if (this.result.getThrowable() != null) {
            Throwable t = this.result.getThrowable();
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                this.exception = sw.toString();
            } catch (Exception e) {
                this.exception = "Unable to report exception because of " + e.getMessage();
            }
        }

        if (this.result.isPassed()) {
            String resname = this.result.getName();
            logger.info(GherkinTest.LOG_ENDING + GherkinTest.LOG_START_LINE + GherkinTest.LOG_ASTERS
                    + GherkinTest.LOG_START_LINE + "*** " + resname + " - Test method " + this.testName
                    + "#" + this.name + GherkinTest.LOG_START_LINE
                    + GherkinTest.LOG_ASTERS);
        } else {
            String exception = "";
            if (this.exception != null) {
                exception = "\n" + this.exception;
            }
            logger.info(GherkinTest.LOG_ENDING + GherkinTest.LOG_START_LINE + GherkinTest.LOG_ASTERS
                    + GherkinTest.LOG_START_LINE + "*** " + this.result.getName() + " - Test method "
                    + this.testName + "#" + this.name
                    + GherkinTest.LOG_START_LINE + GherkinTest.LOG_ASTERS + exception);
        }

        this.endTime = Instant.now();
        this.status = "finished";
    }

    public boolean fullStop() {
        return this.result.isFailed();
    }

    public Result getResult() {
        return this.result;
    }

    public Instant getStartTime() {
        return this.startTime;
    }

    public Instant getEndTime() {
        return this.endTime;
    }
}