package org.bbottema.javareflection;

import org.assertj.core.api.Assertions;
import org.bbottema.javareflection.ClassUtils.ConstructorFactory;
import org.bbottema.javareflection.testmodel.A;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClassUtilsPowermockTest {

	@BeforeEach
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

	private void testExceptionHandling(Throwable exceptionThatShouldBeHandled, String expectedExceptionMessage, boolean onGetConstructor) throws InvocationTargetException, InstantiationException, IllegalAccessException {
		try (MockedStatic<ConstructorFactory> mockedStatic = Mockito.mockStatic(ConstructorFactory.class)) {
			if (onGetConstructor) {
				mockedStatic.when(() -> ConstructorFactory.obtainConstructor(A.class)).thenThrow(exceptionThatShouldBeHandled);
			} else {
				Constructor<A> constructorMock = Mockito.mock(Constructor.class);
				mockedStatic.when(() -> ConstructorFactory.obtainConstructor(A.class)).thenReturn(constructorMock);
				Mockito.when(constructorMock.newInstance()).thenThrow(exceptionThatShouldBeHandled);
			}

			Exception exception = assertThrows(RuntimeException.class, () -> ClassUtils.newInstanceSimple(A.class));
			Assertions.assertThat(exception.getMessage()).contains(expectedExceptionMessage);
			Assertions.assertThat(exception).hasCauseExactlyInstanceOf(exceptionThatShouldBeHandled.getClass());
		}
	}

	@Test
	public void testLocateClass_CacheShouldShortcutLookup() throws Exception {
		try (MockedStatic<ClassUtils> mockedStatic = Mockito.mockStatic(ClassUtils.class)) {
			mockedStatic.when(() -> ClassUtils.locateClass("Integer", false, null)).thenCallRealMethod().thenCallRealMethod();
			mockedStatic.when(() -> ClassUtils.locateClass("Integer", "java.lang", null)).thenReturn(Byte.class).thenReturn(Double.class);

			Class<?> resultFromLookup = ClassUtils.locateClass("Integer", false, null);
			Class<?> resultFromCache = ClassUtils.locateClass("Integer", false, null);

			assertThat(resultFromLookup).isSameAs(Byte.class);
			assertThat(resultFromCache).isSameAs(resultFromLookup);
		}
	}
}