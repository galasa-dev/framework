/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.spi;

/**
 * Exceptions types which support it have an associated FrameworkErrorCode.
 *
 * This allows a FrameworkException to have some finer-grained error information
 * which is programmatically accessible. ie: Not just the failure message text.
 *
 * We explicitly set an ID for each enumeration value, so that we have complete
 * control over it, and it will never move if the enumeration values are re-ordered,
 * or any are inserted in any order. Using the ordinal leads to issues with support
 * if the compiler allocates different values over different releases.
 */
public enum FrameworkErrorCode {
    /**
     * We don't know the nature of the failure beyond the exception type and the message text.
     */
    UNKNOWN(0),

    /**
     * Validation of the namespace name failed for some reason.
     */
    INVALID_NAMESPACE(1),

    /**
     * Validation of the CPS property name failed for some reason.
     */
    INVALID_PROPERTY(2);

    
    private int id ;
    private FrameworkErrorCode(int errorId) {
        this.id = errorId;
    }

    /**
     * Each error code has an ID value.
     * @return The id for this error code.
     */
    public int getErrorId() {
        return this.id ;
    }

    /**
     * Given the errorId , work out which FrameworkErrorCode corresponds to it.
     * @param errorIdToFind The error id to search for
     * @return The FrameworkErrorCode found, else {@link FrameworkErrorCode#UNKNOWN}
     */
    public static FrameworkErrorCode findByErrorId(int errorIdToFind) {
        for ( FrameworkErrorCode value : FrameworkErrorCode.values() ) {
            if (value.getErrorId() == errorIdToFind ) {
                // Found it.
                return value ;
            }
        }
        return UNKNOWN;
    }
}
