/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi.language;

import java.lang.reflect.Method;

import dev.galasa.framework.spi.language.gherkin.GherkinMethod;

public class GalasaMethod {

    private GalasaLanguage language;

    private Method javaTestMethod;
    private Method javaExecutionMethod;
    private GherkinMethod gherkinMethod;

    public GalasaMethod(Method executionMethod, Method testMethod) {
        this.javaTestMethod = testMethod;
        this.javaExecutionMethod = executionMethod;
        this.language = GalasaLanguage.java;
    }

    public GalasaMethod(GherkinMethod method) {
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

    public GherkinMethod getGherkinMethod() {
        return this.gherkinMethod;
    }
    
}