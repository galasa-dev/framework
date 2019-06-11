package dev.voras.framework.spi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.voras.framework.spi.ValidAnnotatedFields;

@Retention(RUNTIME)
@Target(FIELD)
@ValidAnnotatedFields({ String.class, Long.class })
@TestManagerAnnotation
public @interface TestFieldAnnotation {

}
