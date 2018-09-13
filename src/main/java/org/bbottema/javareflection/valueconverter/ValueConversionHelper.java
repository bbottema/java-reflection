package org.bbottema.javareflection.valueconverter;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.util.graph.GraphHelper;
import org.bbottema.javareflection.util.graph.Node;
import org.bbottema.javareflection.valueconverter.converters.BooleanConverters;
import org.bbottema.javareflection.valueconverter.converters.CharacterConverters;
import org.bbottema.javareflection.valueconverter.converters.NumberConverters;
import org.bbottema.javareflection.valueconverter.converters.StringConverters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

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
@UtilityClass
public final class ValueConversionHelper {

	/**
	 * A list of all primitive number types.
	 */
	private static final List<Class<?>> PRIMITIVE_NUMBER_TYPES = asList(new Class<?>[] { byte.class, short.class, int.class, long.class,
			float.class, double.class });
	
	/**
	 * Contains all user-provided converters. User converters also act as intermediate converters, ie. if a user converter can go to <code>int</code>,
	 * <code>double</code> is automatically supported as well as common conversion.
	 */
	private static final Map<Class<?>, Map<Class<?>, ValueFunction<Object, Object>>> valueConverters = new HashMap<>();
	
	/**
	 * Graph of from-to type conversions so we can calculate shortes conversion path between two types.
	 */
	private static final Map<Class<?>, Node<Class<?>>> converterGraph = new HashMap<>();
	
	static {
		final Collection<ValueFunction<?, ?>> defaultConverters = new HashSet<>();
		defaultConverters.addAll(NumberConverters.NUMBER_CONVERTERS);
		defaultConverters.addAll(BooleanConverters.BOOLEAN_CONVERTERS);
		defaultConverters.addAll(CharacterConverters.CHARACTER_CONVERTERS);
		defaultConverters.addAll(StringConverters.STRING_CONVERTERS);
		
		for (ValueFunction<?, ?> numberConverter : defaultConverters) {
			registerValueConverter(numberConverter);
		}
	}
	
	/**
	 * Registers a user-provided converter. User converters also act as intermediate converters, ie. if a user converter can go to <code>int</code>,
	 * <code>double</code> is automatically supported as well as common conversion.
	 */
	@SuppressWarnings({"unchecked", "unused", "WeakerAccess"})
	public static void registerValueConverter(final ValueFunction<?, ?> userConverter) {
		if (!valueConverters.containsKey(userConverter.getFromType())) {
			valueConverters.put(userConverter.getFromType(), new HashMap<Class<?>, ValueFunction<Object, Object>>());
		}
		valueConverters.get(userConverter.getFromType()).put(userConverter.getTargetType(), (ValueFunction<Object, Object>) userConverter);
		
		updateTypeGraph();
	}
	
	private static void updateTypeGraph() {
		converterGraph.clear();
		
		// add nodes and edges
		for (Map.Entry<Class<?>, Map<Class<?>, ValueFunction<Object, Object>>> convertersForFromType : valueConverters.entrySet()) {
			Class<?> fromType = convertersForFromType.getKey();
			Node<Class<?>> fromNode = converterGraph.containsKey(fromType) ? converterGraph.get(fromType) : new Node<Class<?>>(fromType);
			converterGraph.put(fromType, fromNode);
			for (Class<?> toType : convertersForFromType.getValue().keySet()) {
				Node<Class<?>> toNode = converterGraph.containsKey(toType) ? converterGraph.get(toType) : new Node<Class<?>>(toType);
				converterGraph.put(toType, toNode);
				fromNode.getToNodes().put(toNode, 1); // edge
			}
		}
	}
	
	private static ValueFunction<Object, Object> locateValueConverter(Class<?> fromType, Class<?> toType) {
		return valueConverters.get(fromType).get(toType);
	}
	
	/**
	 * @param c The class to inspect.
	 * @return whether given type is a known common type.
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean isCommonType(final Class<?> c) {
		return valueConverters.containsKey(c);
	}

	/**
	 * Determines to which types the specified value (its type) can be converted to. Most common types can be converted to most other common
	 * types and all types can be converted into a String using {@link Object#toString()}.
	 * 
	 * @param fromType The input type to find compatible conversion output types for
	 * @return The list with compatible conversion output types.
	 */
	@SuppressWarnings("WeakerAccess")
	@NotNull
	public static Set<Class<?>> collectRegisteredCompatibleTargetTypes(final Class<?> fromType) {
		Set<Class<?>> compatibleTypes = new HashSet<>(Collections.<Class<?>>singleton(fromType));
		if (converterGraph.containsKey(fromType)) {
			for (Node<Class<?>> reachableNode : GraphHelper.findReachableNodes(converterGraph.get(fromType))) {
				compatibleTypes.add(reachableNode.getType());
			}
		}
		return compatibleTypes;
	}
	
