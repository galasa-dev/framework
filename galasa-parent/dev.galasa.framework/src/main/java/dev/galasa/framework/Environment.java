/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework;

/** 
 * An interface through which we can get environment variable values. 
 * Allows for easy mocking of the System class, which is notoriously hard to mock
 * out for unit tests.
 */
public interface Environment {

    /** Gets the value of a given environment variable. */
    String getenv(String propertyName);

    /** Gets a system property */
    String getProperty(String propertyName);
}
