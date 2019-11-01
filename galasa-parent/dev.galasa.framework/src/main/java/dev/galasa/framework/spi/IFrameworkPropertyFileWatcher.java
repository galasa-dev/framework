/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

public interface IFrameworkPropertyFileWatcher {

    public enum Event {
        MODIFIED,
        NEW,
        DELETE
    }

    void propertyModified(String key, Event event, String oldValue, String newValue);
}
