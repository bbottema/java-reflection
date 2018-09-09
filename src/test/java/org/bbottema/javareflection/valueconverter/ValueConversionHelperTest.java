package org.bbottema.javareflection.valueconverter;

import org.bbottema.javareflection.util.graph.Node;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings({"WrapperTypeMayBePrimitive", "ConstantConditions"})
public class ValueConversionHelperTest {

	@SuppressWarnings("unused")
	enum TestEnum {
		ONE, TWO, THREE
	}

	@Test
	public void testIsCommonType() {
		// basic commons
		assertTrue(ValueConversionHelper.isCommonType(String.class));
		assertTrue(ValueConversionHelper.isCommonType(Integer.class));
		assertTrue(ValueConversionHelper.isCommonType(int.class));
		assertTrue(ValueConversionHelper.isCommonType(Float.class));
		assertTrue(ValueConversionHelper.isCommonType(float.class));
		assertTrue(ValueConversionHelper.isCommonType(Double.class));
		assertTrue(ValueConversionHelper.isCommonType(double.class));
		assertTrue(ValueConversionHelper.isCommonType(Long.class));
		assertTrue(ValueConversionHelper.isCommonType(long.class));
		assertTrue(ValueConversionHelper.isCommonType(Byte.class));
		assertTrue(ValueConversionHelper.isCommonType(byte.class));
		assertTrue(ValueConversionHelper.isCommonType(Short.class));
		assertTrue(ValueConversionHelper.isCommonType(short.class));
		// limited commons
		assertTrue(ValueConversionHelper.isCommonType(Boolean.class));
		assertTrue(ValueConversionHelper.isCommonType(boolean.class));
		assertTrue(ValueConversionHelper.isCommonType(Character.class));
		assertTrue(ValueConversionHelper.isCommonType(char.class));
		// no commons
		assertFalse(ValueConversionHelper.isCommonType(Math.class));
		assertFalse(ValueConversionHelper.isCommonType(ValueConversionHelper.class));
		assertFalse(ValueConversionHelper.isCommonType(ValueConversionHelper.class));
		assertFalse(ValueConversionHelper.isCommonType(Calendar.class));
	}

	@Test
	public void testCollectCompatibleTypes() {
		// test that all commons types are convertible to all common types
		for (Class<?> basicCommonType : ValueConversionHelper.COMMON_TYPES) {
			assertContainsAllCommonTypes(ValueConversionHelper.collectRegisteredCompatibleTargetTypes(basicCommonType));
		}

		Set<Class<?>> types = ValueConversionHelper.collectRegisteredCompatibleTargetTypes(String.class);
		assertContainsAllCommonTypes(types);

		types = ValueConversionHelper.collectRegisteredCompatibleTargetTypes(boolean.class);
		assertContainsAllCommonTypes(types);

		types = ValueConversionHelper.collectRegisteredCompatibleTargetTypes(Character.class);
		assertContainsAllCommonTypes(types);

		types = ValueConversionHelper.collectRegisteredCompatibleTargetTypes(Calendar.class);
		assertEquals(1, types.size());
		assertTrue(types.contains(String.class));
	}

	private void assertContainsAllCommonTypes(Set<Class<?>> types) {
		for (Class<?> basicCommonType : ValueConversionHelper.COMMON_TYPES) {
			assertTrue(types.contains(basicCommonType));
		}
	}

