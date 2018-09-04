package org.bbottema.javareflection.valueconverter;

import org.bbottema.javareflection.commonslang25.NumberUtils;
import org.bbottema.javareflection.util.Dijkstra;
import org.bbottema.javareflection.util.Dijkstra.Node;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Arrays.asList;
import static org.bbottema.javareflection.util.MiscUtil.assumeTrue;

/**
 * This reflection utility class predicts (and converts) which types a specified value can be converted into. It can only do conversions of
 * known 'common' types, which include:
 * <ul>
 * <li>Any {@link Number} type (Integer, Character, Double, byte, etc.)</li>
 * <li><code>String</code></li>
 * <li><code>Boolean</code></li>
 * <li><code>Character</code></li>
 * </ul>
 * In addition enums can be converted as well.
 * 
 * @author Benny Bottema
 * @see IncompatibleTypeException
 */
public final class ValueConversionHelper {

	/**
	 * List of common types that all other common types can always convert to. For example, <code>String</code> and <code>Integer</code> are
	 * basic common types and can be converted to any other common type.
	 */
	//FIXME move this out
	public static final Set<Class<?>> COMMON_TYPES = new HashSet<Class<?>>(asList(String.class, Integer.class, int.class, Float.class,
			float.class, Double.class, double.class, Long.class, long.class, Byte.class, byte.class, Short.class, short.class,
			Boolean.class, boolean.class, Character.class, char.class));

	/**
	 * A list of all primitive number types.
	 */
	private static final List<Class<?>> PRIMITIVE_NUMBER_TYPES = asList(new Class<?>[] { byte.class, short.class, int.class, long.class,
			float.class, double.class });
	
	/**
	 * Contains all user-provided converters. User converters also act as intermediate converters, ie. if a user converter can go to <code>int</code>,
	 * <code>double</code> is automatically supported as well as common conversion.
	 */
	private static final Map<Class<?>, Map<Class<?>, ValueFunction<Object, Object>>> userValueConverters = new HashMap<>();
	
	/**
	 * Graph of from-to type conversions so we can calculate shortes conversion path between two types.
	 */
	private static final Map<Class<?>, Node> converterGraph = new HashMap<>();
	
	/**
	 * Registers a user-provided converter. User converters also act as intermediate converters, ie. if a user converter can go to <code>int</code>,
	 * <code>double</code> is automatically supported as well as common conversion.
	 */
	@SuppressWarnings("unchecked")
	public static void registerUserValueConverter(final ValueFunction<?, ?> userConverter) {
		if (!userValueConverters.containsKey(userConverter.fromType())) {
			userValueConverters.put(userConverter.fromType(), new HashMap<Class<?>, ValueFunction<Object, Object>>());
		}
		userValueConverters.get(userConverter.fromType()).put(userConverter.targetType(), (ValueFunction<Object, Object>) userConverter);
		
		updateTypeGraph();
	}
	
	private static void updateTypeGraph() {
		converterGraph.clear();
		
		// add nodes
		for (Class<?> forType : userValueConverters.keySet()) {
			converterGraph.put(forType, new Node(forType));
		}
		// add edges
		for (Map<Class<?>, ValueFunction<Object, Object>> convertersForType : userValueConverters.values()) {
			for (ValueFunction<Object, Object> converter : convertersForType.values()) {
				Node fromNode = converterGraph.get(converter.fromType());
				Node toNode = converterGraph.get(converter.targetType());
				fromNode.getToTypes().put(toNode, 1);
			}
		}
	}
	
	/**
	 * @param c The class to inspect.
	 * @return whether given type is a known common type.
	 */
	public static boolean isCommonType(final Class<?> c) {
		return COMMON_TYPES.contains(c);
	}

	/**
	 * Private constructor prevents from instantiating this utility class.
	 */
	private ValueConversionHelper() {
		// utility class
	}

	/**
	 * Determines to which types the specified value (its type) can be converted to. Most common types can be converted to most other common
	 * types and all types can be converted into a String using {@link Object#toString()}.
	 * 
	 * @param c The input type to find compatible conversion output types for
	 * @return The list with compatible conversion output types.
	 */
	@Nonnull
	public static Set<Class<?>> collectCompatibleTypes(final Class<?> c) {
		Set<Class<?>> compatibleTypes = new HashSet<>();
		for (Node reachableNode : Dijkstra.findReachableNodes(converterGraph.get(c))) {
			compatibleTypes.add(reachableNode.getType());
		}
		return compatibleTypes;
	}
	
