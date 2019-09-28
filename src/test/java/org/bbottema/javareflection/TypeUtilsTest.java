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
package org.bbottema.javareflection;

import org.bbottema.javareflection.testmodel.AnnotationsHelper;
import org.bbottema.javareflection.testmodel.AnnotationsHelper.MethodAnnotation;
import org.bbottema.javareflection.testmodel.AnnotationsHelper.ParamAnnotation3;
import org.bbottema.javareflection.testmodel.Fruit;
import org.bbottema.javareflection.testmodel.Pear;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bbottema.javareflection.ClassUtils.collectMethodsByName;
import static org.bbottema.javareflection.model.MethodModifier.MATCH_ANY;
import static org.bbottema.javareflection.testmodel.AnnotationsHelper.*;

public class TypeUtilsTest {
	
	@Test
	public void testAutobox() {
		assertThat(TypeUtils.autobox(boolean.class)).isEqualTo(Boolean.class);
		assertThat(TypeUtils.autobox(char.class)).isEqualTo(Character.class);
		assertThat(TypeUtils.autobox(byte.class)).isEqualTo(Byte.class);
		assertThat(TypeUtils.autobox(short.class)).isEqualTo(Short.class);
		assertThat(TypeUtils.autobox(int.class)).isEqualTo(Integer.class);
		assertThat(TypeUtils.autobox(long.class)).isEqualTo(Long.class);
		assertThat(TypeUtils.autobox(float.class)).isEqualTo(Float.class);
		assertThat(TypeUtils.autobox(double.class)).isEqualTo(Double.class);
		assertThat(TypeUtils.autobox(Boolean.class)).isEqualTo(boolean.class);
		assertThat(TypeUtils.autobox(Character.class)).isEqualTo(char.class);
		assertThat(TypeUtils.autobox(Byte.class)).isEqualTo(byte.class);
		assertThat(TypeUtils.autobox(Short.class)).isEqualTo(short.class);
		assertThat(TypeUtils.autobox(Integer.class)).isEqualTo(int.class);
		assertThat(TypeUtils.autobox(Long.class)).isEqualTo(long.class);
		assertThat(TypeUtils.autobox(Float.class)).isEqualTo(float.class);
		assertThat(TypeUtils.autobox(Double.class)).isEqualTo(double.class);
		assertThat(TypeUtils.autobox(Fruit.class)).isNull();
	}
	
	@Test
	public void testCollectTypes() {
		final Class<?>[] expectedTypeList = new Class<?>[]{Pear.class, String.class, null, Double.class};
		final Object[] objectList = new Object[]{new Pear(), "foo", null, 4d};
		assertThat(TypeUtils.collectTypes(objectList)).isEqualTo(expectedTypeList);
	}
	
	@Test
	public void testWidestNumberClass() {
		byte b = 1;
		short s = 1;
		int i = 1;
		long l = 1;
		float f = 1;
		double d = 1;
		Byte B = 1;
		Short S = 1;
		Integer I = 1;
		Long L = 1L;
		Float F = 1f;
		Double D = 1d;
		assertThat(TypeUtils.widestNumberClass(s, b)).isEqualTo(Short.class);
		assertThat(TypeUtils.widestNumberClass(b, B)).isEqualTo(Byte.class);
		assertThat(TypeUtils.widestNumberClass(B, b)).isEqualTo(Byte.class);
		assertThat(TypeUtils.widestNumberClass(b, s, i)).isEqualTo(Integer.class);
		assertThat(TypeUtils.widestNumberClass(b, s, i, l, f, d)).isEqualTo(Double.class);
		assertThat(TypeUtils.widestNumberClass(B, S, I, L, F, D)).isEqualTo(Double.class);
		assertThat(TypeUtils.widestNumberClass(L)).isEqualTo(Long.class);
		assertThat(TypeUtils.widestNumberClass(i, D)).isEqualTo(Double.class);
	}
	
	@Test
	public void testIsPackage() {
		assertThat(TypeUtils.isPackage("java")).isTrue();
		assertThat(TypeUtils.isPackage("java.util")).isTrue();
		assertThat(TypeUtils.isPackage("org.bbottema.javareflection")).isTrue();
		assertThat(TypeUtils.isPackage("java.lang.reflect")).isTrue();
		assertThat(TypeUtils.isPackage("org.junit")).isTrue();
		assertThat(TypeUtils.isPackage("donkey.cake")).isFalse();
		assertThat(TypeUtils.isPackage("org.bbottema")).isFalse();
	}
	
	@Test
	public void testReplaceInArray() {
		Integer[] initial = new Integer[]{1, 2, 3, 4};
		Integer[] second = TypeUtils.replaceInArray(initial, 2, 2);
		assertThat(second).isEqualTo(initial);
		assertThat(second).isEqualTo(new Integer[]{1, 2, 2, 4});
	}
	
	@Test
	public void testContainsAnnotation() {
		final Method annotatedMethod = findAnnotatedMethod();
		assertThat(annotatedMethod.getParameterAnnotations().length).isEqualTo(3);
		
		List<Annotation> methodAnnotations = asList(annotatedMethod.getAnnotations());
		assertThat(TypeUtils.containsAnnotation(methodAnnotations, Nullable.class)).isFalse();
		assertThat(TypeUtils.containsAnnotation(methodAnnotations, NotNull.class)).isFalse(); // retention Class
		assertThat(TypeUtils.containsAnnotation(methodAnnotations, MethodAnnotation.class)).isTrue(); // retention Runtime
		
		List<Annotation> param1Annotations = asList(annotatedMethod.getParameterAnnotations()[0]);
		assertThat(TypeUtils.containsAnnotation(param1Annotations, ParamAnnotation1.class)).isTrue();
		assertThat(TypeUtils.containsAnnotation(param1Annotations, ParamAnnotation2.class)).isFalse();
		assertThat(TypeUtils.containsAnnotation(param1Annotations, ParamAnnotation3.class)).isFalse();
		
		List<Annotation> param2Annotations = asList(annotatedMethod.getParameterAnnotations()[1]);
		assertThat(TypeUtils.containsAnnotation(param2Annotations, ParamAnnotation1.class)).isFalse();
		assertThat(TypeUtils.containsAnnotation(param2Annotations, ParamAnnotation2.class)).isTrue();
		assertThat(TypeUtils.containsAnnotation(param2Annotations, ParamAnnotation3.class)).isFalse();
		
		
		List<Annotation> param3Annotations = asList(annotatedMethod.getParameterAnnotations()[2]);
		assertThat(TypeUtils.containsAnnotation(param3Annotations, ParamAnnotation1.class)).isTrue();
		assertThat(TypeUtils.containsAnnotation(param3Annotations, ParamAnnotation2.class)).isTrue();
		assertThat(TypeUtils.containsAnnotation(param3Annotations, ParamAnnotation3.class)).isFalse();
	}
	
	private Method findAnnotatedMethod() {
		return collectMethodsByName(AnnotationsHelper.class, Object.class, MATCH_ANY, "methodWithAnnotations").iterator().next();
	}
}