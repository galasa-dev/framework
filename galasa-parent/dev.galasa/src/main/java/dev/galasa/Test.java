package dev.galasa;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * {@literal @}Test annotation
 */
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Test {

}
