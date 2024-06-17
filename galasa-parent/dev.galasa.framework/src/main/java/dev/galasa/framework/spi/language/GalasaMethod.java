/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language;

import java.lang.reflect.Method;

import dev.galasa.framework.spi.language.gherkin.GherkinScenario;

public class GalasaMethod {

    private GalasaLanguage language;

    private Method javaTestMethod;
    private Method javaExecutionMethod;
    private GherkinScenario gherkinMethod;

    public GalasaMethod(Method executionMethod, Method testMethod) {
        this.javaTestMethod = testMethod;
        this.javaExecutionMethod = executionMethod;
        this.language = GalasaLanguage.java;
    }

    public GalasaMethod(GherkinScenario method) {
        this.gherkinMethod = method;
        this.language = GalasaLanguage.gherkin;
    }

    public GalasaLanguage getLanguage() {
        return this.language;
    }

    public Boolean isJava() {
        return this.language == GalasaLanguage.java;
    }

    public Boolean isGherkin() {
        return this.language == GalasaLanguage.gherkin;
    }

    public Method getJavaTestMethod() {
        return this.javaTestMethod;
    }

    public Method getJavaExecutionMethod() {
        return this.javaExecutionMethod;
    }

    public GherkinScenario getGherkinMethod() {
        return this.gherkinMethod;
    }
    
}