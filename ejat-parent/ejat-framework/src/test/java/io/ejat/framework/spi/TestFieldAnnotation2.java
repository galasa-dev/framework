package io.ejat.framework.spi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
@ValidAnnotatedFields({ String.class, Long.class })
public @interface TestFieldAnnotation2 {

}
