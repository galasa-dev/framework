/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Paired with ITestCatalogBuilder, classes annotated with this will be called
 * during the test catalog build process to append information to the test class
 * entry, and the overall test catalog json file.
 * 
 * @author Michael Baylis
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface TestCatalogBuilder {

}
