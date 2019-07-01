package org.bbottema.javareflection.valueconverter;

import org.bbottema.javareflection.util.Function;
import org.bbottema.javareflection.util.graph.Node;
import org.bbottema.javareflection.valueconverter.ValueFunction.ValueFunctionImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SuppressWarnings({"WrapperTypeMayBePrimitive", "ConstantConditions"})
public class ValueConversionHelperTest {
	
	private static final Set<Class<?>> COMMON_TYPES = new HashSet<Class<?>>(asList(String.class, Integer.class, int.class, Float.class,
			float.class, Double.class, double.class, Long.class, long.class, Byte.class, byte.class, Short.class, short.class,
			Boolean.class, boolean.class, Character.class, char.class));
	
	@SuppressWarnings("unused")
	enum TestEnum {
		ONE, TWO, THREE
	}
	
	@Before
	public void clearRuntimeTypes() {
		ValueConversionHelper.resetDefaultConverters();
	}
	
	@Test
	public void testIsCommonType() {
		// basic commons
		assertThat(ValueConversionHelper.isCommonType(String.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(Integer.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(int.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(Float.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(float.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(Double.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(double.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(Long.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(long.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(Byte.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(byte.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(Short.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(short.class)).isTrue();
		// limited commons
		assertThat(ValueConversionHelper.isCommonType(Boolean.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(boolean.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(Character.class)).isTrue();
		assertThat(ValueConversionHelper.isCommonType(char.class)).isTrue();
		// no commons
		assertThat(ValueConversionHelper.isCommonType(Math.class)).isFalse();
		assertThat(ValueConversionHelper.isCommonType(ValueConversionHelper.class)).isFalse();
		assertThat(ValueConversionHelper.isCommonType(ValueConversionHelper.class)).isFalse();
		assertThat(ValueConversionHelper.isCommonType(Calendar.class)).isFalse();
	}
	
	@Test
	public void testCollectCompatibleTypes() {
		// test that all commons types are convertible to all common types
		for (Class<?> basicCommonType : COMMON_TYPES) {
			assertThat(ValueConversionHelper.collectCompatibleTargetTypes(basicCommonType)).containsAll(COMMON_TYPES);
		}
		
		Set<Class<?>> types = ValueConversionHelper.collectCompatibleTargetTypes(String.class);
		assertThat(types).containsAll(COMMON_TYPES);
		
		types = ValueConversionHelper.collectCompatibleTargetTypes(boolean.class);
		assertThat(types).containsAll(COMMON_TYPES);
		
		types = ValueConversionHelper.collectCompatibleTargetTypes(Character.class);
		assertThat(types).containsAll(COMMON_TYPES);
		
		types = ValueConversionHelper.collectRegisteredCompatibleTargetTypes(Calendar.class);
		assertThat(types).containsExactly(Calendar.class);
		types = ValueConversionHelper.collectCompatibleTargetTypes(Calendar.class);
		assertThat(types.size()).isGreaterThan(10); // number depends on order of junit execution (due to dynamic runtime type registration)
		assertThat(types).contains(String.class);
	}
	
	@Test
	public void testConvertObjectArrayClassOfQArray() {
		// empty list
		Object[] emptyArray = ValueConversionHelper.convert(new Object[] {}, new Class<?>[] {}, true);
		assertThat(emptyArray).isNotNull();
		assertThat(emptyArray).isEmpty();
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
		assertThat(result).isNotNull();
		assertThat(result.length).isEqualTo(4);
		assertThat(result[0]).isEqualTo("1");
		assertThat(result[1]).isEqualTo("blah");
		assertThat(result[2]).isEqualTo(calendar);
		assertThat(result[3] instanceof Boolean).isTrue();
		assertThat((Boolean) result[3]).isFalse();
	}
	
	@SuppressWarnings("ConstantConditions")
	@Test
	public void testConvertObjectClassOfQ() {
		// test null value
		assertThat(ValueConversionHelper.convert(null, Number.class)).isNull();
		// test integer -> number (allowed)
		Integer integer = 50;
		assertThat(ValueConversionHelper.convert(integer, Number.class)).isEqualTo(integer);
		// test with exact same type (allowed, should return the original value)
		Calendar calendar = Calendar.getInstance();
		assertThat(ValueConversionHelper.convert(calendar, Calendar.class)).isEqualTo(calendar);
		// test number -> integer (not allowed)
		Number number = 100.5f;
		Object o = ValueConversionHelper.convert(number, Integer.class);
		assertThat(o).isNotEqualTo(number);
		assertThat(o).isEqualTo(100);
		// test to string conversion
		assertThat(ValueConversionHelper.convert("a value", String.class)).isEqualTo("a value");
		assertThat(ValueConversionHelper.convert(number, String.class)).isEqualTo("100.5");
		// test from string to anything else conversion
		assertThat(ValueConversionHelper.convert("a value", String.class)).isEqualTo("a value");
		assertThat(ValueConversionHelper.convert("false", boolean.class)).isFalse();
		assertThat((Object) ValueConversionHelper.convert("33", float.class)).isEqualTo(33f);
		// test from character
		Character chara = '5';
		char charb = '8';
		assertThat(ValueConversionHelper.convert(chara, Number.class).intValue()).isEqualTo(5);
		assertThat((Object) ValueConversionHelper.convert(charb, float.class)).isEqualTo(8f);
		// test from boolean
		Boolean boola = false;
		boolean boolb = true;
		assertThat(ValueConversionHelper.convert(boola, Number.class).intValue()).isEqualTo(0);
		assertThat((Object) ValueConversionHelper.convert(boolb, float.class)).isEqualTo(1f);
		assertThat(ValueConversionHelper.convert(boola, String.class)).isEqualTo("false");
		assertThat(ValueConversionHelper.convert(boolb, String.class)).isEqualTo("true");
		// test for incompatibility error
		try {
			ValueConversionHelper.convert(false, Calendar.class);
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
		assertThat(ValueConversionHelper.convert(null, boolean.class)).isNull();
		assertThat(ValueConversionHelper.convert(0, boolean.class)).isFalse();
		assertThat(ValueConversionHelper.convert(1, boolean.class)).isTrue();
		assertThat(ValueConversionHelper.convert(50, boolean.class)).isTrue();
		assertThat((Object) ValueConversionHelper.convert(50, float.class)).isEqualTo(50f);
		assertThat((Object) ValueConversionHelper.convert(50, double.class)).isEqualTo(50d);
		assertThat((Object) ValueConversionHelper.convert(50, long.class)).isEqualTo(50L);
		assertThat((Object) ValueConversionHelper.convert(50, Integer.class)).isEqualTo(50);
		assertThat((Object) ValueConversionHelper.convert(50, byte.class)).isEqualTo((byte) 50);
		assertThat((Object) ValueConversionHelper.convert(50, short.class)).isEqualTo((short) 50);
		assertThat((Object) ValueConversionHelper.convert(5, char.class)).isEqualTo('5');
		assertThat(ValueConversionHelper.convert(50, String.class)).isEqualTo("50");
		
		try {
			ValueConversionHelper.convert(50, Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}
	
	@Test
	public void testConvertBooleanClassOfQ() {
		assertThat(ValueConversionHelper.convert(null, Calendar.class)).isNull();
		assertThat(ValueConversionHelper.convert(false, boolean.class)).isFalse();
		assertThat(ValueConversionHelper.convert(true, boolean.class)).isTrue();
		assertThat(ValueConversionHelper.convert(true, boolean.class)).isTrue();
		assertThat(ValueConversionHelper.convert(true, String.class)).isEqualTo("true");
		assertThat(ValueConversionHelper.convert(false, String.class)).isEqualTo("false");
		assertThat((Object) ValueConversionHelper.convert(true, Integer.class)).isEqualTo(1);
		assertThat((Object) ValueConversionHelper.convert(true, Float.class)).isEqualTo(1f);
		assertThat(ValueConversionHelper.convert(true, Number.class).intValue()).isEqualTo(1);
		assertThat((Object) ValueConversionHelper.convert(false, double.class)).isEqualTo(0d);
		assertThat((Object) ValueConversionHelper.convert(false, Character.class)).isEqualTo('0');
		assertThat((Object) ValueConversionHelper.convert(true, Character.class)).isEqualTo('1');
		
		try {
			ValueConversionHelper.convert(false, Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}
	
	@Test
	public void testConvertCharacterClassOfQ() {
		assertThat(ValueConversionHelper.convert(null, Object.class)).isNull();
		assertThat((Object) ValueConversionHelper.convert('5', char.class)).isEqualTo('5');
		assertThat(ValueConversionHelper.convert('h', String.class)).isEqualTo("h");
		assertThat(ValueConversionHelper.convert('1', Boolean.class)).isTrue();
		assertThat(ValueConversionHelper.convert('0', Boolean.class)).isFalse();
		assertThat(ValueConversionHelper.convert('h', Boolean.class)).isTrue();
		assertThat((Object) ValueConversionHelper.convert('9', Integer.class)).isEqualTo(9);
		assertThat(ValueConversionHelper.convert('9', Number.class).intValue()).isEqualTo(9);
		assertThat((Object) ValueConversionHelper.convert('9', Double.class)).isEqualTo(9d);
		assertThat((Object) ValueConversionHelper.convert("d", Integer.class)).isEqualTo(100);
		
		try {
			ValueConversionHelper.convert('5', Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
	}
	
	@Test
	public void testConvertStringClassOfQ() {
		assertThat((Object) ValueConversionHelper.convert("0", Integer.class)).isEqualTo(0);
		assertThat(ValueConversionHelper.convert(null, Integer.class)).isNull();
		assertThat((Object) ValueConversionHelper.convert("10", Integer.class)).isEqualTo(10);
		assertThat((Object) ValueConversionHelper.convert("0", Float.class)).isEqualTo(0f);
		assertThat((Object) ValueConversionHelper.convert("10", Float.class)).isEqualTo(10f);
		assertThat((Object) ValueConversionHelper.convert("0", double.class)).isEqualTo(0d);
		assertThat((Object) ValueConversionHelper.convert("10", double.class)).isEqualTo(10d);
		assertThat(ValueConversionHelper.convert("0", Number.class).intValue()).isEqualTo(0);
		assertThat(ValueConversionHelper.convert("10", Number.class).intValue()).isEqualTo(10);
		assertThat(ValueConversionHelper.convert("0", Boolean.class)).isFalse();
		assertThat(ValueConversionHelper.convert("1", Boolean.class)).isTrue();
		assertThat(ValueConversionHelper.convert("true", Boolean.class)).isTrue();
		assertThat(ValueConversionHelper.convert("false", Boolean.class)).isFalse();
		assertThat((Object) ValueConversionHelper.convert("h", char.class)).isEqualTo('h');
		assertThat(ValueConversionHelper.convert("h", String.class)).isEqualTo("h");
		assertThat(ValueConversionHelper.convert("ONE", TestEnum.class)).isEqualTo(TestEnum.ONE);
		assertThat(ValueConversionHelper.convert("h", Boolean.class)).isTrue();
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
		assertThat(ValueConversionHelper.convert(null, TestEnum.class)).isNull();
		assertThat(ValueConversionHelper.convert("ONE", TestEnum.class)).isEqualTo(TestEnum.ONE);
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
		assertThat(ValueConversionHelper.convert(null, Integer.class)).isNull();
		assertThat((Object) ValueConversionHelper.convert("1", Integer.class)).isEqualTo(1);
		assertThat((Object) ValueConversionHelper.convert("1", Float.class)).isEqualTo(1f);
		assertThat((Object) ValueConversionHelper.convert("1", Double.class)).isEqualTo(1d);
		assertThat((Object) ValueConversionHelper.convert("1", Byte.class)).isEqualTo((byte) 1);
		assertThat(ValueConversionHelper.convert("1", Number.class).intValue()).isEqualTo(1);
		assertThat((Object) ValueConversionHelper.convert("1", short.class)).isEqualTo((short) 1);
		assertThat((Object) ValueConversionHelper.convert("1", long.class)).isEqualTo(1L);
		assertThat(ValueConversionHelper.convert("1", BigDecimal.class)).isEqualTo(BigDecimal.valueOf(1));
		assertThat(ValueConversionHelper.convert("1", BigInteger.class)).isEqualTo(BigInteger.valueOf(1));
		try {
			ValueConversionHelper.convert("", Integer.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
			// OK
		}
		try {
			ValueConversionHelper.convert("1", (Class<Integer>) (Object) Calendar.class);
			fail("should not be able to convert value");
		} catch (IncompatibleTypeException e) {
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
	
	@Test
	public void testIsPrimitiveNumber() {
		assertThat(ValueConversionHelper.isPrimitiveNumber(char.class)).isFalse();
		assertThat(ValueConversionHelper.isPrimitiveNumber(int.class)).isTrue();
		assertThat(ValueConversionHelper.isPrimitiveNumber(float.class)).isTrue();
		assertThat(ValueConversionHelper.isPrimitiveNumber(double.class)).isTrue();
		assertThat(ValueConversionHelper.isPrimitiveNumber(long.class)).isTrue();
		assertThat(ValueConversionHelper.isPrimitiveNumber(byte.class)).isTrue();
		assertThat(ValueConversionHelper.isPrimitiveNumber(short.class)).isTrue();
		assertThat(ValueConversionHelper.isPrimitiveNumber(boolean.class)).isFalse();
		assertThat(ValueConversionHelper.isPrimitiveNumber(Calendar.class)).isFalse();
		assertThat(ValueConversionHelper.isPrimitiveNumber(Boolean.class)).isFalse();
		assertThat(ValueConversionHelper.isPrimitiveNumber(Character.class)).isFalse();
		assertThat(ValueConversionHelper.isPrimitiveNumber(Integer.class)).isFalse();
		assertThat(ValueConversionHelper.isPrimitiveNumber(Number.class)).isFalse();
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
	
	@Test
	public void testConversionOverInstanceForSameType() {
		assertThat(ValueConversionHelper.convert("moo", String.class)).isEqualTo("moo");
		
		ValueConversionHelper.registerValueConverter(new ValueFunctionImpl<>(String.class, String.class, new Function<String, String>() {
			@Override
			public String apply(String value) {
				return format("--%s--", value);
			}
		}));
		
		assertThat(ValueConversionHelper.convert("moo", String.class)).isEqualTo("--moo--");
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
		
		@NotNull
		@Override
		public Class<Object> getFromType() {
			return (Class<Object>) fromType;
		}
		
		@NotNull
		@Override
		public Class<Object> getTargetType() {
			return (Class<Object>) targetType;
		}
		
		@NotNull
		@Override
		public Class<Object> convertValue(Object value) {
			throw new AssertionError("This method should not be used");
		}
	}
}