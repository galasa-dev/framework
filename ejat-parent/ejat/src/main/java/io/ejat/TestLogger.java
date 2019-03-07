package io.ejat;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * {@literal @}TestLogger annotation
 */
@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface TestLogger {

}
