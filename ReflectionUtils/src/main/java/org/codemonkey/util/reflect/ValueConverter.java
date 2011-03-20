package org.codemonkey.util.reflect;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

/**
 * This reflection utility class predicts which types a specified value can be converted into. We can only do conversions of known types so
 * the common conversions include:
 * <ul>
 * <li>Any {@link Number} type (Integer, int, Double, double, etc.)</li>
 * <li>String</li>
 * <li>Boolean</li>
 * <li>Character</li>
 * </ul>
 * In addition to predicting compatible output types, this class can also actually perform those conversions.
 * 
 * @author Benny Bottema
 * @see IncompatibleTypeException
 */
public final class ValueConverter {

	/**
	 * Determines to which types the specified value (its type) can be converted to. Most common types can be converted to most other common
	 * types and all types can be converted into a String using {@link Object#toString()}
	 * 
	 * @param c The input type to find compatible conversion output types for
	 * @return The list with compatible conversion output types.
	 */
	public static Class<?>[] collectCompatibleTypes(final Class<?> c) {
		// all common types can at least convert into any of these types
		final Class<?>[] baselist = new Class<?>[] { String.class, Integer.class, int.class, Float.class, float.class, Double.class,
				double.class, Long.class, long.class, Byte.class, byte.class, Short.class, short.class };
		final List<Class<?>> list = new LinkedList<Class<?>>(Arrays.asList(baselist));

		// add whatever conversion can be applied to instances the specified class
		if (c.equals(String.class)) {
			list.add(Boolean.class);
			list.add(boolean.class);
			list.add(Character.class);
			list.add(char.class);
		} else if (Number.class.isAssignableFrom(c) || isPrimitiveNumber(c)) {
			list.add(Boolean.class);
			list.add(boolean.class);
			list.add(Character.class);
			list.add(char.class);
		} else if (c.equals(Boolean.class) || c.equals(boolean.class)) {
			list.add(Character.class);
			list.add(char.class);
		} else if (c.equals(Character.class) || c.equals(char.class)) {
			list.add(Boolean.class);
			list.add(boolean.class);
		} else {
			// not a common type, we only know we're able to convert to String
			return new Class[] { String.class };
		}
		// return combined compatible types
		return list.toArray(new Class[0]);
	}

	/**
	 * Converts a list of values to their converted form, as indicated by the specified targetTypes.
	 * 
	 * @param args The list with value to convert.
	 * @param targetTypes The output types the specified values should be converted into.
	 * @throws IncompatibleTypeException
	 */
	public static void convert(final Object[] args, final Class<?>[] targetTypes)
			throws IncompatibleTypeException {
		for (int i = 0; i < targetTypes.length; i++) {
			args[i] = convert(args[i], targetTypes[i]);
		}
	}

	/**
	 * Converts a single value into a target output datatype. Only input/output pairs should be passed in here according to the possible
	 * conversions as determined by {@link #collectCompatibleTypes(Class)}.<br />
	 * <br />
	 * First checks if the input and output types aren't the same. Then the conversions are checked for and done in the following order:
	 * <ol>
	 * <li>conversion to <code>String</code></li>
	 * <li>conversion to any {@link Number}</li>
	 * <li>conversion to <code>Boolean</code></li>
	 * <li>conversion to <code>Character</code></li>
	 * </ol>
	 * 
	 * @param value The value to convert.
	 * @param targetType The target datatype the value should be converted into.
	 * @return The converted value according the specified target datatype.
	 * @throws IncompatibleTypeException
	 */
	public static Object convert(final Object value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		}
		final Class<?> original = value.getClass();

