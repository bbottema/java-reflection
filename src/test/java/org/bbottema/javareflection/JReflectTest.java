package org.bbottema.javareflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;

import org.bbottema.javareflection.JReflect.LookupMode;
import org.bbottema.javareflection.valueconverter.IncompatibleTypeException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Junit test for {@link JReflect}.
 */
public class JReflectTest {
	
	@Before
	public void resetStaticCaches() {
		JReflect.resetCaches();
	}
	
	/**
	 * Test for {@link JReflect#newInstanceSimple(Class)}.
	 * <p>
	 * We'll ignore all the catch exceptions here, which are not there to provide function: only to hide boilerplate code). Just test happy
	 * flow here.
	 */
	@Test
	public void testNewInstanceSimple() {
		assertSame(Object.class, JReflect.newInstanceSimple(Object.class).getClass());
	}

	/**
	 * Test for {@link JReflect#findCompatibleMethod(Class, String, EnumSet, Class...)}.
	 * 
	 * @throws NoSuchMethodException Thrown by tested method.
	 */
	@Test
	public void testFindCompatibleMethod()
			throws NoSuchMethodException {
		// find method through interface on superclass, using autoboxing, class casting and an auto convert
		Method m = JReflect.findCompatibleMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Pear.class, String.class);
		assertNotNull(m);
		assertEquals(Foo.class.getMethod("foo", Double.class, Fruit.class, char.class), m);
		Method m2 = JReflect.findCompatibleMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Pear.class, String.class);
		assertNotNull(m2);
		assertSame(m, m2);
		// find the same method, but now the first implementation on C should be returned
		m = JReflect.findCompatibleMethod(C.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Pear.class, String.class);
		assertNotNull(m);
		assertEquals(C.class.getMethod("foo", Double.class, Fruit.class, char.class), m);
		// find a String method
		m = JReflect.findCompatibleMethod(String.class, "concat", EnumSet.noneOf(LookupMode.class), String.class);
		assertNotNull(m);
		assertEquals(String.class.getMethod("concat", String.class), m);
		// shouldn't be able to find the following methods
		try {
			JReflect.findCompatibleMethod(B.class, "foos", EnumSet.allOf(LookupMode.class), double.class, Pear.class, String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			JReflect.findCompatibleMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, String.class, String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			JReflect.findCompatibleMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Fruit.class, Math.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}

	/**
	 * Test for {@link JReflect#findCompatibleConstructor(Class, EnumSet, Class...)}.
	 * 
	 * @throws NoSuchMethodException Thrown by tested method.
	 */
	@Test
	public void testFindCompatibleConstructor()
			throws NoSuchMethodException {
		// find constructor on superclass, using autoboxing
		Constructor<?> m = JReflect.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), Fruit.class);
		assertNotNull(m);
		assertEquals(B.class.getConstructor(Fruit.class), m);
		Constructor<?> m2 = JReflect.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), Fruit.class);
		assertNotNull(m2);
		assertSame(m, m2);
		// find constructor on superclass, using autoboxing and class casting
		m = JReflect.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), Pear.class);
		assertNotNull(m);
		assertEquals(B.class.getConstructor(Fruit.class), m);
		// still find constructor on superclass
		m = JReflect.findCompatibleConstructor(C.class, EnumSet.allOf(LookupMode.class), Fruit.class);
		assertNotNull(m);
		assertEquals(C.class.getConstructor(Fruit.class), m);
		// still find constructor on subclass
		m = JReflect.findCompatibleConstructor(C.class, EnumSet.allOf(LookupMode.class), Pear.class);
		assertNotNull(m);
		assertEquals(C.class.getConstructor(Pear.class), m);
		// find a String constructor
		m = JReflect.findCompatibleConstructor(String.class, EnumSet.noneOf(LookupMode.class), String.class);
		assertNotNull(m);
		assertEquals(String.class.getConstructor(String.class), m);
		// shouldn't be able to find the following methods
		try {
			JReflect.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), double.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			JReflect.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}

	/**
	 * Test for {@link JReflect#invokeCompatibleMethod(Object, Class, String, Object...)}.
	 * 
	 * @throws NoSuchMethodException Thrown by tested method.
	 * @throws IllegalArgumentException Thrown by tested method.
	 * @throws IllegalAccessException Thrown by tested method.
	 * @throws InvocationTargetException Thrown by tested method.
	 */
	@Test
	public void testInvokeCompatibleMethod()
			throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		JReflect.invokeCompatibleMethod(new C(new Pear()), B.class, "foo", 50d, new Pear(), "g");
		JReflect.invokeCompatibleMethod(new C(new Pear()), C.class, "foo", 50d, new Pear(), "g");
		JReflect.invokeCompatibleMethod("", String.class, "concat", String.class);
		// shouldn't be able to find the following methods
		try {
			JReflect.invokeCompatibleMethod(new C(new Pear()), C.class, "foos", 50d, new Pear(), "g");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			JReflect.invokeCompatibleMethod(new C(new Pear()), C.class, "foo", 50d, "foobar", "g");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			JReflect.invokeCompatibleMethod(new C(new Pear()), C.class, "foo", 50d, new Pear(), Calendar.getInstance());
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}

	/**
	 * Test for {@link JReflect#invokeCompatibleConstructor(Class, Object...)}.
	 * 
	 * @throws NoSuchMethodException Thrown by tested method.
	 * @throws IllegalArgumentException Thrown by tested method.
	 * @throws IllegalAccessException Thrown by tested method.
	 * @throws InvocationTargetException Thrown by tested method.
	 * @throws InstantiationException Thrown by tested method.
	 */
	@Test
	public void testInvokeCompatibleConstructor()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		try {
			JReflect.invokeCompatibleConstructor(B.class, new Pear());
		} catch (InstantiationException e) {
			// Ok
		}
		JReflect.invokeCompatibleConstructor(C.class, new Pear());
		JReflect.invokeCompatibleConstructor(String.class, "test string");
		JReflect.invokeCompatibleConstructor(String.class, 1234567);
		// shouldn't be able to find the following methods
		try {
			JReflect.invokeCompatibleConstructor(B.class, 50d);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			JReflect.invokeCompatibleConstructor(B.class, "foobar");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}

	/**
	 * Test for {@link JReflect#autobox(Class)}.
	 */
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

	/**
	 * Test for {@link JReflect#collectTypes(Object[])}.
	 */
	@Test
	public void testCollectTypes() {
		final Class<?>[] expectedTypeList = new Class<?>[] { Pear.class, String.class, Object.class, Double.class };
		final Object[] objectList = new Object[] { new Pear(), "foo", null, 4d };
		assertArrayEquals(expectedTypeList, JReflect.collectTypes(objectList));
	}

	/**
	 * Test for {@link JReflect#isPackage(String)}.
	 */
	@Test
	public void testIsPackage() {
		assertTrue(JReflect.isPackage("java"));
		assertTrue(JReflect.isPackage("java.util"));
		assertTrue(JReflect.isPackage("org.bbottema.javareflection"));
		assertTrue(JReflect.isPackage("java.lang.reflect"));
		assertTrue(JReflect.isPackage("org.junit"));
		assertFalse(JReflect.isPackage("donkey.cake"));
		assertFalse(JReflect.isPackage("org.bbottema"));
	}

	/**
	 * Test for {@link JReflect#widestNumberClass(Number...)}.
	 */
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
		assertSame(Short.class, JReflect.widestNumberClass(s, b));
		assertSame(Byte.class, JReflect.widestNumberClass(b, B));
		assertSame(Byte.class, JReflect.widestNumberClass(B, b));
		assertSame(Integer.class, JReflect.widestNumberClass(b, s, i));
		assertSame(Double.class, JReflect.widestNumberClass(b, s, i, l, f, d));
		assertSame(Double.class, JReflect.widestNumberClass(B, S, I, L, F, D));
		assertSame(Long.class, JReflect.widestNumberClass(L));
		assertSame(Double.class, JReflect.widestNumberClass(i, D));
	}

	/**
	 * Test for {@link JReflect#replaceInArray(Object[], int, Object)}.
	 */
	@Test
	public void testReplaceInArray() {
		Integer[] initial = new Integer[] { 1, 2, 3, 4 };
		Integer[] second = JReflect.replaceInArray(initial, 2, 2);
		assertSame(initial, second);
		assertArrayEquals(new Integer[] { 1, 2, 2, 4 }, second);
	}

	/**
	 * Test for {@link JReflect#assignToField(Object, String, Object)}.
	 * 
	 * @throws IllegalAccessException Thrown by tested method.
	 * @throws NoSuchFieldException Thrown by tested method.
	 */
	@Test
	public void testAssignToField()
			throws IllegalAccessException, NoSuchFieldException {
		assertEquals(50, JReflect.assignToField(new C(new Pear()), "numberB", 50));
		assertEquals(50, JReflect.assignToField(new C(new Pear()), "numberB", "50"));
		assertEquals(50, JReflect.assignToField(new C(new Pear()), "numberB", 50d));
		try {
			JReflect.assignToField(new C(new Pear()), "numberB", new Pear());
			fail("IllegalAccessException expected due to incompatible types");
		} catch (IncompatibleTypeException e) {
			// ok
		}
		try {
			JReflect.assignToField(new C(new Pear()), "number_privateB", new Pear());
			fail("IllegalAccessException expected due to incompatible types");
		} catch (NoSuchFieldException e) {
			// ok
		}
	}

	/**
	 * Test for {@link JReflect#collectProperties(Object)}.
	 */
	@Test
	public void testCollectProperties() {
		Collection<String> properties = JReflect.collectProperties(new C(new Pear()));
		assertNotNull(properties);
		assertEquals(4, properties.size());
		assertTrue(properties.contains("numberA"));
		assertTrue(properties.contains("numberB"));
		assertTrue(properties.contains("numberB_static"));
		assertTrue(properties.contains("numberC"));
	}

    /**
     * Test for {@link JReflect#collectMethods(Object, boolean)}.
     */
    @Test
    public void testCollectPublicMethods() {
        Collection<String> oProperties = JReflect.collectMethods(new Object(), true);
        Collection<String> cProperties = JReflect.collectMethods(new C(new Pear()), true);
        assertNotNull(oProperties);
        assertTrue(oProperties.size() > 0);
        assertEquals(oProperties.size() + 1, cProperties.size());
        cProperties.removeAll(oProperties);
        assertEquals(1, cProperties.size());
        assertTrue(cProperties.contains("foo"));
    }

    /**
     * Test for {@link JReflect#collectMethods(Object, boolean)}.
     */
    @Test
    public void testCollectAllMethods() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Object o = new Object();
        final C c = new C(new Pear());
        Collection<String> oProperties = JReflect.collectMethods(o, false);
        Collection<String> cProperties = JReflect.collectMethods(c, false);
        assertNotNull(oProperties);
        assertTrue(oProperties.size() > 0);
        assertEquals(oProperties.size() + 3, cProperties.size());
        cProperties.removeAll(oProperties);
        assertEquals(3, cProperties.size());
        assertTrue(cProperties.contains("foo"));
        assertTrue(cProperties.contains("protectedMethod"));
        assertTrue(cProperties.contains("privateMethod"));
        JReflect.invokeCompatibleMethod(c, C.class, "privateMethod");
    }

    /**
     * Test for {@link JReflect#collectMethods(Object, boolean)}.
     */
    @Test
    public void testInvokeMethods() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final C c = new C(new Pear());
        assertEquals("private 1", JReflect.invokeCompatibleMethod(c, A.class, "privateMethod"));
        assertEquals("protected 2", JReflect.invokeCompatibleMethod(c, B.class, "protectedMethod"));
        assertEquals("private 2", JReflect.invokeCompatibleMethod(c, C.class, "privateMethod"));
        assertEquals("protected 2", JReflect.invokeCompatibleMethod(c, C.class, "protectedMethod"));
    }

	/**
	 * Test for {@link JReflect#solveField(Object, String)}.
	 * 
	 * @throws NoSuchFieldException Thrown by tested method.
	 */
	@Test
	public void testSolveField()
			throws NoSuchFieldException {
		Field f1 = JReflect.solveField(new C(new Pear()), "numberB");
		assertNotNull(f1);
		assertEquals(C.class.getField("numberB"), f1);
		Field f2 = JReflect.solveField(C.class, "numberB_static");
		assertNotNull(f2);
		assertEquals(C.class.getField("numberB_static"), f2);
	}

	/**
	 * Test for {@link JReflect#locateClass(String, boolean, ExternalClassLoader)}.
	 */
	@Test
	public void testLocateClass() {
		assertSame(Math.class, JReflect.locateClass("Math", false, null));
		assertNull(JReflect.locateClass("Mathh", false, null));
		assertNull(JReflect.locateClass("JReflect", false, null));
		assertSame(JReflect.class, JReflect.locateClass("JReflect", true, null));
		assertNull(JReflect.locateClass("Socket", false, null));
		assertSame(Socket.class, JReflect.locateClass("Socket", true, null));
	}

	/*
	 * Test classes
	 */

	static abstract class Fruit {
	}

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