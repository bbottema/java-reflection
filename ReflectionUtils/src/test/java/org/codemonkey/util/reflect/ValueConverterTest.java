package org.codemonkey.util.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.codemonkey.util.reflect.ValueConverter.IncompatibleTypeException;
import org.junit.Test;

public class ValueConverterTest {

	enum TestEnum {
		ONE, TWO, THREE
	}

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
		for (Class<?> basicCommonType : ValueConverter.COMMON_TYPES) {
			assertContainsAll(ValueConverter.collectCompatibleTypes(basicCommonType), ValueConverter.COMMON_TYPES);
		}

		List<Class<?>> types = ValueConverter.collectCompatibleTypes(String.class);
		assertContainsAll(types, ValueConverter.COMMON_TYPES);

		types = ValueConverter.collectCompatibleTypes(boolean.class);
		assertContainsAll(types, ValueConverter.COMMON_TYPES);

		types = ValueConverter.collectCompatibleTypes(Character.class);
		assertContainsAll(types, ValueConverter.COMMON_TYPES);

		types = ValueConverter.collectCompatibleTypes(Calendar.class);
		assertEquals(1, types.size());
		assertTrue(types.contains(String.class));
	}

	private void assertContainsAll(List<Class<?>> types, List<Class<?>> basiccommontypes) {
		for (Class<?> basicCommonType : basiccommontypes) {
			assertTrue(types.contains(basicCommonType));
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
		assertNull(ValueConverter.convert((Character) null, Object.class));
		assertEquals('5', ValueConverter.convert('5', char.class));
		assertEquals("h", ValueConverter.convert('h', String.class));
		assertTrue((Boolean) ValueConverter.convert('1', Boolean.class));
		assertFalse((Boolean) ValueConverter.convert('0', Boolean.class));
		assertEquals(9, ValueConverter.convert('9', Integer.class));
		assertEquals(9, ValueConverter.convert('9', Number.class));
		assertEquals(9d, ValueConverter.convert('9', Double.class));

		try {
			ValueConverter.convert('5', Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}

		try {
			ValueConverter.convert('h', Boolean.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertStringClassOfQ() {
		assertEquals(0, ValueConverter.convert("0", Integer.class));
		assertNull(ValueConverter.convert((String) null, Integer.class));
		assertEquals(10, ValueConverter.convert("10", Integer.class));
		assertEquals(0f, ValueConverter.convert("0", Float.class));
		assertEquals(10f, ValueConverter.convert("10", Float.class));
		assertEquals(0d, ValueConverter.convert("0", double.class));
		assertEquals(10d, ValueConverter.convert("10", double.class));
		assertEquals(0, ValueConverter.convert("0", Number.class));
		assertEquals(10, ValueConverter.convert("10", Number.class));
		assertFalse((Boolean) ValueConverter.convert("0", Boolean.class));
		assertTrue((Boolean) ValueConverter.convert("1", Boolean.class));
		assertTrue((Boolean) ValueConverter.convert("true", Boolean.class));
		assertFalse((Boolean) ValueConverter.convert("false", Boolean.class));
		assertEquals('h', ValueConverter.convert("h", char.class));
		assertEquals("h", ValueConverter.convert("h", String.class));
		assertSame(TestEnum.ONE, ValueConverter.convert("ONE", TestEnum.class));
		try {
			ValueConverter.convert("h", Boolean.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConverter.convert("falsef", Boolean.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConverter.convert("h", Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConverter.convert("hello", Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConverter.convert("", int.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertEnum() {
		assertNull(ValueConverter.convertEnum(null, TestEnum.class));
		assertSame(TestEnum.ONE, ValueConverter.convertEnum("ONE", TestEnum.class));
		try {
			ValueConverter.convertEnum("5", TestEnum.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConvertNumber() {
		assertNull(ValueConverter.convertNumber(null, Integer.class));
		assertEquals(1, ValueConverter.convertNumber("1", Integer.class));
		assertEquals(1f, ValueConverter.convertNumber("1", Float.class));
		assertEquals(1d, ValueConverter.convertNumber("1", Double.class));
		assertEquals((byte) 1, ValueConverter.convertNumber("1", Byte.class));
		assertEquals(1, ValueConverter.convertNumber("1", Number.class));
		assertEquals((short) 1, ValueConverter.convertNumber("1", short.class));
		assertEquals(1l, ValueConverter.convertNumber("1", long.class));
		assertEquals(BigDecimal.valueOf(1), ValueConverter.convertNumber("1", BigDecimal.class));
		assertEquals(BigInteger.valueOf(1), ValueConverter.convertNumber("1", BigInteger.class));
		try {
			ValueConverter.convertNumber("", Integer.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConverter.convertNumber("d", Integer.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConverter.convertNumber("1", (Class<Integer>) (Object) Calendar.class);
			fail("should not be able to convert value");
		} catch (IllegalArgumentException e) {
			// OK
		}
		try {
			ValueConverter.convertNumber("1", CustomNumber.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@SuppressWarnings("serial")
	private static class CustomNumber extends Number {

		@Override
		public int intValue() {
			throw new NotImplementedException();
		}

		@Override
		public long longValue() {
			throw new NotImplementedException();
		}

		@Override
		public float floatValue() {
			throw new NotImplementedException();
		}

		@Override
		public double doubleValue() {
			throw new NotImplementedException();
		}
	}

	@Test
	public void testIsPrimitiveNumber() {
		assertFalse(ValueConverter.isPrimitiveNumber(char.class));
		assertTrue(ValueConverter.isPrimitiveNumber(int.class));
		assertTrue(ValueConverter.isPrimitiveNumber(float.class));
		assertTrue(ValueConverter.isPrimitiveNumber(double.class));
		assertTrue(ValueConverter.isPrimitiveNumber(long.class));
		assertTrue(ValueConverter.isPrimitiveNumber(byte.class));
		assertTrue(ValueConverter.isPrimitiveNumber(short.class));
		assertFalse(ValueConverter.isPrimitiveNumber(boolean.class));
		assertFalse(ValueConverter.isPrimitiveNumber(Calendar.class));
		assertFalse(ValueConverter.isPrimitiveNumber(Boolean.class));
		assertFalse(ValueConverter.isPrimitiveNumber(Character.class));
		assertFalse(ValueConverter.isPrimitiveNumber(Integer.class));
		assertFalse(ValueConverter.isPrimitiveNumber(Number.class));
	}
}