		// 1. check if conversion is required to begin with
		if (targetType.isAssignableFrom(original)) {
			return value;
			// 2. check if we can use Object.toString() implementation instead
		} else if (targetType.equals(String.class)) {
			return value.toString();
			// 3. check if we can use a Number.xxxValue() implementation instead
		} else if (original.equals(String.class)) {
			return convert((String) value, targetType);
			// 4. check if we can convert value from type Boolean
		} else if (Number.class.isAssignableFrom(original) || isPrimitiveNumber(original)) {
			return convert((Number) value, targetType);
			// 4. check if we can convert value from type Boolean
		} else if (original.equals(Boolean.class) || original.equals(boolean.class)) {
			return convert((Boolean) value, targetType);
			// 5. check if we can convert value from type Character
		} else if (original.equals(Character.class) || original.equals(char.class)) {
			return convert((Character) value, targetType);
		} else {
			throw new IncompatibleTypeException(value, original.toString(), targetType.toString());
		}
	}

	/**
	 * Attempts to convert a {@link Number} to the target datatype.
	 * 
	 * @param value The number to convert.
	 * @param targetType The target datatype the number should be converted into.
	 * @return The converted number.
	 * @throws IncompatibleTypeException
	 */
	public static Object convert(final Number value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		}
		if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
			return value.intValue();
		} else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
			// any non-zero number converts to true
			return value.intValue() > 0;
		} else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
			return value.floatValue();
		} else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
			return value.doubleValue();
		} else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
			return value.longValue();
		} else if (targetType.equals(Byte.class) || targetType.equals(byte.class)) {
			return value.byteValue();
		} else if (targetType.equals(Short.class) || targetType.equals(short.class)) {
			return value.shortValue();
		} else if (targetType.equals(Character.class) || targetType.equals(char.class)) {
			// any non-zero number converts to true
			return Character.forDigit(value.intValue(), 10);
		}
		throw new IncompatibleTypeException(value, Boolean.class.toString(), targetType.toString());
	}

	/**
	 * Attempts to convert a <code>Boolean</code> to the target datatype.
	 * 
	 * @param value The boolean to convert.
	 * @param targetType The target datatype the boolean should be converted into.
	 * @return The converted boolean.
	 * @throws IncompatibleTypeException
	 */
	public static Object convert(final Boolean value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		}
		if (Number.class.isAssignableFrom(targetType) || isPrimitiveNumber(targetType)) {
			return value ? 1 : 0;
		} else if (targetType.equals(Character.class) || targetType.equals(char.class)) {
			return value ? '1' : '0';
		}
		// Boolean value incompatible with targetType
		throw new IncompatibleTypeException(value, Boolean.class.toString(), targetType.toString());
	}

	/**
	 * Attempts to convert a <code>Character</code> to the target datatype.
	 * 
	 * @param value The character to convert.
	 * @param targetType The target datatype the character should be converted into.
	 * @return The converted character.
	 * @throws IncompatibleTypeException
	 */
	public static Object convert(final Character value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		}
		if (Number.class.isAssignableFrom(targetType) || isPrimitiveNumber(targetType)) {
			// convert Character to Number
			return Integer.parseInt(value.toString());
		} else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
			// convert Character to Boolean
			if (value.equals('0')) {
				return false;
			} else if (value.equals('1')) {
				return true;
			} else {
				// Character incompatible with type Boolean
			}
		}
		// Character incompatible with type targetType
		throw new IncompatibleTypeException(value, Character.class.toString(), targetType.toString());
	}

	/**
	 * Attempts to convert a <code>String</code> to the target datatype.
	 * 
	 * @param value The string to convert.
	 * @param targetType The target datatype the string should be converted into.
	 * @return The converted string.
	 * @throws IncompatibleTypeException
	 */
	@SuppressWarnings("unchecked")
	public static Object convert(final String value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		}
		if (targetType.equals(String.class)) {
			return value;
		}
		if (targetType.isEnum()) {
			return convertEnum(value, (Class<? super Enum<?>>) targetType);
		}
		if (value.length() == 0) {
			// can't convert the String value to anything else
		} else if (value.length() == 1) {
			// Convert String as Character
			if (targetType.equals(Character.class)) {
				return value.charAt(0);
			} else {
				return convert(value.charAt(0), targetType);
			}
		}
		if (Number.class.isAssignableFrom(targetType) || isPrimitiveNumber(targetType)) {
			return convertNumber(value, (Class<? super Number>) targetType);
		} else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
			// convert String to Boolean
			if (value.equalsIgnoreCase("false")) {
				return false;
			} else if (value.equalsIgnoreCase("true")) {
				return true;
			} else {
				// String incompatible with type Boolean
			}
		}
		// String value incompatible with targetType
		throw new IncompatibleTypeException(value, value.getClass().toString(), targetType.toString());
	}

	private static Object convertEnum(final String value, final Class<? super Enum<?>> targetType) {
		if (value == null) {
			return null;
		}
		try {
			return targetType.getMethod("valueOf", String.class).invoke(null, value);
		} catch (final IllegalArgumentException e) {
			throw new IncompatibleTypeException(value, Enum.class.toString(), targetType.toString(), e);
		} catch (final SecurityException e) {
			throw new IncompatibleTypeException(value, Enum.class.toString(), targetType.toString(), e);
		} catch (final IllegalAccessException e) {
			throw new IncompatibleTypeException(value, Enum.class.toString(), targetType.toString(), e);
		} catch (final InvocationTargetException e) {
			throw new IncompatibleTypeException(value, Enum.class.toString(), targetType.toString(), e);
		} catch (final NoSuchMethodException e) {
			throw new IncompatibleTypeException(value, Enum.class.toString(), targetType.toString(), e);
		}

	}

	/**
	 * Attempts to convert a <code>String</code> to the specified <code>Number</code> type.
	 * 
	 * @param value The string value which should be a number.
	 * @param numberType The <code>Class</code> type that should be one of <code>Number</code>.
	 * @return
	 */
	public static Object convertNumber(final String value, final Class<? super Number> numberType) {
		if (value == null) {
			return null;
		}
		assert Number.class.isAssignableFrom(numberType) || isPrimitiveNumber(numberType);
		if (NumberUtils.isNumber(value)) {
			if (Integer.class.equals(numberType) || int.class.equals(numberType)) {
				return Integer.parseInt(value);
			} else if (Byte.class.equals(numberType) || byte.class.equals(numberType)) {
				return Byte.parseByte(value);
			} else if (Short.class.equals(numberType) || short.class.equals(numberType)) {
				return Short.parseShort(value);
			} else if (Long.class.equals(numberType) || long.class.equals(numberType)) {
				return Long.parseLong(value);
			} else if (Float.class.equals(numberType) || float.class.equals(numberType)) {
				return Float.parseFloat(value);
			} else if (Double.class.equals(numberType) || double.class.equals(numberType)) {
				return Double.parseDouble(value);
			} else if (BigInteger.class.equals(numberType)) {
				return BigInteger.valueOf(Long.parseLong(value));
			} else if (BigDecimal.class.equals(numberType)) {
				return BigDecimal.valueOf(Long.parseLong(value));
			} else {
				// specified type incompatible with Number
				throw new IncompatibleTypeException(value, value.getClass().toString(), numberType.toString());
			}
		} else {
			// specified value incompatible with Number
			throw new IncompatibleTypeException(value, value.getClass().toString(), numberType.toString());
		}
	}

	/**
	 * Returns whethe a {@link Class} is a primitive number.
	 * 
	 * @param targetType The class to check whether it's a number.
	 * @return Whether specified class is a primitive number.
	 */
	private final static boolean isPrimitiveNumber(final Class<?> targetType) {
		final Class<?>[] nums = new Class<?>[] { byte.class, short.class, int.class, long.class, float.class, double.class };
		for (final Class<?> c : nums) {
			if (targetType.equals(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This exception can be thrown in any of the conversion methods of {@link ValueConverter}, to indicate a value could not be converted
	 * into the target datatype. It doesn't mean a failed attempt at a conversion, it means that there was no way to convert the input value
	 * to begin with.
	 * 
	 * @author Benny Bottema
	 */
	public static final class IncompatibleTypeException extends RuntimeException {
		private static final long serialVersionUID = -9234872336546L;

		private static final String pattern = "error: unable to convert value '%s': '%s' to '%s'";

		public IncompatibleTypeException(final String message, final Exception e) {
			super(message, e);
		}

		public IncompatibleTypeException(final Object value, final String className, final String targetName) {
			super(String.format(pattern, value, className, targetName));
		}

		public IncompatibleTypeException(final Object value, final String className, final String targetName,
				final Exception nestedException) {
			super(String.format(pattern, value, className, targetName), nestedException);
		}
	}
}
