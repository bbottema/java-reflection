package org.codemonkey.util.reflect;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.apache.commons.lang.ArrayUtils;
import org.codemonkey.util.reflect.ValueConverter.IncompatibleTypeException;
import org.junit.Test;

public class ValueConverterTest {

	@Test
	public void testIsCommonType() {
		// basic commons
		assertTrue(ValueConverter.isCommonType(String.class));
		assertTrue(ValueConverter.isCommonType(Integer.class));
		assertTrue(ValueConverter.isCommonType(int.class));
		assertTrue(ValueConverter.isCommonType(Float.class));
		assertTrue(ValueConverter.isCommonType(float.class));
		assertTrue(ValueConverter.isCommonType(Double.class));
		assertTrue(ValueConverter.isCommonType(double.class));
		assertTrue(ValueConverter.isCommonType(Long.class));
		assertTrue(ValueConverter.isCommonType(long.class));
		assertTrue(ValueConverter.isCommonType(Byte.class));
		assertTrue(ValueConverter.isCommonType(byte.class));
		assertTrue(ValueConverter.isCommonType(Short.class));
		assertTrue(ValueConverter.isCommonType(short.class));
		// limited commons
		assertTrue(ValueConverter.isCommonType(Boolean.class));
		assertTrue(ValueConverter.isCommonType(boolean.class));
		assertTrue(ValueConverter.isCommonType(Character.class));
		assertTrue(ValueConverter.isCommonType(char.class));
		// no commons
		assertFalse(ValueConverter.isCommonType(Math.class));
		assertFalse(ValueConverter.isCommonType(ValueConverter.class));
		assertFalse(ValueConverter.isCommonType(ValueConverter.class));
		assertFalse(ValueConverter.isCommonType(Calendar.class));
	}

	@Test
	public void testCollectCompatibleTypes() {
		// test that all commons types are convertible to all common types
		for (Class<?> basicCommonType : ValueConverter.basicCommonTypes) {
			assertContainsAll(ValueConverter.collectCompatibleTypes(basicCommonType), ValueConverter.basicCommonTypes);
		}

		Class<?>[] types = ValueConverter.collectCompatibleTypes(String.class);
		assertContainsAll(types, ValueConverter.basicCommonTypes);
		ArrayUtils.contains(types, char.class);
		ArrayUtils.contains(types, Character.class);
		ArrayUtils.contains(types, boolean.class);
		ArrayUtils.contains(types, Boolean.class);

		types = ValueConverter.collectCompatibleTypes(boolean.class);
		assertContainsAll(types, ValueConverter.basicCommonTypes);
		ArrayUtils.contains(types, char.class);
		ArrayUtils.contains(types, Character.class);

		types = ValueConverter.collectCompatibleTypes(Character.class);
		assertContainsAll(types, ValueConverter.basicCommonTypes);
		ArrayUtils.contains(types, boolean.class);
		ArrayUtils.contains(types, Boolean.class);

		types = ValueConverter.collectCompatibleTypes(Calendar.class);
		assertEquals(1, types.length);
		ArrayUtils.contains(types, String.class);
	}

	private void assertContainsAll(Class<?>[] types, Class<?>[] basiccommontypes) {
		for (Class<?> basicCommonType : basiccommontypes) {
			assertTrue(ArrayUtils.contains(types, basicCommonType));
		}
	}