	/**
	 * @return Whether <code>targetType</code> can be derived from <code>fromType</code>.
	 */
	public static boolean typesCompatible(final Class<?> fromType, final Class<?> targetType) {
		if (targetType.isAssignableFrom(fromType)) {
			return true;
		} else {
			for (Class<?> registeredCompatibleTargetType : collectCompatibleTargetTypes(fromType)) {
				if (targetType.isAssignableFrom(registeredCompatibleTargetType)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static Set<Class<?>> collectCompatibleTargetTypes(Class<?> fromType) {
		HashSet<Class<?>> universalTargetTypes = new HashSet<>();
		universalTargetTypes.add(fromType);
		universalTargetTypes.add(String.class);
		
		if (!valueConverters.keySet().contains(fromType)) {
			return universalTargetTypes;
		}
		
		Set<Class<?>> compatibleTargetTypes = new HashSet<>(universalTargetTypes);
		Node<Class<?>> fromNode = converterGraph.get(fromType);
		for (Map<Class<?>, ValueFunction<Object, Object>> convertersForFromTypes : valueConverters.values()) {
			for (Class<?> targetType : convertersForFromTypes.keySet()) {
				if (isCompatibleTargetType(fromNode, targetType)) {
					compatibleTargetTypes.add(targetType);
				}
			}
		}
		return compatibleTargetTypes;
	}
	
	private static boolean isCompatibleTargetType(Node<Class<?>> fromNode, Class<?> targetType) {
		for (Node<Class<?>> toNode : collectTypeCompatibleNodes(targetType)) {
			if (GraphHelper.isPathPossible(fromNode, toNode)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Converts a list of values to their converted form, as indicated by the specified targetTypes.
	 * 
	 * @param args The list with value to convert.
	 * @param targetTypes The output types the specified values should be converted into.
	 * @param useOriginalValueWhenIncompatible Indicates whether an exception should be thrown for inconvertible values or that the original
	 *            value should be used instead. Basically change mode to "convert what you can".
	 * @return Array containing converted values where convertible or the original value otherwise.
	 * @throws IncompatibleTypeException Thrown when unable to convert and not use the original value.
	 */
	@NotNull
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
	 * conversions as determined by {@link #collectRegisteredCompatibleTargetTypes(Class)}.<br />
	 * <br />
	 * First checks if the input and output types aren't the same. Then the conversions are checked for and done in the following order:
	 * <ol>
	 * <li>conversion to <code>String</code> (value.toString())</li>
	 * </ol>
	 * 
	 * @param fromValue The value to convert.
	 * @param targetType The target data type the value should be converted into.
	 * @return The converted value according the specified target data type.
	 * @throws IncompatibleTypeException Thrown by the various <code>convert()</code> methods used.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> T convert(@Nullable final Object fromValue, final Class<T> targetType)
			throws IncompatibleTypeException {
		if (fromValue == null) {
			return null;
		} else if (targetType.isAssignableFrom(fromValue.getClass())) {
			return (T) fromValue;
		} else {
			checkForAndRegisterEnumConverter(targetType);
			checkForAndRegisterToStringConverter(fromValue.getClass());
			return convertWithConversionGraph(fromValue, targetType);
		}
	}
	
	@NotNull
	private static <T> T convertWithConversionGraph(@NotNull Object fromValue, @NotNull Class<T> targetType) {
		final Node<Class<?>> fromNode = converterGraph.get(fromValue.getClass());
		
		if (fromNode != null) {
			for (Node<Class<?>> toNode : collectTypeCompatibleNodes(targetType)) {
				for (List<Node<Class<?>>> conversionPathAscending : GraphHelper.findAllPathsAscending(fromNode, toNode)) {
					try {
						Object evolvingValueToConvert = fromValue;
						for (Node<Class<?>> nodeInConversionPath : conversionPathAscending) {
							Class<?> currentFromType = evolvingValueToConvert.getClass();
							Class<?> currentToType = nodeInConversionPath.getType();
							evolvingValueToConvert = locateValueConverter(currentFromType, currentToType).convertValue(evolvingValueToConvert);
						}
						//noinspection unchecked
						return (T) evolvingValueToConvert;
					} catch (IncompatibleTypeException e) {
						// keep trying conversion paths...
					}
				}
			}
		}
		// conversion paths exhausted.
		throw new IncompatibleTypeException(fromValue, fromValue.getClass(), targetType);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> void checkForAndRegisterEnumConverter(Class<?> targetType) {
		if (Enum.class.isAssignableFrom(targetType)) {
			if (!valueConverters.get(String.class).containsKey(targetType)) {
				registerValueConverter(StringConverters.produceStringToEnumConverter((Class<T>) targetType));
			}
		}
	}
	
	private static void checkForAndRegisterToStringConverter(Class<?> fromType) {
		if (!valueConverters.containsKey(fromType) || !valueConverters.get(fromType).containsKey(String.class)) {
			registerValueConverter(StringConverters.produceTypeToStringConverter(fromType));
		}
	}
	
	static Set<Node<Class<?>>> collectTypeCompatibleNodes(Class<?> targetType) {
		final Set<Node<Class<?>>> typeCompatibleNodes = new HashSet<>();
		for (Map.Entry<Class<?>, Node<Class<?>>> converterNodeEntry : converterGraph.entrySet()) {
			if (targetType.isAssignableFrom(converterNodeEntry.getKey())) {
				typeCompatibleNodes.add(converterNodeEntry.getValue());
			}
		}
		return typeCompatibleNodes;
	}

	/**
	 * Returns whether a {@link Class} is a primitive number.
	 * 
	 * @param targetType The class to check whether it's a number.
	 * @return Whether specified class is a primitive number.
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean isPrimitiveNumber(final Class<?> targetType) {
		return PRIMITIVE_NUMBER_TYPES.contains(targetType);
	}
}