	@Test
	public void testConvertObjectArrayClassOfQArray() {
		// empty list
		Object[] emptyArray = ValueConversionHelper.convert(new Object[] {}, new Class<?>[] {}, true);
		assertNotNull(emptyArray);
		assertEquals(0, emptyArray.length);
		// asymmetric list
		try {
			ValueConversionHelper.convert(new Object[] { 1, 2, 3 }, new Class<?>[] { String.class }, true);
			fail("should not accept array arguments of different lengths!");
		} catch (IllegalStateException e) {
			// OK
		}
		// list with inconvertible items, throwing exception for inconvertible values
		try {
			Calendar calendar = Calendar.getInstance();
			ValueConversionHelper.convert(new Object[] { 1, "blah", calendar }, new Class<?>[] { String.class, Integer.class, Float.class }, false);
			fail("should not accept inconvertible values!");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		// list with inconvertible items, keeping original for inconvertible values
		Calendar calendar = Calendar.getInstance();
		Object[] result = ValueConversionHelper.convert(new Object[] { 1, "blah", calendar, 0 }, new Class<?>[] { String.class, Integer.class,
				Float.class, Boolean.class }, true);
		assertNotNull(result);
		assertEquals(4, result.length);
		assertEquals("1", result[0]);
		assertEquals("blah", result[1]);
		assertSame(calendar, result[2]);
		assertTrue(result[3] instanceof Boolean);
		assertFalse((Boolean) result[3]);
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	public void testConvertObjectClassOfQ() {
		// test null value
		assertNull(ValueConversionHelper.convert((Object) null, Number.class));
		// test integer -> number (allowed)
		Integer integer = 50;
		assertSame(integer, ValueConversionHelper.convert((Object) integer, Number.class));
		// test with exact same type (allowed, should return the original value)
		Calendar calendar = Calendar.getInstance();
		assertSame(calendar, ValueConversionHelper.convert(calendar, Calendar.class));
		// test number -> integer (not allowed)
		Number number = 100.5f;
		Object o = ValueConversionHelper.convert((Object) number, Integer.class);
		assertNotSame(number, o);
		assertEquals(100, o);
		// test to string conversion
		assertEquals("a value", ValueConversionHelper.convert((Object) "a value", String.class));
		assertEquals("100.5", ValueConversionHelper.convert((Object) number, String.class));
		// test from string to anything else conversion
		assertEquals("a value", ValueConversionHelper.convert((Object) "a value", String.class));
		assertFalse((Boolean) ValueConversionHelper.convert((Object) "false", boolean.class));
		assertEquals(33f, ValueConversionHelper.convert((Object) "33", float.class));
		// test from character
		Character chara = '5';
		char charb = '8';
		assertEquals(5, ValueConversionHelper.convert((Object) chara, Number.class));
		assertEquals(8f, ValueConversionHelper.convert((Object) charb, float.class));
		// test from boolean
		Boolean boola = false;
		boolean boolb = true;
		assertEquals(0, ValueConversionHelper.convert((Object) boola, Number.class));
		assertEquals(1f, ValueConversionHelper.convert((Object) boolb, float.class));
		assertEquals("false", ValueConversionHelper.convert((Object) boola, String.class));
		assertEquals("true", ValueConversionHelper.convert((Object) boolb, String.class));
		// test for incompatibility error
		try {
			ValueConversionHelper.convert((Object) false, Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConversionHelper.convert(Calendar.getInstance(), Number.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertNumberClassOfQ() {
		assertNull(ValueConversionHelper.convert((Number) null, boolean.class));
		assertFalse((Boolean) ValueConversionHelper.convert(0, boolean.class));
		assertTrue((Boolean) ValueConversionHelper.convert(1, boolean.class));
		assertTrue((Boolean) ValueConversionHelper.convert(50, boolean.class));
		assertEquals(50f, ValueConversionHelper.convert(50, float.class));
		assertEquals(50d, ValueConversionHelper.convert(50, double.class));
		assertEquals(50L, ValueConversionHelper.convert(50, long.class));
		assertEquals(50, ValueConversionHelper.convert(50, Integer.class));
		assertEquals((byte) 50, ValueConversionHelper.convert(50, byte.class));
		assertEquals((short) 50, ValueConversionHelper.convert(50, short.class));
		assertEquals('5', ValueConversionHelper.convert(5, char.class));
		assertEquals("50", ValueConversionHelper.convert(50, String.class));

		try {
			ValueConversionHelper.convert(50, Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertBooleanClassOfQ() {
		assertNull(ValueConversionHelper.convert((Boolean) null, Calendar.class));
		assertFalse((Boolean) ValueConversionHelper.convert(false, boolean.class));
		assertTrue((Boolean) ValueConversionHelper.convert(true, boolean.class));
		assertTrue((Boolean) ValueConversionHelper.convert(true, boolean.class));
		assertEquals("true", ValueConversionHelper.convert(true, String.class));
		assertEquals("false", ValueConversionHelper.convert(false, String.class));
		assertEquals(1, ValueConversionHelper.convert(true, Integer.class));
		assertEquals(1f, ValueConversionHelper.convert(true, Float.class));
		assertEquals(1, ValueConversionHelper.convert(true, Number.class));
		assertEquals(0d, ValueConversionHelper.convert(false, double.class));
		assertEquals('0', ValueConversionHelper.convert(false, Character.class));
		assertEquals('1', ValueConversionHelper.convert(true, Character.class));

		try {
			ValueConversionHelper.convert(false, Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertCharacterClassOfQ() {
		assertNull(ValueConversionHelper.convert((Character) null, Object.class));
		assertEquals('5', ValueConversionHelper.convert('5', char.class));
		assertEquals("h", ValueConversionHelper.convert('h', String.class));
		assertTrue((Boolean) ValueConversionHelper.convert('1', Boolean.class));
		assertFalse((Boolean) ValueConversionHelper.convert('0', Boolean.class));
		assertTrue((Boolean) ValueConversionHelper.convert('h', Boolean.class));
		assertEquals(9, ValueConversionHelper.convert('9', Integer.class));
		assertEquals(9, ValueConversionHelper.convert('9', Number.class));
		assertEquals(9d, ValueConversionHelper.convert('9', Double.class));

		try {
			ValueConversionHelper.convert('5', Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertStringClassOfQ() {
		assertEquals(0, ValueConversionHelper.convert("0", Integer.class));
		assertNull(ValueConversionHelper.convert((String) null, Integer.class));
		assertEquals(10, ValueConversionHelper.convert("10", Integer.class));
		assertEquals(0f, ValueConversionHelper.convert("0", Float.class));
		assertEquals(10f, ValueConversionHelper.convert("10", Float.class));
		assertEquals(0d, ValueConversionHelper.convert("0", double.class));
		assertEquals(10d, ValueConversionHelper.convert("10", double.class));
		assertEquals(0, ValueConversionHelper.convert("0", Number.class));
		assertEquals(10, ValueConversionHelper.convert("10", Number.class));
		assertFalse((Boolean) ValueConversionHelper.convert("0", Boolean.class));
		assertTrue((Boolean) ValueConversionHelper.convert("1", Boolean.class));
		assertTrue((Boolean) ValueConversionHelper.convert("true", Boolean.class));
		assertFalse((Boolean) ValueConversionHelper.convert("false", Boolean.class));
		assertEquals('h', ValueConversionHelper.convert("h", char.class));
		assertEquals("h", ValueConversionHelper.convert("h", String.class));
		assertSame(TestEnum.ONE, ValueConversionHelper.convert("ONE", TestEnum.class));
		assertTrue((Boolean) ValueConversionHelper.convert("h", Boolean.class));
		try {
			ValueConversionHelper.convert("falsef", Boolean.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConversionHelper.convert("h", Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConversionHelper.convert("hello", Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConversionHelper.convert("", int.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@Test
	public void testConvertEnum() {
		assertNull(ValueConversionHelper.convert(null, TestEnum.class));
		assertSame(TestEnum.ONE, ValueConversionHelper.convert("ONE", TestEnum.class));
		try {
			ValueConversionHelper.convert("5", TestEnum.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConvertNumber() {
		assertNull(ValueConversionHelper.convert(null, Integer.class));
		assertEquals(1, ValueConversionHelper.convert("1", Integer.class));
		assertEquals(1f, ValueConversionHelper.convert("1", Float.class));
		assertEquals(1d, ValueConversionHelper.convert("1", Double.class));
		assertEquals((byte) 1, ValueConversionHelper.convert("1", Byte.class));
		assertEquals(1, ValueConversionHelper.convert("1", Number.class));
		assertEquals((short) 1, ValueConversionHelper.convert("1", short.class));
		assertEquals(1L, ValueConversionHelper.convert("1", long.class));
		assertEquals(BigDecimal.valueOf(1), ValueConversionHelper.convert("1", BigDecimal.class));
		assertEquals(BigInteger.valueOf(1), ValueConversionHelper.convert("1", BigInteger.class));
		try {
			ValueConversionHelper.convert("", Integer.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConversionHelper.convert("d", Integer.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConversionHelper.convert("1", (Class<Integer>) (Object) Calendar.class);
			fail("should not be able to convert value");
		} catch (IllegalArgumentException e) {
			// OK
		}
		try {
			ValueConversionHelper.convert("1", CustomNumber.class);
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

	/**
	 * Test for {@link ValueConversionHelper#isPrimitiveNumber(Class)}.
	 */
	@Test
	public void testIsPrimitiveNumber() {
		assertFalse(ValueConversionHelper.isPrimitiveNumber(char.class));
		assertTrue(ValueConversionHelper.isPrimitiveNumber(int.class));
		assertTrue(ValueConversionHelper.isPrimitiveNumber(float.class));
		assertTrue(ValueConversionHelper.isPrimitiveNumber(double.class));
		assertTrue(ValueConversionHelper.isPrimitiveNumber(long.class));
		assertTrue(ValueConversionHelper.isPrimitiveNumber(byte.class));
		assertTrue(ValueConversionHelper.isPrimitiveNumber(short.class));
		assertFalse(ValueConversionHelper.isPrimitiveNumber(boolean.class));
		assertFalse(ValueConversionHelper.isPrimitiveNumber(Calendar.class));
		assertFalse(ValueConversionHelper.isPrimitiveNumber(Boolean.class));
		assertFalse(ValueConversionHelper.isPrimitiveNumber(Character.class));
		assertFalse(ValueConversionHelper.isPrimitiveNumber(Integer.class));
		assertFalse(ValueConversionHelper.isPrimitiveNumber(Number.class));
	}
	
	@Test
	public void testCollectTypeCompatibleNodes() {
		ValueConversionHelper.registerValueConverter(new DummyValueConverter(Fruit.class, Vehicle.class));
		ValueConversionHelper.registerValueConverter(new DummyValueConverter(Fruit.class, Car.class));
		ValueConversionHelper.registerValueConverter(new DummyValueConverter(Fruit.class, Leon.class));
		ValueConversionHelper.registerValueConverter(new DummyValueConverter(Vehicle.class, Fruit.class));
		ValueConversionHelper.registerValueConverter(new DummyValueConverter(Car.class, Apple.class));
		ValueConversionHelper.registerValueConverter(new DummyValueConverter(Leon.class, Elstar.class));
		
		Set<Node<Class<?>>> result = ValueConversionHelper.collectTypeCompatibleNodes(Car.class);
		
		assertThat(result).containsExactlyInAnyOrder(
				new Node<Class<?>>(Car.class),
				new Node<Class<?>>(Leon.class)
		);
	}
	
	class Fruit{}
	class Apple extends Fruit{}
	class Elstar extends Apple{}
	
	class Vehicle{}
	class Car extends Vehicle{}
	class Leon extends Car{}
	
	@SuppressWarnings("unchecked")
	public static class DummyValueConverter implements ValueFunction<Object, Object> {
		
		private final Class<?> fromType;
		private final Class<?> targetType;
		
		public DummyValueConverter(Class<?> fromType, Class<?> targetType) {
			this.fromType = fromType;
			this.targetType = targetType;
		}
		
		@Nonnull
		@Override
		public Class<Object> getFromType() {
			return (Class<Object>) fromType;
		}
		
		@Nonnull
		@Override
		public Class<Object> getTargetType() {
			return (Class<Object>) targetType;
		}
		
		@Nonnull
		@Override
		public Class<Object> convertValue(@Nonnull Object value) {
			throw new AssertionError("This method should not be used");
		}
	}
}