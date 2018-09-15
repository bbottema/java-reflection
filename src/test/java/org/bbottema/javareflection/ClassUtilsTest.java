package org.bbottema.javareflection;

import org.bbottema.javareflection.util.ExternalClassLoader;
import org.bbottema.javareflection.valueconverter.IncompatibleTypeException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClassUtilsTest {
	
	@Before
	public void resetStaticCaches() {
		ClassUtils.resetCache();
	}
	
	/**
	 * Test for {@link ClassUtils#locateClass(String, boolean, ExternalClassLoader)}.
	 */
	@Test
	public void testLocateClass() {
		assertThat(ClassUtils.locateClass("Math", false, null)).isEqualTo(Math.class);
		assertThat(ClassUtils.locateClass("Mathh", false, null)).isNull();
		assertThat(ClassUtils.locateClass("ClassUtils", false, null)).isNull();
		assertThat(ClassUtils.locateClass("ClassUtils", true, null)).isEqualTo(ClassUtils.class);
		assertThat(ClassUtils.locateClass("Socket", false, null)).isNull();
		assertThat(ClassUtils.locateClass("Socket", true, null)).isEqualTo(Socket.class);
	}
	
	@Test
	public void testNewInstanceHappyFlow() {
		assertThat(ClassUtils.newInstanceSimple(Object.class).getClass()).isEqualTo(Object.class);
	}
	
	/**
	 * Test for {@link ClassUtils#solveField(Object, String)}.
	 *
	 * @throws NoSuchFieldException Thrown by tested method.
	 */
	@Test
	public void testSolveField()
			throws NoSuchFieldException {
		Field f1 = ClassUtils.solveField(new C(new Pear()), "numberB");
		assertThat(f1).isNotNull();
		assertThat(f1).isEqualTo(C.class.getField("numberB"));
		Field f2 = ClassUtils.solveField(C.class, "numberB_static");
		assertThat(f2).isNotNull();
		assertThat(f2).isEqualTo(C.class.getField("numberB_static"));
	}
	
	/**
	 * Test for {@link ClassUtils#assignToField(Object, String, Object)}.
	 *
	 * @throws IllegalAccessException Thrown by tested method.
	 * @throws NoSuchFieldException Thrown by tested method.
	 */
	@Test
	public void testAssignToField()
			throws IllegalAccessException, NoSuchFieldException {
		assertThat(ClassUtils.assignToField(new C(new Pear()), "numberB", 50)).isEqualTo(50);
		assertThat(ClassUtils.assignToField(new C(new Pear()), "numberB", "50")).isEqualTo(50);
		assertThat(ClassUtils.assignToField(new C(new Pear()), "numberB", 50d)).isEqualTo(50);
		try {
			ClassUtils.assignToField(new C(new Pear()), "numberB", new Pear());
			fail("IllegalAccessException expected due to incompatible types");
		} catch (IncompatibleTypeException e) {
			// ok
		}
		try {
			ClassUtils.assignToField(new C(new Pear()), "number_privateB", new Pear());
			fail("IllegalAccessException expected due to incompatible types");
		} catch (NoSuchFieldException e) {
			// ok
		}
	}
	
	/**
	 * Test for {@link ClassUtils#collectPropertyNames(Object)}.
	 */
	@Test
	public void testCollectPropertyNames() {
		Collection<String> properties = ClassUtils.collectPropertyNames(new C(new Pear()));
		assertThat(properties).isNotNull();
		assertThat(properties.size()).isEqualTo(4);
		assertThat(properties.contains("numberA")).isTrue();
		assertThat(properties.contains("numberB")).isTrue();
		assertThat(properties.contains("numberB_static")).isTrue();
		assertThat(properties.contains("numberC")).isTrue();
	}
	
	/**
	 * Test for {@link ClassUtils#collectMethods(Object, boolean)}.
	 */
	@Test
	public void testCollectPublicMethods() {
		Collection<String> oProperties = ClassUtils.collectMethods(new Object(), true);
		Collection<String> cProperties = ClassUtils.collectMethods(new MethodUtilsTest.C(new MethodUtilsTest.Pear()), true);
		assertThat(oProperties).isNotNull();
		assertThat(oProperties.size() > 0).isTrue();
		assertThat(cProperties.size()).isEqualTo(oProperties.size() + 1);
		cProperties.removeAll(oProperties);
		assertThat(cProperties.size()).isEqualTo(1);
		assertThat(cProperties.contains("foo")).isTrue();
	}
	
	/**
	 * Test for {@link ClassUtils#collectMethods(Object, boolean)}.
	 */
	@Test
	public void testCollectAllMethods() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final Object o = new Object();
		final MethodUtilsTest.C c = new MethodUtilsTest.C(new MethodUtilsTest.Pear());
		Collection<String> oProperties = ClassUtils.collectMethods(o, false);
		Collection<String> cProperties = ClassUtils.collectMethods(c, false);
		assertThat(oProperties).isNotNull();
		assertThat(oProperties.size() > 0).isTrue();
		assertThat(cProperties.size()).isEqualTo(oProperties.size() + 3);
		cProperties.removeAll(oProperties);
		assertThat(cProperties.size()).isEqualTo(3);
		assertThat(cProperties.contains("foo")).isTrue();
		assertThat(cProperties.contains("protectedMethod")).isTrue();
		assertThat(cProperties.contains("privateMethod")).isTrue();
		MethodUtils.invokeCompatibleMethod(c, MethodUtilsTest.C.class, "privateMethod");
	}
	
	static abstract class Fruit {
	}
	
	@SuppressWarnings("WeakerAccess")
	static class Pear extends Fruit {
	}
	
	interface Foo {
		String foo(Double value, Fruit fruit, char c);
	}
	
	@SuppressWarnings({"unused", "SameReturnValue", "WeakerAccess"})
	static abstract class A implements Foo {
		
		public Integer numberA;
		Integer number_privateA;
		
		public A(Fruit f) {
		}
		
		abstract String protectedMethod();
		
		@SuppressWarnings("unused")
		private String privateMethod() {
			return "private 1";
		}
	}
	
	@SuppressWarnings({"unused", "WeakerAccess"})
	static abstract class B extends A {
		
		public Integer numberB;
		Integer number_privateB;
		
		public static Integer numberB_static;
		
		public B(Fruit f) {
			super(f);
		}
		
		@Override
		String protectedMethod() {
			return "protected 1";
		}
	}
	
	@SuppressWarnings({"unused", "SameReturnValue", "WeakerAccess"})
	static class C extends B {
		public Integer numberC;
		Integer number_privateC;
		
		public C(Fruit f) {
			super(f);
		}
		
		public C(Pear p) {
			super(p);
		}
		
		@Override
		public String foo(Double value, Fruit fruit, char c) {
			return String.format("%s-%s-%s", value, fruit.getClass().getSimpleName(), c);
		}
		
		@Override
		String protectedMethod() {
			return "protected 2";
		}
		
		@SuppressWarnings("unused")
		private String privateMethod() {
			return "private 2";
		}
	}
}