package dev.voras.framework.spi;

public interface IDynamicStatusStoreWatcher {
	
    public enum Event {
        MODIFIED, NEW, DELETE
    }

    void propertyModified(String key, Event event, String oldValue, String newValue);


}
