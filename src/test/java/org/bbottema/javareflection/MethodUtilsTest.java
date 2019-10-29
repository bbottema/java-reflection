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

import org.bbottema.javareflection.model.InvokableObject;
import org.bbottema.javareflection.model.LookupMode;
import org.bbottema.javareflection.model.MethodModifier;
import org.bbottema.javareflection.model.MethodParameter;
import org.bbottema.javareflection.testmodel.A;
import org.bbottema.javareflection.testmodel.B;
import org.bbottema.javareflection.testmodel.C;
import org.bbottema.javareflection.testmodel.Foo;
import org.bbottema.javareflection.testmodel.Fruit;
import org.bbottema.javareflection.testmodel.Kraa;
import org.bbottema.javareflection.testmodel.Meta;
import org.bbottema.javareflection.testmodel.Moo;
import org.bbottema.javareflection.testmodel.Pear;
import org.bbottema.javareflection.testmodel.Skree;
import org.bbottema.javareflection.util.MetaAnnotationExtractor;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.junit.Before;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.EnumSet.allOf;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.bbottema.javareflection.ClassUtils.collectMethodsByName;
import static org.bbottema.javareflection.MethodUtils.onlyMethod;
import static org.bbottema.javareflection.model.MethodModifier.MATCH_ANY;

public class MethodUtilsTest {
	
	@Before
	public void resetStaticCaches() {
		ValueConversionHelper.resetDefaultConverters();
	}
	
