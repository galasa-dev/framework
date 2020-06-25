package dev.galasa.framework.spi.gherkin;

import java.util.ArrayList;
import java.util.List;

public class GhrekinMethod {

    private String name;
    private List<GherkinStatement> statements;

    public GhrekinMethod (String name) {
        this.name = name;
        this.statements = new ArrayList<>();
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
    
}