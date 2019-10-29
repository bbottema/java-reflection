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

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.bbottema.javareflection.testmodel.A;
import org.bbottema.javareflection.testmodel.Fruit;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClassUtils.class, A.class})
public class ClassUtilsPowermockTest {
	
	@Before
	public void resetStaticCaches() {
		ValueConversionHelper.resetDefaultConverters();
	}
	
	@Test
	public void testNewInstanceHappyFlow() {
		assertThat(ClassUtils.newInstanceSimple(Object.class).getClass()).isEqualTo(Object.class);
	}
	
	@Test
	public void testNewInstanceSimple() throws Exception {
		testExceptionHandling(new SecurityException("moo"), "unable to invoke parameterless constructor; security problem", false);
		testExceptionHandling(new InstantiationException("moo"), "unable to complete instantiation of object", false);
		testExceptionHandling(new IllegalAccessException("moo"), "unable to access parameterless constructor", false);
		testExceptionHandling(new InvocationTargetException(null, "moo"), "unable to invoke parameterless constructor", false);
		testExceptionHandling(new NoSuchMethodException("moo"), "unable to find parameterless constructor (not public?)", true);
	}
	
	@SuppressWarnings({ "unchecked", "JavaReflectionMemberAccess" })
	private void testExceptionHandling(Throwable exceptionThatShouldBeHandled, String expectedExceptionMessage, boolean onGetConstructor) throws Exception {
		PowerMockito.mockStatic(A.class);
		if (onGetConstructor) {
			PowerMockito.when(A.class.getConstructor()).thenThrow(exceptionThatShouldBeHandled);
		} else {
			Constructor<A> constructorMock = PowerMockito.mock(Constructor.class);
			PowerMockito.when(A.class.getConstructor()).thenReturn(constructorMock);
			PowerMockito.when(constructorMock.newInstance()).thenThrow(exceptionThatShouldBeHandled);
		}
		
		Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			public void call() {
				ClassUtils.newInstanceSimple(A.class);
			}
		})
				.isInstanceOf(RuntimeException.class)
				.hasMessage(expectedExceptionMessage)
				.hasStackTraceContaining("moo");
	}
	
	@Test
	public void testLocateClass_CacheShouldShortcutLookup() throws Exception {
		PowerMockito.mockStatic(ClassUtils.class);
		PowerMockito.when(ClassUtils.class, "locateClass", "Integer", false, null).thenCallRealMethod().thenCallRealMethod();
		PowerMockito.when(ClassUtils.class, "locateClass", "Integer", "java.lang", null).thenReturn(Byte.class).thenReturn(Double.class);
		
		Class<?> resultFromLookup = ClassUtils.locateClass("Integer", false, null);
		Class<?> resultFromCache = ClassUtils.locateClass("Integer", false, null);
		
		assertThat(resultFromLookup).isSameAs(Byte.class);
		assertThat(resultFromCache).isSameAs(resultFromLookup);
	}
}