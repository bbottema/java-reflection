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

import org.bbottema.javareflection.testmodel.reflectionutils.Bob;
import org.bbottema.javareflection.testmodel.reflectionutils.Moo;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReflectionUtilsTest {

	private Moo<Boolean> fieldDefinedGeneric = new Moo<Boolean>() {
	};

	@Test
	public void testFieldHasCorrectType() {
		assertThat(fieldDefinedGeneric.responseType).isEqualTo(Boolean.class);
	}

	@Test
	public void testFieldHasCorrectType2() {
		assertThat(((Bob) fieldDefinedGeneric).responseType).isEqualTo(Boolean.class);
	}

	@Test
	public void getFirstGenericClassType() {
		assertThat(ReflectionUtils.findParameterType(TestClassInteger.class, TestGenericClass.class, 0)).isEqualTo(Integer.class);
		assertThat(ReflectionUtils.findParameterType(TestClassNested.class, TestGenericClass.class, 0)).isEqualTo(OtherTestGenericClass.class);
		assertThat(ReflectionUtils.findParameterType(TestSubClassMultipleTypesSpecified.class, TestClassMultipleTypes.class, 1)).isEqualTo(Boolean.class);
	}

	public static class TestGenericClass<T> {
	}

	public static class OtherTestGenericClass<T> {
	}

	public static class TestClassInteger extends TestGenericClass<Integer> {
	}

	public static class TestClassNested extends TestGenericClass<OtherTestGenericClass<Integer>> {
	}

	public static class TestClassMultipleTypes<T1, T2> {
	}

	public static class TestSubClassMultipleTypes<T> extends TestClassMultipleTypes<Number, T> {
	}

	public static class TestSubClassMultipleTypesSpecified extends TestSubClassMultipleTypes<Boolean> {
	}
}