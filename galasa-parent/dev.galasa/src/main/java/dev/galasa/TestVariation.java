/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates this test has variations that will be entered into the Test Catalog
 * 
 * @author Michael Baylis
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(TestVariations.class)
public @interface TestVariation {
	
	String name();
	
	boolean defaultVariation() default false;
	
	TestVariationProperty[] properties() default {};

}
