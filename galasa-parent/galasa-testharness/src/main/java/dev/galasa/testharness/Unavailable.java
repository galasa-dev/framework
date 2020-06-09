/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.testharness;

public class Unavailable extends UnsupportedOperationException {
    private static final long serialVersionUID = 1L;

    public Unavailable() {
        super("Unavailable in test harness, add if needed");
    }

}
