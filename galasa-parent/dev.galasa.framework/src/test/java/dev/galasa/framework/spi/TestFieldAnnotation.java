/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RUNTIME)
@Target(FIELD)
@ValidAnnotatedFields({ String.class, Long.class })
@TestManagerAnnotation
public @interface TestFieldAnnotation {

}
