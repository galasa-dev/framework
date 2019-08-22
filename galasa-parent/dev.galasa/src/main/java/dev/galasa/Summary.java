package dev.galasa;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Provides a summary text to add to the test catalog
 * 
 * @author Michael Baylis
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Summary {
	
	String value();
}