	/**
	 * @return Whether <code>targetType</code> can be derived from <code>fromType</code>.
	 */
	public static boolean typesCompatible(final Class<?> fromType, final Class<?> targetType) {
		return collectCompatibleTypes(fromType).contains(targetType);
	}

	/**
	 * Converts a list of values to their converted form, as indicated by the specified targetTypes.
	 * 
	 * @param args The list with value to convert.
	 * @param targetTypes The output types the specified values should be converted into.
	 * @param useOriginalValueWhenIncompatible Indicates whether an exception should be thrown for inconvertible values or that the original
	 *            value should be used instead.
	 * @return Array containing converted values where convertible or the original value otherwise.
	 * @throws IncompatibleTypeException Thrown when unable to convert and not use the original value.
	 */
	@Nonnull
	public static Object[] convert(final Object[] args, final Class<?>[] targetTypes, boolean useOriginalValueWhenIncompatible)
			throws IncompatibleTypeException {
		if (args.length != targetTypes.length) {
			throw new IllegalStateException("number of target types should match the number of arguments");
		}
		final Object[] convertedValues = new Object[args.length];
		for (int i = 0; i < targetTypes.length; i++) {
			try {
				convertedValues[i] = convert(args[i], targetTypes[i]);
			} catch (IncompatibleTypeException e) {
				if (useOriginalValueWhenIncompatible) {
					// simply take over the original value and keep converting where possible
					convertedValues[i] = args[i];
				} else {
					throw e;
				}
			}
		}
		return convertedValues;
	}

	/**
	 * Converts a single value into a target output datatype. Only input/output pairs should be passed in here according to the possible
	 * conversions as determined by {@link #collectCompatibleTypes(Class)}.<br />
	 * <br />
	 * First checks if the input and output types aren't the same. Then the conversions are checked for and done in the following order:
	 * <ol>
	 * <li>conversion to <code>String</code> (value.toString())</li>
	 * <li>conversion from <code>String</code> ({@link #convert(String, Class)})</li>
	 * <li>conversion to any {@link Number} ({@link #convert(Number, Class)})</li>
	 * <li>conversion to <code>Boolean</code> ({@link #convert(Boolean, Class)})</li>
	 * <li>conversion to <code>Character</code> ({@link #convert(Character, Class)})</li>
	 * </ol>
	 * 
	 * @param value The value to convert.
	 * @param targetType The target data type the value should be converted into.
	 * @return The converted value according the specified target data type.
	 * @throws IncompatibleTypeException Thrown by the various <code>convert()</code> methods used.
	 */
	@Nullable
	public static Object convert(@Nullable final Object value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		} else {
			final Class<?> valueType = value.getClass();
			if (targetType.isAssignableFrom(valueType)) {
				return value;
			} else if (typesCompatible(valueType, targetType)) {
				Dijkstra.calculateShortestPathFromSource(converterGraph.get(valueType));
				Object valueInFlux = value;
				for (Node nodeInConversionPath : converterGraph.get(targetType).getShortestPath()) {
					Class<?> fromType = valueInFlux.getClass();
					Class<?> toType = nodeInConversionPath.getType();
					valueInFlux = userValueConverters.get(fromType).get(toType).convertValue(valueInFlux);
				}
				return valueInFlux;
			}
		}
		
