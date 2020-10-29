package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * This DSS action request that a NEW property is added to the DSS.   If the 
 * property already exists, the transaction will fail.
 * If you want to add a property whether it exists or not, use DssUpdate
 * 
 * @author Michael Baylis
 *
 */
public class DssAdd implements IDssAction {
    
    private final String key;
    private final String value;

    public DssAdd(@NotNull String key, @NotNull String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }

}
