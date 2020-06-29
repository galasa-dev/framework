package dev.galasa.framework.spi.gherkin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunManagers;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IGherkinManager;
import dev.galasa.framework.spi.Result;

public class GherkinMethod {

    private Log logger  = LogFactory.getLog(GherkinMethod.class);

    private String name;
    private List<GherkinStatement> statements;
    private String status;
    private String testName;

    private Result result;
    private Instant startTime;
    private Instant endTime;
    private String exception;

    public GherkinMethod(String name, String testName) {
        this.name = name;
        this.statements = new ArrayList<>();
        this.testName = testName;
    }

    public void addStatement(String statement) {
        this.statements.add(new GherkinStatement(statement));
    }

    public String getName() {
        return this.name;
    }

    public List<GherkinStatement> getStatements() {
        return this.statements;
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

    public void invoke(TestRunManagers managers) throws TestRunException {
        try {
            Result ignored = managers.anyReasonGherkinTestMethodShouldBeIgnored(this);
            if (ignored != null) {
                this.result = ignored;
                return;
            }
            managers.startOfGherkinTestMethod(this);

            logger.info(GherkinTest.LOG_STARTING + GherkinTest.LOG_START_LINE + GherkinTest.LOG_ASTERS
                    + GherkinTest.LOG_START_LINE + "*** Start of test method " + this.testName + "#"
                    + this.name + GherkinTest.LOG_START_LINE
                    + GherkinTest.LOG_ASTERS);
            this.startTime = Instant.now();
            this.status = "started";

            for(GherkinStatement statement : this.statements) {
                IGherkinManager manager = statement.getRegisteredManager();
                try {
                    logger.info("Executing Statement: " + statement.toString());
                    manager.executeStatement(statement);
                } catch (ManagerException e) {
                    this.result = Result.failed(e);
                }
            }
            this.result = Result.passed();

            Result overrideResult = managers.endOfGherkinTestMethod(this, this.result, this.result.getThrowable());
            if (overrideResult != null) {
                this.result = overrideResult;
            }

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

        } catch (FrameworkException e) {
            throw new TestRunException("There was a problem with the framework, please check stacktrace", e);
        }
    }

    public boolean fullStop() {
        return this.result.isFailed();
    }

    public Result getResult() {
        return this.result;
    }
}