	@Test
	public void testConvertObjectArrayClassOfQArray() {
		// empty list
		Object[] emptyArray = ValueConverter.convert(new Object[] {}, new Class[] {}, true);
		assertNotNull(emptyArray);
		assertEquals(0, emptyArray.length);
		// asymmetric list
		try {
			ValueConverter.convert(new Object[] { 1, 2, 3 }, new Class[] { String.class }, true);
			fail("should not accept array arguments of different lengths!");
		} catch (IllegalStateException e) {
			// OK
		}
		// list with inconvertible items, throwing exception for inconvertible values
		try {
			Calendar calendar = Calendar.getInstance();
			ValueConverter.convert(new Object[] { 1, "blah", calendar }, new Class[] { String.class, Integer.class, Float.class }, false);
			fail("should not accept inconvertible values!");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		// list with inconvertible items, keeping original for inconvertible values
		Calendar calendar = Calendar.getInstance();
		Object[] result = ValueConverter.convert(new Object[] { 1, "blah", calendar, 0 }, new Class[] { String.class, Integer.class,
				Float.class, Boolean.class }, true);
		assertNotNull(result);
		assertEquals(4, result.length);
		assertEquals("1", result[0]);
		assertEquals("blah", result[1]);
		assertSame(calendar, result[2]);
		assertTrue(result[3] instanceof Boolean);
		assertFalse((Boolean) result[3]);
	}

	@Test
	public void testConvertObjectClassOfQ() {
		// test null value
		assertNull(ValueConverter.convert((Object) null, Number.class));
		// test integer -> number (allowed)
		Integer integer = 50;
		assertSame(integer, ValueConverter.convert((Object) integer, Number.class));
		// test with exact same type (allowed, should return the original value)
		Calendar calendar = Calendar.getInstance();
		assertSame(calendar, ValueConverter.convert((Object) calendar, Calendar.class));
		// test number -> integer (not allowed)
		Number number = 100.5f;
		Object o = ValueConverter.convert((Object) number, Integer.class);
		assertNotSame(number, o);
		assertEquals(100, o);
		// test to string conversion
		assertEquals("a value", ValueConverter.convert((Object) "a value", String.class));
		assertEquals("100.5", ValueConverter.convert((Object) number, String.class));
		// test from string to anything else conversion
		assertEquals("a value", ValueConverter.convert((Object) "a value", String.class));
		assertFalse((Boolean) ValueConverter.convert((Object) "false", boolean.class));
		assertEquals(33f, ValueConverter.convert((Object) "33", float.class));
		// test from character
		Character chara = '5';
		char charb = '8';
		assertEquals(5, ValueConverter.convert((Object) chara, Number.class));
		assertEquals(8f, ValueConverter.convert((Object) charb, float.class));
		// test from boolean
		Boolean boola = false;
		boolean boolb = true;
		assertEquals(0, ValueConverter.convert((Object) boola, Number.class));
		assertEquals(1f, ValueConverter.convert((Object) boolb, float.class));
		assertEquals("false", ValueConverter.convert((Object) boola, String.class));
		assertEquals("true", ValueConverter.convert((Object) boolb, String.class));
		// test for incompatibility error
		try {
			ValueConverter.convert((Object) false, Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConverter.convert((Object) Calendar.getInstance(), Number.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertNumberClassOfQ() {
		assertNull(ValueConverter.convert((Number) null, boolean.class));
		assertFalse((Boolean) ValueConverter.convert(0, boolean.class));
		assertTrue((Boolean) ValueConverter.convert(1, boolean.class));
		assertTrue((Boolean) ValueConverter.convert(50, boolean.class));
		assertEquals(50f, ValueConverter.convert(50, float.class));
		assertEquals(50d, ValueConverter.convert(50, double.class));
		assertEquals(50l, ValueConverter.convert(50, long.class));
		assertEquals(50, ValueConverter.convert(50, Integer.class));
		assertEquals((byte) 50, ValueConverter.convert(50, byte.class));
		assertEquals((short) 50, ValueConverter.convert(50, short.class));
		assertEquals('5', ValueConverter.convert(5, char.class));
		assertEquals("50", ValueConverter.convert(50, String.class));

		try {
			ValueConverter.convert(50, Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertBooleanClassOfQ() {
		assertNull(ValueConverter.convert((Boolean) null, Calendar.class));
		assertFalse((Boolean) ValueConverter.convert(false, boolean.class));
		assertTrue((Boolean) ValueConverter.convert(true, boolean.class));
		assertTrue((Boolean) ValueConverter.convert(true, boolean.class));
		assertEquals("true", ValueConverter.convert(true, String.class));
		assertEquals("false", ValueConverter.convert(false, String.class));
		assertEquals(1, ValueConverter.convert(true, Integer.class));
		assertEquals(1f, ValueConverter.convert(true, Float.class));
		assertEquals(1, ValueConverter.convert(true, Number.class));
		assertEquals(0d, ValueConverter.convert(false, double.class));
		assertEquals('0', ValueConverter.convert(false, Character.class));
		assertEquals('1', ValueConverter.convert(true, Character.class));

		try {
			ValueConverter.convert(false, Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertCharacterClassOfQ() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertStringClassOfQ() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertEnum() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertNumber() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsPrimitiveNumber() {
		fail("Not yet implemented");
	}

}
