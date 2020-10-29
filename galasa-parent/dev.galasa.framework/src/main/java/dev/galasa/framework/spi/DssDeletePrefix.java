package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * This DSS action request that all properties with this prefix are deleted.
 * 
 * @author Michael Baylis
 *
 */
public class DssDeletePrefix implements IDssAction {
    
    private final String prefix;

    public DssDeletePrefix(@NotNull String prefix) {
        this.prefix = prefix;
    }
    
    public String getPfrefix() {
        return prefix;
    }
    
}
