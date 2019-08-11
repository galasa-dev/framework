package dev.voras.framework.spi;

import javax.validation.constraints.NotNull;


public interface IResultArchiveStoreRegistration {
    
    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws ResultArchiveStoreException;
}