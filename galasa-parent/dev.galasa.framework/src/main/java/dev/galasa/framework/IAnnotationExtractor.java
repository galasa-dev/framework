/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.lang.annotation.Annotation;

public interface IAnnotationExtractor {
    public <A,B extends Annotation> B getAnnotation (Class<A> testClass, Class<B> annotationClass);
}
