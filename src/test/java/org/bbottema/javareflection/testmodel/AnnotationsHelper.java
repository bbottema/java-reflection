/*
 * Copyright (C) ${project.inceptionYear} Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
