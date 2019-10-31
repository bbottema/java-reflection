package org.bbottema.javareflection.util;

import org.assertj.core.api.iterable.Extractor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MetaAnnotationExtractor<T extends Annotation> implements Extractor<Method, Annotation> {
	private final Class<T> annotationClass;
	
	public MetaAnnotationExtractor(Class<T> annotationClass) {
		this.annotationClass = annotationClass;
	}
	
	@Override
	public Annotation extract(Method input) {
		return input.getAnnotation(annotationClass);
	}
}
