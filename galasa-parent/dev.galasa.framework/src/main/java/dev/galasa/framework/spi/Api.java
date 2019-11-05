/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

public enum Api {
    AUTHENTICATION("auth", "authentication"),
    TESTCATALOG("testcatalog", "testcatalog"),
    RUN("run", "run");

    private final String suffix;
    private final String property;

    private Api(String suffix, String property) {
        this.suffix = suffix;
        this.property = property;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getProperty() {
        return property;
    }
}
