package org.codemonkey.util.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EnumSet;

import org.codemonkey.util.reflect.JReflect.LookupMode;
import org.junit.Test;

public class JReflectTest {
	/**
	 * We'll believe all the catch exceptions, they are not there to provide function, but to hide boilerplate code. just test happy code.
	 */
	@Test
	public void testNewInstanceSimple() {
		assertSame(Object.class, JReflect.newInstanceSimple(Object.class).getClass());
	}

	@Test
	public void testFindCompatibleJavaMethod()
			throws NoSuchMethodException {
		// find method through interface on superclass, using autoboxing, class casting and an auto convert
		Method m = JReflect.findCompatibleJavaMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Pear.class,
				String.class);
		assertNotNull(m);
		assertEquals(Foo.class.getMethod("foo", Double.class, Fruit.class, char.class), m);
		Method m2 = JReflect.findCompatibleJavaMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Pear.class,
				String.class);
		assertNotNull(m2);
		assertSame(m, m2);
		// find the same method, but now the first implementation on C should be returned
		m = JReflect.findCompatibleJavaMethod(C.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Pear.class, String.class);
		assertNotNull(m);
		assertEquals(C.class.getMethod("foo", Double.class, Fruit.class, char.class), m);
		// find a String method
		m = JReflect.findCompatibleJavaMethod(String.class, "concat", EnumSet.noneOf(LookupMode.class), String.class);
		assertNotNull(m);
		assertEquals(String.class.getMethod("concat", String.class), m);
		// shouldn't be able to find the following methods
		try {
			JReflect.findCompatibleJavaMethod(B.class, "foos", EnumSet.allOf(LookupMode.class), double.class, Pear.class, String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			JReflect.findCompatibleJavaMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, String.class, String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			JReflect.findCompatibleJavaMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Fruit.class, Math.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}

	@Test
	public void testFindCompatibleJavaConstructor()
			throws NoSuchMethodException {
		// find constructor on superclass, using autoboxing
		Constructor<?> m = JReflect.findCompatibleJavaConstructor(B.class, EnumSet.allOf(LookupMode.class), Fruit.class);
		assertNotNull(m);
		assertEquals(B.class.getConstructor(Fruit.class), m);
		Constructor<?> m2 = JReflect.findCompatibleJavaConstructor(B.class, EnumSet.allOf(LookupMode.class), Fruit.class);
		assertNotNull(m2);
		assertSame(m, m2);
		// find constructor on superclass, using autoboxing and class casting
		m = JReflect.findCompatibleJavaConstructor(B.class, EnumSet.allOf(LookupMode.class), Pear.class);
		assertNotNull(m);
		assertEquals(B.class.getConstructor(Fruit.class), m);
		// still find constructor on superclass
		m = JReflect.findCompatibleJavaConstructor(C.class, EnumSet.allOf(LookupMode.class), Fruit.class);
		assertNotNull(m);
		assertEquals(C.class.getConstructor(Fruit.class), m);
		// still find constructor on subclass
		m = JReflect.findCompatibleJavaConstructor(C.class, EnumSet.allOf(LookupMode.class), Pear.class);
		assertNotNull(m);
		assertEquals(C.class.getConstructor(Pear.class), m);
		// find a String constructor
		m = JReflect.findCompatibleJavaConstructor(String.class, EnumSet.noneOf(LookupMode.class), String.class);
		assertNotNull(m);
		assertEquals(String.class.getConstructor(String.class), m);
		// shouldn't be able to find the following methods
		try {
			JReflect.findCompatibleJavaConstructor(B.class, EnumSet.allOf(LookupMode.class), double.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			JReflect.findCompatibleJavaConstructor(B.class, EnumSet.allOf(LookupMode.class), String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}

	@Test
	public void testAutobox() {
		assertSame(Boolean.class, JReflect.autobox(boolean.class));
		assertSame(Character.class, JReflect.autobox(char.class));
		assertSame(Byte.class, JReflect.autobox(byte.class));
		assertSame(Short.class, JReflect.autobox(short.class));
		assertSame(Integer.class, JReflect.autobox(int.class));
		assertSame(Long.class, JReflect.autobox(long.class));
		assertSame(Float.class, JReflect.autobox(float.class));
		assertSame(Double.class, JReflect.autobox(double.class));
		assertSame(boolean.class, JReflect.autobox(Boolean.class));
		assertSame(char.class, JReflect.autobox(Character.class));
		assertSame(byte.class, JReflect.autobox(Byte.class));
		assertSame(short.class, JReflect.autobox(Short.class));
		assertSame(int.class, JReflect.autobox(Integer.class));
		assertSame(long.class, JReflect.autobox(Long.class));
		assertSame(float.class, JReflect.autobox(Float.class));
		assertSame(double.class, JReflect.autobox(Double.class));
		assertNull(JReflect.autobox(Fruit.class));
	}

	@Test
	public void testCollectTypes() {
		final Class<?>[] expectedTypeList = new Class[] { Pear.class, String.class, Object.class, Double.class };
		final Object[] objectList = new Object[] { new Pear(), "foo", null, 4d };
		assertArrayEquals(expectedTypeList, JReflect.collectTypes(objectList));
	}

	@Test
	public void testIsPackage() {
		assertTrue(JReflect.isPackage("java"));
		assertTrue(JReflect.isPackage("java.util"));
		assertTrue(JReflect.isPackage("org.codemonkey.util.reflect"));
		assertFalse(JReflect.isPackage("org.codemonkey"));
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
		Long L = 1l;
		Float F = 1f;
		Double D = 1d;
		assertSame(Short.class, JReflect.widestNumberClass(s, b));
		assertSame(Byte.class, JReflect.widestNumberClass(b, B));
		assertSame(Byte.class, JReflect.widestNumberClass(B, b));
		assertSame(Double.class, JReflect.widestNumberClass(b, s, i, l, f, d));
		assertSame(Double.class, JReflect.widestNumberClass(B, S, I, L, F, D));
		assertSame(Long.class, JReflect.widestNumberClass(L));
		assertSame(Double.class, JReflect.widestNumberClass(i, D));
	}

	@Test
	public void testReplaceInArray() {
		Integer[] initial = new Integer[] { 1, 2, 3, 4 };
		Integer[] second = JReflect.replaceInArray(initial, 2, 2);
		assertSame(initial, second);
		assertArrayEquals(new Integer[] { 1, 2, 2, 4 }, second);

	}

	/*
	 * Test classes
	 */

	static abstract class Fruit {
	}

	static class Pear extends Fruit {
	};

	static interface Foo {
		String foo(Double value, Fruit fruit, char c);
	}

	static abstract class A implements Foo {
		public A(Fruit f) {
		}
	}

	static abstract class B extends A {

		public B(Fruit f) {
			super(f);
		}
	}

	static class C extends B {
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
	}
}