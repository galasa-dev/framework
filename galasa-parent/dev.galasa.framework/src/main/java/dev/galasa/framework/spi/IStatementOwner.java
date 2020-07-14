package dev.galasa.framework.spi;

import dev.galasa.framework.spi.language.gherkin.CustomExecutionMethod;

public interface IStatementOwner {

    public Boolean registerCustom(CustomExecutionMethod annotation, IGherkinExecutable executable);
    
}