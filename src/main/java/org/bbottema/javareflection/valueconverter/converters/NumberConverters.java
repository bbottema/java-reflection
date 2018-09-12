package org.bbottema.javareflection.valueconverter.converters;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.util.Function;
import org.bbottema.javareflection.util.Function.Functions;
import org.bbottema.javareflection.valueconverter.ValueFunction;
import org.bbottema.javareflection.valueconverter.ValueFunction.ValueFunctionImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.bbottema.javareflection.util.MiscUtil.newArrayList;

/**
 * Generates converters for all numbers to all other numbers, by virtue of Number's own interface that forces all subclasses to implement basic
 * conversions to common other Number classes.
 * <p>
 * Attempts to convert a {@link Number} to the target datatype.
 * <p>
 * <strong>NOTE: </strong> precision may be lost when converting from a wide number to a narrower number (say float to integer). These
 * conversions are done by simply calling {@link Number#intValue()} and {@link Number#floatValue()} etc.
 * <p>
 * Conversions are as follows:
 * <ol>
 * <li><strong>String</strong>: <code>value.toString()</code></li>
 * <li><strong>Integer</strong>: <code>value.intValue()</code></li>
 * <li><strong>Boolean</strong>: <code>value.intValue() > 0</code></li>
 * <li><strong>Float</strong>: <code>value.floatValue()</code></li>
 * <li><strong>Double</strong>: <code>value.doubleValue()</code></li>
 * <li><strong>Long</strong>: <code>value.longValue()</code></li>
 * <li><strong>Byte</strong>: <code>value.byteValue()</code></li>
 * <li><strong>Short</strong>: <code>value.shortValue()</code></li>
 * <li><strong>Character</strong>: <code>Character.forDigit(value, 10)</code> ({@link Character#forDigit(int, int)})</code></li>
 * </ol>
 */
@UtilityClass
public class NumberConverters {
	
	private static final List<Class<? extends Number>> CONVERTABLE_NUMBER_FROM_CLASSES_JDK7 = newArrayList(Number.class, AtomicInteger.class, AtomicLong.class, BigDecimal.class, BigInteger.class, byte.class, Byte.class, double.class, Double.class, float.class, Float.class, int.class, Integer.class, long.class, Long.class, short.class, Short.class);
	private static final Map<Class<?>, Function<Number, ?>> CONVERTERS_BY_TARGET_TYPE = new HashMap<Class<?>, Function<Number, ?>>() {{
		put(Number.class, new NumberDoubleFunction());
		put(Integer.class, new NumberIntegerFunction());
		put(int.class, new NumberIntegerFunction());
		put(Boolean.class, new NumberBooleanFunction());
		put(boolean.class, new NumberBooleanFunction());
		put(Float.class, new NumberFloatFunction());
		put(float.class, new NumberFloatFunction());
		put(Double.class, new NumberDoubleFunction());
		put(double.class, new NumberDoubleFunction());
		put(Long.class, new NumberLongFunction());
		put(long.class, new NumberLongFunction());
		put(Byte.class, new NumberByteFunction());
		put(byte.class, new NumberByteFunction());
		put(Short.class, new NumberShortFunction());
		put(short.class, new NumberShortFunction());
		put(Character.class, new NumberCharacterFunction());
		put(char.class, new NumberCharacterFunction());
		put(String.class, Functions.<Number>simpleToString());
	}};
	
	public static final Collection<ValueFunction<? extends Number, ?>> NUMBER_CONVERTERS = produceNumberConverters();
	
	private static Collection<ValueFunction<? extends Number, ?>> produceNumberConverters() {
		ArrayList<ValueFunction<? extends Number, ?>> valueFunctions = new ArrayList<>();
		for (Class<? extends Number> numberFromClass : CONVERTABLE_NUMBER_FROM_CLASSES_JDK7) {
			for (Map.Entry<Class<?>, Function<Number, ?>> targetClassConverter : CONVERTERS_BY_TARGET_TYPE.entrySet()) {
				Class<?> targetClass = targetClassConverter.getKey();
				Function<?, ?> converter = (numberFromClass == targetClass || targetClass.isAssignableFrom(numberFromClass))
						? Functions.identity()
						: targetClassConverter.getValue();
				//noinspection unchecked
				valueFunctions.add(new ValueFunctionImpl(numberFromClass, targetClass, converter));
			}
		}
		return valueFunctions;
	}
	
	public static class NumberIntegerFunction implements Function<Number, Integer> {
		public Integer apply(Number value) {
			return value.intValue();
		}
	}
	
	public static class NumberBooleanFunction implements Function<Number, Boolean> {
		public Boolean apply(Number value) {
			return value.intValue() > 0;
		}
	}
	
	public static class NumberFloatFunction implements Function<Number, Float> {
		public Float apply(Number value) {
			return value.floatValue();
		}
	}
	
	public static class NumberDoubleFunction implements Function<Number, Double> {
		public Double apply(Number value) {
			return value.doubleValue();
		}
	}
	
	public static class NumberLongFunction implements Function<Number, Long> {
		public Long apply(Number value) {
			return value.longValue();
		}
	}
	
	public static class NumberByteFunction implements Function<Number, Byte> {
		public Byte apply(Number value) {
			return value.byteValue();
		}
	}
	
	public static class NumberShortFunction implements Function<Number, Short> {
		public Short apply(Number value) {
			return value.shortValue();
		}
	}
	
	public static class NumberCharacterFunction implements Function<Number, Character> {
		public Character apply(Number value) {
			return Character.forDigit(value.intValue(), 10);
		}
	}
}