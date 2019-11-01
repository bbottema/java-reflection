package org.bbottema.javareflection.testmodel;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AnnotationsHelper {
	
	@MethodAnnotation
	@NotNull
	public Integer methodWithAnnotations(@ParamAnnotation1 int i,
									  @ParamAnnotation2 Object o,
									  @ParamAnnotation1 @ParamAnnotation2 Moo m) {
		return null;
	}
	
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface MethodAnnotation { }
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) public @interface ParamAnnotation1 { }
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) public @interface ParamAnnotation2 { }
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) public @interface ParamAnnotation3 { }
	
}