	@Test
	public void testFindCompatibleMethod()
			throws NoSuchMethodException {
		// find method through interface on superclass, using autoboxing, class casting and an auto convert
		Set<LookupMode> allButSmartLookup = new HashSet<>(LookupMode.FULL);
		allButSmartLookup.remove(LookupMode.SMART_CONVERT);
		
		Set<InvokableObject<Method>> m = MethodUtils.findCompatibleMethod(B.class, "foo", allButSmartLookup, double.class, Pear.class, String.class);
		assertThat(m).hasSize(1);
		assertThat(onlyMethod(m)).isEqualTo(Foo.class.getMethod("foo", Double.class, Fruit.class, char.class));
		Set<InvokableObject<Method>> m2 = MethodUtils.findCompatibleMethod(B.class, "foo", allButSmartLookup, double.class, Pear.class, String.class);
		assertThat(m2).hasSize(1);
		assertThat(m2).isEqualTo(m);
		// find the same method, but now the first implementation on C should be returned
		m = MethodUtils.findCompatibleMethod(C.class, "foo", allButSmartLookup, double.class, Pear.class, String.class);
		assertThat(m).hasSize(1);
		assertThat(onlyMethod(m)).isEqualTo(C.class.getMethod("foo", Double.class, Fruit.class, char.class));
		// find a String method
		m = MethodUtils.findCompatibleMethod(String.class, "concat", EnumSet.noneOf(LookupMode.class), String.class);
		assertThat(m).hasSize(1);
		assertThat(onlyMethod(m)).isEqualTo(String.class.getMethod("concat", String.class));
		// shouldn't be able to find the following methods
		try {
			MethodUtils.findCompatibleMethod(B.class, "foos", allButSmartLookup, double.class, Pear.class, String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.findCompatibleMethod(B.class, "foo", allButSmartLookup, double.class, String.class, String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.findCompatibleMethod(B.class, "foo", allButSmartLookup, double.class, Fruit.class, Math.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		Set<InvokableObject<Method>> result = MethodUtils.findCompatibleMethod(B.class, "foo", LookupMode.FULL, double.class, Fruit.class, Math.class);
		assertThat(result).hasSize(1);
	}
	
	@Test
	public void testFindCompatibleConstructor()
			throws NoSuchMethodException {
		// find constructor on superclass, using autoboxing
		Set<InvokableObject<Constructor>> m = MethodUtils.findCompatibleConstructor(B.class, LookupMode.FULL, Fruit.class);
		assertThat(m).isNotEmpty();
		assertThat(m.iterator().next().getMethod()).isEqualTo(B.class.getConstructor(Fruit.class));
		Set<InvokableObject<Constructor>> m2 = MethodUtils.findCompatibleConstructor(B.class, LookupMode.FULL, Fruit.class);
		assertThat(m2).isNotEmpty();
		assertThat(m2).isEqualTo(m);
		// find constructor on superclass, using autoboxing and class casting
		m = MethodUtils.findCompatibleConstructor(B.class, LookupMode.FULL, Pear.class);
		assertThat(m).isNotEmpty();
		assertThat(m.iterator().next().getMethod()).isEqualTo(B.class.getConstructor(Fruit.class));
		// still find constructor on superclass
		m = MethodUtils.findCompatibleConstructor(C.class, LookupMode.FULL, Fruit.class);
		assertThat(m).isNotEmpty();
		assertThat(m.iterator().next().getMethod()).isEqualTo(C.class.getConstructor(Fruit.class));
		// still find constructor on subclass
		m = MethodUtils.findCompatibleConstructor(C.class, LookupMode.FULL, Pear.class);
		assertThat(m).isNotEmpty();
		assertThat(m.iterator().next().getMethod()).isEqualTo(C.class.getConstructor(Pear.class));
		// find a String constructor
		m = MethodUtils.findCompatibleConstructor(String.class, EnumSet.noneOf(LookupMode.class), String.class);
		assertThat(m).isNotEmpty();
		assertThat(m.iterator().next().getMethod()).isEqualTo(String.class.getConstructor(String.class));
		// shouldn't be able to find the following methods
		try {
			MethodUtils.findCompatibleConstructor(B.class, LookupMode.FULL, double.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.findCompatibleConstructor(B.class, LookupMode.FULL, String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}
	
	@Test
	public void testInvokeCompatibleMethod()
			throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		MethodUtils.invokeCompatibleMethod(new C(new Pear()), B.class, "foo", 50d, new Pear(), "g");
		MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "foo", 50d, new Pear(), "g");
		MethodUtils.invokeCompatibleMethod("", String.class, "concat", String.class);
		
		// shouldn't be able to find the following methods
		try {
			MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "foos", 50d, new Pear(), "g");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "foo", 50d, "foobar", "g");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "foo", 50d, new Pear(), Calendar.getInstance());
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}
	
	@Test
	public void testInvokeCompatibleConstructor()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		try {
			MethodUtils.invokeCompatibleConstructor(B.class, new Pear());
		} catch (InstantiationException e) {
			// Ok
		}
		MethodUtils.invokeCompatibleConstructor(C.class, new Pear());
		MethodUtils.invokeCompatibleConstructor(String.class, "test string");
		MethodUtils.invokeCompatibleConstructor(String.class, 1234567);
		// shouldn't be able to find the following methods
		try {
			MethodUtils.invokeCompatibleConstructor(B.class, 50d);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.invokeCompatibleConstructor(B.class, "foobar");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}
	
	@Test
	public void testIsMethodCompatible_Simple() throws NoSuchMethodException {
		Set<LookupMode> lookupModes = EnumSet.noneOf(LookupMode.class);
		
		Method m = Math.class.getMethod("min", int.class, int.class);
		assertThat(MethodUtils.isMethodCompatible(m, lookupModes, int.class, int.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(m, lookupModes, int.class, Calendar.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(m, lookupModes, int.class, A.class)).isFalse();
		
		Method stringConcat = String.class.getMethod("concat", String.class);
		assertThat(MethodUtils.isMethodCompatible(stringConcat, lookupModes, String.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, lookupModes, Calendar.class)).isFalse();
	}
	
	@Test
	public void testIsMethodCompatible_TestLookupModes() throws NoSuchMethodException {
		Set<LookupMode> noConversions = EnumSet.noneOf(LookupMode.class);
		Set<LookupMode> commonConversions = EnumSet.of(LookupMode.COMMON_CONVERT);
		Set<LookupMode> castConvert = EnumSet.of(LookupMode.CAST_TO_SUPER, LookupMode.CAST_TO_INTERFACE);
		Set<LookupMode> castThenCommonsConvert = EnumSet.of(LookupMode.CAST_TO_SUPER, LookupMode.COMMON_CONVERT);
		Set<LookupMode> smartConversions = EnumSet.of(LookupMode.SMART_CONVERT);
		
		Method m = Math.class.getMethod("min", int.class, int.class);
		assertThat(MethodUtils.isMethodCompatible(m, noConversions, int.class, boolean.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(m, commonConversions, int.class, boolean.class)).isTrue();
		
		Method cFoo = C.class.getMethod("foo", Double.class, Fruit.class, char.class);
		assertThat(MethodUtils.isMethodCompatible(cFoo, castConvert, Double.class, Pear.class, char.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(cFoo, noConversions, double.class, Pear.class, String.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(cFoo, commonConversions, double.class, Pear.class, String.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(cFoo, castConvert, double.class, Pear.class, String.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(cFoo, castThenCommonsConvert, double.class, Pear.class, String.class)).isTrue();
		
		Method stringConcat = String.class.getMethod("concat", String.class);
		assertThat(MethodUtils.isMethodCompatible(stringConcat, noConversions, Calendar.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, noConversions, String.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, commonConversions, String.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, commonConversions, Calendar.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, smartConversions, Calendar.class)).isTrue();
	}
	
	@SuppressWarnings("ConstantConditions")
	@Test
	public void testInvokeCompatibleMethod_VariousAccessLevels() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final C c = new C(new Pear());
		assertThat(MethodUtils.invokeCompatibleMethod(c, A.class, "privateMethod")).isEqualTo("private 1");
		assertThat(MethodUtils.invokeCompatibleMethod(c, B.class, "protectedMethod")).isEqualTo("protected 2");
		assertThat(MethodUtils.invokeCompatibleMethod(c, C.class, "privateMethod")).isEqualTo("private 2");
		assertThat(MethodUtils.invokeCompatibleMethod(c, C.class, "protectedMethod")).isEqualTo("protected 2");
		
		assertThat(((Number) MethodUtils.invokeCompatibleMethod(null, Math.class, "min", 1, true)).intValue()).isEqualTo(1);
		assertThat(((Number) MethodUtils.invokeCompatibleMethod(null, Math.class, "min", 1, false)).intValue()).isEqualTo(0);
		assertThat(((Number) MethodUtils.invokeCompatibleMethod(null, Math.class, "min", 0, true)).intValue()).isEqualTo(0);
		assertThat(((Number) MethodUtils.invokeCompatibleMethod(null, Math.class, "min", "d", 1000)).intValue()).isEqualTo(100); // d -> 100
	}
	
	@Test
	public void testFindMatchingMethodsParamArray() {
		assertThat(MethodUtils.findMatchingMethods(Moo.class, Object.class, "method1", "Integer"))
				.extracting(new MetaAnnotationExtractor<>(Meta.class))
				.extractingResultOf("value")
				.containsExactlyInAnyOrder("Moo.method1-A", "Shmoo.method1-A");
		
		assertThat(MethodUtils.findMatchingMethods(Moo.class, Object.class, "method1", "Object", "java.lang.Integer"))
				.extracting(new MetaAnnotationExtractor<>(Meta.class))
				.extractingResultOf("value")
				.containsExactlyInAnyOrder("Moo.method1-C");
	}
	
	@Test
	public void testFindMatchingMethodsParamCollection() {
		//noinspection ArraysAsListWithZeroOrOneArgument
		assertThat(MethodUtils.findMatchingMethods(Moo.class, Object.class, "method1", asList("Integer")))
				.extracting(new MetaAnnotationExtractor<>(Meta.class))
				.extractingResultOf("value")
				.containsExactlyInAnyOrder("Moo.method1-A", "Shmoo.method1-A");
		
		assertThat(MethodUtils.findMatchingMethods(Moo.class, Object.class, "method1", asList("Object", "java.lang.Integer")))
				.extracting(new MetaAnnotationExtractor<>(Meta.class))
				.extractingResultOf("value")
				.containsExactlyInAnyOrder("Moo.method1-C");
	}

	@Test
	public void testZipParametersAndArguments() {
		Method testMethod = ClassUtils.findFirstMethodByName(Kraa.class, Kraa.class, allOf(MethodModifier.class), "testMethod");
		LinkedHashMap<MethodParameter, Object> result = MethodUtils.zipParametersAndArguments(testMethod, 5, new ArrayList(), new HashSet<Double>());

		Annotation[] p1 = testMethod.getParameterAnnotations()[0];
		Annotation[] p2 = testMethod.getParameterAnnotations()[1];
		Annotation[] p3 = testMethod.getParameterAnnotations()[2];

		// verify annotations, which means the result assertions also saw these annotations
		assertThat(p1).isEmpty();
		assertThat(p2).extractingResultOf("annotationType").containsExactly(Nullable.class);
		assertThat(p3).extractingResultOf("annotationType").containsExactly(Nonnull.class);

		ParameterizedTypeImpl parameterizedHashSet = ParameterizedTypeImpl.make(HashSet.class, new Type[]{Double.class}, null);

		assertThat(result).containsExactly(
				new SimpleEntry<>(new MethodParameter(0, Integer.class, Integer.class, asList(p1)), 5),
				new SimpleEntry<>(new MethodParameter(1, List.class, List.class, asList(p2)), new ArrayList()),
				new SimpleEntry<>(new MethodParameter(2, HashSet.class, parameterizedHashSet, asList(p3)), new HashSet<Double>())
		);
	}
	
	@Test
	public void testMethodHasCollectionParameter() {
		assertThat(MethodUtils.methodHasCollectionParameter(findSkreeMethod("methodWithArray"))).isTrue();
		assertThat(MethodUtils.methodHasCollectionParameter(findSkreeMethod("methodWithCollection1"))).isTrue();
		assertThat(MethodUtils.methodHasCollectionParameter(findSkreeMethod("methodWithCollection2"))).isTrue();
		assertThat(MethodUtils.methodHasCollectionParameter(findSkreeMethod("methodWithCollection3"))).isTrue();
		assertThat(MethodUtils.methodHasCollectionParameter(findSkreeMethod("methodWithCollection4"))).isTrue();
		assertThat(MethodUtils.methodHasCollectionParameter(findSkreeMethod("methodWithCollection5"))).isTrue();
		assertThat(MethodUtils.methodHasCollectionParameter(findSkreeMethod("methodWithoutCollection1"))).isFalse();
		assertThat(MethodUtils.methodHasCollectionParameter(findSkreeMethod("methodWithoutCollection2"))).isFalse();
		assertThat(MethodUtils.methodHasCollectionParameter(findSkreeMethod("methodWithoutCollection3"))).isFalse();
	}
	
	private Method findSkreeMethod(String methodName) {
		return collectMethodsByName(Skree.class, Skree.class, MATCH_ANY, methodName).iterator().next();
	}

	@Test
	public void testFirstParameterArgumentByAnnotation() {
		final Method testMethod = requireNonNull(ClassUtils.findFirstMethodByName(Kraa.class, Kraa.class, MATCH_ANY, "testMethod"));
		final int argOne = 2;
		final List argTwo = null;
		final HashSet argThree = new HashSet();
		final Object[] arguments = {argOne, argTwo, argThree};
		assertThat(MethodUtils.firstParameterArgumentByAnnotation(testMethod, arguments, Nullable.class)).isSameAs(argTwo);
		assertThat(MethodUtils.firstParameterArgumentByAnnotation(testMethod, arguments, Nonnull.class)).isSameAs(argThree);
		assertThat(MethodUtils.firstParameterArgumentByAnnotation(testMethod, arguments, Meta.class)).isNull();
	}

	@Test
	public void testFirstParameterIndexByAnnotation() {
		final Method testMethod = requireNonNull(ClassUtils.findFirstMethodByName(Kraa.class, Kraa.class, MATCH_ANY, "testMethod"));

		assertThat(MethodUtils.firstParameterIndexByAnnotation(testMethod, Nullable.class)).isEqualTo(1);
		assertThat(MethodUtils.firstParameterIndexByAnnotation(testMethod, Nonnull.class)).isEqualTo(2);
		assertThat(MethodUtils.firstParameterIndexByAnnotation(testMethod, Meta.class)).isEqualTo(-1);
	}
}