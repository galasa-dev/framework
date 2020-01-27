/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that this Class setups up and validates a Shared Environment.
 * The structure is the same as a {@literal @}Test class
 */
@Retention(RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface SharedEnvironment {
    
    /**
     * How many hours after the Shared Environment was built should the Run be discarded, should only
     * matter if the discard was run properly after all the testing has been completed
     */
    int expireAfterHours() default 8;

}
