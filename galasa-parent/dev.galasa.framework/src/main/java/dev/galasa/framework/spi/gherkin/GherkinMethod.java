package dev.galasa.framework.spi.gherkin;

import java.util.ArrayList;
import java.util.List;

public class GherkinMethod {

    private String name;
    private List<GherkinStatement> statements;
    private String status;
    private String testName;

    public GherkinMethod (String name, String testName) {
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

        String subPrefix = prefix + "    ";

        sb.append(prefix);
        sb.append("Test Method ");
        sb.append(testName);
        sb.append(".");
        sb.append(name);
        sb.append(", status=");
        sb.append(actualStatus);
    }
}