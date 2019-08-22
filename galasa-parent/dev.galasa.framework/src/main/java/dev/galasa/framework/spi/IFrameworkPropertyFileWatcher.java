package dev.galasa.framework.spi;

public interface IFrameworkPropertyFileWatcher{

    public enum Event {
        MODIFIED, NEW, DELETE
    }

    void propertyModified(String key, Event event, String oldValue, String newValue);
}