		// 1. check if conversion is required to begin with
		if (targetType.isAssignableFrom(valueType)) {
			return value;
			// 2. check if we can simply use Object.toString() implementation
		} else if (targetType.equals(String.class)) {
			return value.toString();
			// 3. check if we can reuse conversion from String
		} else if (valueType.equals(String.class)) {
			return convert((String) value, targetType);
			// 4. check if we can reuse conversion from a Number subtype
		} else if (Number.class.isAssignableFrom(valueType) || isPrimitiveNumber(valueType)) {
			return convert((Number) value, targetType);
			// 4. check if we can reuse conversion from boolean value
		} else if (valueType.equals(Boolean.class) || valueType.equals(boolean.class)) {
			return convert((Boolean) value, targetType);
			// 5. check if we can reuse conversion from character
		} else if (valueType.equals(Character.class) || valueType.equals(char.class)) {
			return convert((Character) value, targetType);
		} else {
			throw new IncompatibleTypeException(value, valueType.toString(), targetType.toString());
		}
	}
	
	/**
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
	 * 
	 * @param value The number to convert.
	 * @param targetType The target datatype the number should be converted into.
	 * @return The converted number.
	 * @throws IncompatibleTypeException Thrown when unable to find a compatible conversion.
	 */
	@Nullable
	public static Object convert(@Nullable final Number value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		}
		if (targetType.equals(String.class)) {
			return value.toString();
		} else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
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
			return Character.forDigit(value.intValue(), 10);
		} else {
			throw new IncompatibleTypeException(value, value.getClass().toString(), targetType.toString());
		}
	}

	/**
	 * Attempts to convert a <code>Boolean</code> to the target datatype.
	 * <p>
	 * Conversions are as follows:
	 * <ol>
	 * <li><strong>Boolean (or boolean)</strong>: <code>value</code></li>
	 * <li><strong>String</strong>: <code>value.toString()</code></li>
	 * <li><strong>Number (or primitive number)</strong>: <code>convertNumber(value ? "1" : "0")</code> ({@link #convertNumber(String, Class)})</li>
	 * <li><strong>Character (or character)</strong>: <code>value ? '1' : '0'</code></li>
	 * </ol>
	 * 
	 * @param value The boolean to convert.
	 * @param targetType The target datatype the boolean should be converted into.
	 * @return The converted boolean.
	 * @throws IncompatibleTypeException Thrown when unable to find a compatible conversion.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static Object convert(@Nullable final Boolean value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		}
		if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
			return value;
		} else if (targetType.equals(String.class)) {
			return value.toString();
		} else if (Number.class.isAssignableFrom(targetType) || isPrimitiveNumber(targetType)) {
			return convertNumber(value ? "1" : "0", (Class<Number>) targetType);
		} else if (targetType.equals(Character.class) || targetType.equals(char.class)) {
			return value ? '1' : '0';
		}
		// Boolean value incompatible with targetType
		throw new IncompatibleTypeException(value, Boolean.class.toString(), targetType.toString());
	}

	/**
	 * Attempts to convert a <code>Character</code> to the target datatype.
	 * <p>
	 * Conversions are as follows:
	 * <ol>
	 * <li><strong>String</strong>: <code>value.toString()</code></li>
	 * <li><strong>Character (or primitive character)</strong>: <code>value</code></li>
	 * <li><strong>Number (or primitive number)</strong>: <code>convertNumber(String.valueOf(value))</code> ({@link #convertNumber(String, Class)})</li>
	 * <li><strong>Boolean (or boolean)</strong>: <code>!value.equals('0')</code></li>
	 * </ol>
	 * 
	 * @param value The character to convert.
	 * @param targetType The target datatype the character should be converted into.
	 * @return The converted character.
	 * @throws IncompatibleTypeException Thrown when unable to find a compatible conversion.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static Object convert(@Nullable final Character value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		}
		if (targetType.equals(String.class)) {
			return value.toString();
		} else if (targetType.equals(char.class) || targetType.equals(Character.class)) {
			return value;
		} else if (Number.class.isAssignableFrom(targetType) || isPrimitiveNumber(targetType)) {
			// convert Character to Number
			return convertNumber(String.valueOf(value), (Class<Number>) targetType);
		} else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
			// convert Character to Boolean
			return !value.equals('0');
		}
		// Character incompatible with type targetType
		throw new IncompatibleTypeException(value, Character.class.toString(), targetType.toString());
	}

	/**
	 * Attempts to convert a <code>String</code> to the target datatype.
	 * <p>
	 * Conversions are as follows:
	 * <ol>
	 * <li><strong>String</strong>: <code>value</code></li>
	 * <li><strong>Enum type</strong>: <code>convertEnum(value)</code> ({@link #convertEnum(String, Class)})</li>
	 * <li><strong>with source string length == 1</strong>
	 * <ol>
	 * <li><strong>Character</strong>: <code>value.charAt(0)</code></li>
	 * <li><strong>Otherwise</strong>: <code>convert(value.charAt(0))</code> ({@link #convert(Character, Class)})</li>
	 * </ol>
	 * </li>
	 * <li><strong>with source string length > 1</strong>
	 * <ol>
	 * <li><strong>Number (or primitive number)</strong>: <code>convertNumber(value)</code> ({@link #convertNumber(String, Class)})</li>
	 * <li><strong>Boolean (or primitive boolean)</strong>: <code>value.equalsIgnoreCase("true") ? true : value.equalsIgnoreCase("false") ? false : nothing (IncompatibleTypeException)</code></li>
	 * </ol>
	 * </li>
	 * </ol>
	 * 
	 * @param value The string to convert.
	 * @param targetType The target datatype the string should be converted into.
	 * @return The converted string.
	 * @throws IncompatibleTypeException Thrown when unable to find a compatible conversion.
	 */
	@Nullable
	@SuppressWarnings({"unchecked", "StatementWithEmptyBody"})
	public static Object convert(@Nullable final String value, final Class<?> targetType)
			throws IncompatibleTypeException {
		if (value == null) {
			return null;
		} else if (targetType.equals(String.class)) {
			return value;
		} else if (targetType.isEnum()) {
			return convertEnum(value, (Class<? extends Enum<?>>) targetType);
		} else if (value.length() > 0) {
			if (value.length() == 1) {
				// Convert String as Character
				if (targetType.equals(Character.class) || targetType.equals(char.class)) {
					return value.charAt(0);
				} else {
					return convert(value.charAt(0), targetType);
				}
			} else if (Number.class.isAssignableFrom(targetType) || isPrimitiveNumber(targetType)) {
				return convertNumber(value, (Class<? extends Number>) targetType);
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
		}
		// String value incompatible with targetType
		throw new IncompatibleTypeException(value, value.getClass().toString(), targetType.toString());
	}

	/**
	 * Attempts to convert a <code>String</code> to an Enum instance, by mapping to the enum's name using
	 * {@link Enum#valueOf(Class, String)}.
	 * 
	 * @param value The value, which should be the name of one instance of the given enum.
	 * @param targetType The enum type to which which we'll try to convert.
	 * @return An enum of the given type, or <code>null</code> otherwise.
	 */
	@Nullable
	public static Object convertEnum(@Nullable final String value, final Class<? extends Enum<?>> targetType) {
		if (value == null) {
			return null;
		}
		// /CLOVER:OFF
		try {
			// /CLOVER:ON
			return targetType.getMethod("valueOf", String.class).invoke(null, value);
			// /CLOVER:OFF
		} catch (final IllegalArgumentException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException e) {
			throw new IncompatibleTypeException(value, Enum.class.toString(), targetType.toString(), e);
		}
		// /CLOVER:ON

	}

	/**
	 * Attempts to convert a <code>String</code> to the specified <code>Number</code> type.
	 * <p>
	 * Conversions are as follows:
	 * <ol>
	 * <li><strong>Integer (or primitive int)</strong>: <code>Integer.parseInt(value)</code></li>
	 * <li><strong>Byte (or primitive byte)</strong>: <code>Byte.parseByte(value)</code></li>
	 * <li><strong>Short (or primitive short)</strong>: <code>Short.parseShort(value)</code></li>
	 * <li><strong>Long (or primitive long)</strong>: <code>Long.parseLong(value)</code></li>
	 * <li><strong>Float (or primitive float)</strong>: <code>Float.parseFloat(value)</code></li>
	 * <li><strong>Double (or primitive double)</strong>: <code>Double.parseDouble(value)</code></li>
	 * <li><strong>BigInteger</strong>: <code>BigInteger.valueOf(Long.parseLong(value))</code></li>
	 * <li><strong>BigDecimal</strong>: <code>BigDecimal.valueOf(Long.parseLong(value))</code></li>
	 * </ol>
	 * 
	 * @param value The string value which should be a number.
	 * @param numberType The <code>Class</code> type that should be one of <code>Number</code>.
	 * @return A {@link Number} subtype value converted from <code>value</code> (or <code>null</code> if value is <code>null</code>).
	 */
	@Nullable
	public static Object convertNumber(@Nullable final String value, final Class<? extends Number> numberType) {
		if (value == null) {
			return null;
		}
		assumeTrue(Number.class.isAssignableFrom(numberType) || isPrimitiveNumber(numberType));
		if (NumberUtils.isNumber(value)) {
			if (Integer.class.equals(numberType) || int.class.equals(numberType) || Number.class.equals(numberType)) {
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
	 * Returns whether a {@link Class} is a primitive number.
	 * 
	 * @param targetType The class to check whether it's a number.
	 * @return Whether specified class is a primitive number.
	 */
	public static boolean isPrimitiveNumber(final Class<?> targetType) {
		return PRIMITIVE_NUMBER_TYPES.contains(targetType);
	}
	
}