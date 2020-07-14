/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import dev.galasa.ManagerException;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;

public interface IGherkinExecutable {

    IGherkinManager getRegisteredManager();

    void execute(Map<String, Object> testVariables) throws ManagerException;

    void registerManager(IGherkinManager manager) throws TestRunException;

    void registerExecutionMethod(Method method, Object owner) throws TestRunException;

    String getValue();

    GherkinKeyword getKeyword();

    List<String> getRegexGroups();

    void setRegexGroups(List<String> groups);
    
}