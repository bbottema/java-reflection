package org.bbottema.javareflection;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JReflect.class})
public class JReflectPowermockTest {
	
	@Before
	public void resetStaticCaches() {
		JReflect.resetCaches();
	}
	
	@Test
	public void testNewInstanceHappyFlow() {
		assertSame(Object.class, JReflect.newInstanceSimple(Object.class).getClass());
	}
	
	@Test
	public void testNewInstanceSimple() throws Exception {
		testExceptionHandling(new SecurityException("moo"), "unable to invoke parameterless constructor; security problem", false);
		testExceptionHandling(new InstantiationException("moo"), "unable to complete instantiation of object", false);
		testExceptionHandling(new IllegalAccessException("moo"), "unable to access parameterless constructor", false);
		testExceptionHandling(new InvocationTargetException(null, "moo"), "unable to invoke parameterless constructor", false);
		testExceptionHandling(new NoSuchMethodException("moo"), "unable to find parameterless constructor (not public?)", true);
	}
	
	private void testExceptionHandling(Throwable exceptionThatShouldBeHandled, String expectedExceptionMessage, boolean onGetConstructor) throws Exception {
		PowerMockito.mockStatic(A.class);
		if (onGetConstructor) {
			PowerMockito.when(A.class.getConstructor()).thenThrow(exceptionThatShouldBeHandled);
		} else {
			@SuppressWarnings("unchecked")
			Constructor<A> constructorMock = PowerMockito.mock(Constructor.class);
			PowerMockito.when(A.class.getConstructor()).thenReturn(constructorMock);
			PowerMockito.when(constructorMock.newInstance()).thenThrow(exceptionThatShouldBeHandled);
		}
		
		Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			public void call() {
				JReflect.newInstanceSimple(A.class);
			}
		})
				.isInstanceOf(RuntimeException.class)
				.hasMessage(expectedExceptionMessage)
				.hasStackTraceContaining("moo");
	}
	
	public static class A {}
	
	@Test
	public void testLocateClass_CacheShouldShortcutLookup() throws Exception {
		PowerMockito.mockStatic(JReflect.class);
		PowerMockito.when(JReflect.class, "locateClass", "Integer", false, null).thenCallRealMethod().thenCallRealMethod();
		PowerMockito.when(JReflect.class, "locateClass", "java.lang.Integer", null).thenReturn(Byte.class).thenReturn(Double.class);
		
		Class<?> resultFromLookup = JReflect.locateClass("Integer", false, null);
		Class<?> resultFromCache = JReflect.locateClass("Integer", false, null);
		
		assertThat(resultFromLookup).isSameAs(Byte.class);
		assertThat(resultFromCache).isSameAs(resultFromLookup);
	}
}