/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.CustomExecutionMethod;
import dev.galasa.framework.spi.language.gherkin.ExecutionMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinTest;

public abstract class AbstractGherkinManager extends AbstractManager implements IGherkinManager {

    public Boolean registerStatements(GherkinTest test, IStatementOwner[] owners) throws ManagerException {
        Boolean required = false;
        try {
            for(IGherkinExecutable executable : test.getAllExecutables()) {
                for(IStatementOwner owner : owners) {
                    for(Method method : owner.getClass().getDeclaredMethods()) {
                        for(ExecutionMethod executeAnno : method.getAnnotationsByType(ExecutionMethod.class)) {
                            if(executeAnno.keyword().equals(executable.getKeyword())) {
                                Pattern annotationRegex = Pattern.compile(executeAnno.regex());
                                Matcher regexMatcher = annotationRegex.matcher(executable.getValue());
                                if(regexMatcher.matches()) {
                                    List<String> groups = new ArrayList<>();
                                    for(int i = 1; i <= regexMatcher.groupCount(); i++) {
                                        groups.add(regexMatcher.group(i));
                                    }
                                    executable.setRegexGroups(groups);
                                    executable.registerManager(this);
                                    executable.registerExecutionMethod(method, owner);
                                    required = true;
                                }
                            }
                        }
                        for(CustomExecutionMethod executeAnno : method.getAnnotationsByType(CustomExecutionMethod.class)) {
                            if(executeAnno.keyword().equals(executable.getKeyword())) {
                                if(owner.registerCustom(executeAnno, executable)) {
                                    executable.registerManager(this);
                                    executable.registerExecutionMethod(method, owner);
                                    required = true;
                                }
                            }
                        }
                    }
                }
            }
            return required;
        } catch (TestRunException e) {
            throw new ManagerException("Issue registering statements", e);
        }
    }
}
