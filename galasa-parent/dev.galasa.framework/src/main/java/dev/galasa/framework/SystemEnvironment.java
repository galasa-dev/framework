/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework;

/**
 * An implementation of the Environment interface which allows code to get
 * environment variables from the real system.
 */
public class SystemEnvironment implements Environment {

    @Override
    public String getenv(String propertyName) {
        return System.getenv(propertyName);
    }

    @Override
    public String getProperty(String propertyName) {
        return System.getProperty(propertyName);
    }
    
}
