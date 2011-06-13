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
		fail("Not yet implemented");
	}

	@Test
	public void testConvertNumberClassOfQ() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertBooleanClassOfQ() {
		fail("Not yet implemented");
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
