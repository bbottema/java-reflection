package org.bbottema.javareflection;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.model.LookupMode;
import org.bbottema.javareflection.util.ArrayKey;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.bbottema.javareflection.LookupCaches.*;

/**
 * Utility functions that deal with type information, conversions and autoboxing.
 * <p>
 * Particularly of interest is {@link #generateCompatibleTypeLists(Set, Class[])},
 * which generates a collection of type lists which can be derived from the input type
 * list. This is usful for method matching, since Java reflection doesn't take into account
 * autoboxing, casting or auto widening, let alone type conversions.
 * <p>
 * <strong>Types that are candidates for Autoboxing:</strong>
 * <ul>
 * <li><strong>boolean</strong> <code>java.lang.Boolean</code></li>
 * <li><strong>char</strong> <code>java.lang.Character</code></li>
 * <li><strong>byte</strong> <code>java.lang.Byte</code></li>
 * <li><strong>short</strong> <code>java.lang.Short</code></li>
 * <li><strong>int</strong> <code>java.lang.Integer</code></li>
 * <li><strong>long</strong> <code>java.lang.Long</code></li>
 * <li><strong>float</strong> <code>java.lang.Float</code></li>
 * <li><strong>double</strong> <code>java.lang.Double</code></li>
 * </ul>
 * <p>
 * For types that are candidates for common conversion, please see {@link ValueConversionHelper}.
 */
@UtilityClass
public final class TypeUtils {
	
	/**
	 * A list with Number types in ascending order to wideness (or size) of each type (ie. double is wider than integer).
	 */
	private static final Map<Class<?>, Integer> numSizes;
	
	static {
		numSizes = new LinkedHashMap<>();
		int size = 0;
		numSizes.put(Byte.class, ++size);
		numSizes.put(Short.class, ++size);
		numSizes.put(Integer.class, ++size);
		numSizes.put(Long.class, ++size);
		numSizes.put(Float.class, ++size);
		numSizes.put(Double.class, ++size);
	}
	
	/**
	 * Creates a new array of class objects harvested from an array of objects.
	 * <p>
	 * NOTE: this method will never return primitive classes (such as double.class, as you can't put primitive values into an array of Objects (they
	 * will be autoboxes by the JVM).
	 *
	 * @param objects The array of objects to harvest classtypes from.
	 * @return The array with the harvested classtypes.
	 */
	@NotNull
	@SuppressWarnings("WeakerAccess")
	public static Class<?>[] collectTypes(final Object[] objects) {
		// collect classtypes of the arguments
		final Class<?>[] types = new Class<?>[objects.length];
		for (int i = 0; i < objects.length; i++) {
			final Object o = objects[i];
			types[i] = o != null ? o.getClass() : null;
		}
		return types;
	}
	
	@SuppressWarnings({"unused", "WeakerAccess"})
	public static boolean isTypeListCompatible(Class<?>[] inputTypeList, Class<?>[] targetTypeList, Set<LookupMode> lookupMode) {
		List<Class<?>[]> derivableTypeLists = generateCompatibleTypeLists(lookupMode, inputTypeList);
		
		for (Class<?>[] derivableTypeList : derivableTypeLists) {
			boolean currentTypeListCompatible = true;
			for (int i = 0; i < derivableTypeList.length && currentTypeListCompatible; i++) {
				if (derivableTypeList[i] != null && !derivableTypeList[i].equals(targetTypeList[i])) {
					currentTypeListCompatible = false;
				}
			}
			if (currentTypeListCompatible) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Initializes the list with type-arrays and starts generating beginning from index 0. This method is used for (un)wrapping.
	 *
	 * @param lookupMode Flag indicating the search steps that need to be done.
	 * @param inputTypelist The list with original user specified types.
	 * @return The list with converted type-arrays.
	 */
	@NotNull
	@SuppressWarnings({"unused", "WeakerAccess"})
	public static List<Class<?>[]> generateCompatibleTypeLists(final Set<LookupMode> lookupMode, final Class<?>... inputTypelist) {
		final ArrayKey arrayKey = new ArrayKey(inputTypelist);
		final List<Class<?>[]> cachedResult = getCachedCompatibleSignatures(lookupMode, arrayKey);
		return cachedResult != null
				? cachedResult
				: addCompatiblesignaturesToCache(lookupMode, arrayKey,
						generateCompatibleTypeLists(0, lookupMode, new ArrayList<Class<?>[]>(), inputTypelist));
	}
	
	/**
	 * Recursively generates a complete list of all possible (un)wraps (autoboxing), supertypes, implemented interfaces, type conversions and any
	 * combination thereof with the specified typeLists's elements (the individual parameter types).<br>
	 * <br>
	 * The combination typeLists are generated in the following order:
	 * <ol>
	 * <li>no conversion; highest priority as it comes closest to user's requirement/specification</li>
	 * <li>autoboxing; the autoboxed counterversion comes closest to the original datatype</li>
	 * <li>interface; where methods can't be found using original type, interface placeholders are attempted</li>
	 * <li>supertype; where methods can't be found using implemented interfaces, supertype placeholders are attempted</li>
	 * <li>conversions; if all else fails, try to convert the datatype for common types (ie. int to String)</li>
	 * </ol>
	 *  @param index The current index to start mutating from.
	 * @param lookupMode Flag indicating the search steps that need to be done.
	 * @param inputTypelist The list with current types, to mutate further upon.
	 */
	private static List<Class<?>[]> generateCompatibleTypeLists(final int index, final Set<LookupMode> lookupMode, final List<Class<?>[]> compatibleTypeLists, final Class<?>... inputTypelist) {
		// if new type array is completed
		if (index == inputTypelist.length) {
			compatibleTypeLists.add(inputTypelist);
		} else {
			// generate new array of types
			final Class<?> original = inputTypelist[index];
			
			// 1. don't generate compatible list; just try the normal type first
			// remember, in combinations types should be allowed to be converted)
			generateCompatibleTypeLists(index + 1, lookupMode, compatibleTypeLists, inputTypelist.clone());

			if (original != null) {
				// 2. generate type in which the original can be (un)wrapped
				if (lookupMode.contains(LookupMode.AUTOBOX) && !lookupMode.contains(LookupMode.SMART_CONVERT)) {
					final Class<?> autoboxed = autobox(original);
					if (autoboxed != null) {
						final Class<?>[] newTypeList = replaceInArray(inputTypelist.clone(), index, autoboxed);
						generateCompatibleTypeLists(index + 1, lookupMode, compatibleTypeLists, newTypeList);
					}
				}

				// autocast to supertype or interface?
				if (lookupMode.contains(LookupMode.CAST_TO_INTERFACE)) {
					// 3. generate implemented interfaces the original value could be converted (cast) into
					for (final Class<?> iface : original.getInterfaces()) {
						final Class<?>[] newTypeList = replaceInArray(inputTypelist.clone(), index, iface);
						generateCompatibleTypeLists(index + 1, lookupMode, compatibleTypeLists, newTypeList);
					}
				}

				if (lookupMode.contains(LookupMode.CAST_TO_SUPER)) {
					// 4. generate supertypes the original value could be converted (cast) into
					Class<?> supertype = original;
					while ((supertype = supertype.getSuperclass()) != null) {
						final Class<?>[] newTypeList = replaceInArray(inputTypelist.clone(), index, supertype);
						generateCompatibleTypeLists(index + 1, lookupMode, compatibleTypeLists, newTypeList);
					}
				}

				// 5. generate types the original value could be converted into
				if (lookupMode.contains(LookupMode.COMMON_CONVERT) && !lookupMode.contains(LookupMode.SMART_CONVERT)) {
					for (final Class<?> convert : collectRegisteredCompatibleTargetTypes(original)) {
						final Class<?>[] newTypeList = replaceInArray(inputTypelist.clone(), index, convert);
						generateCompatibleTypeLists(index + 1, lookupMode, compatibleTypeLists, newTypeList);
					}
				}

				// 6. generate types the original value could be converted into with intermediary conversions
				if (lookupMode.contains(LookupMode.SMART_CONVERT)) {
					for (final Class<?> convert : collectCompatibleTargetTypes(original)) {
						final Class<?>[] newTypeList = replaceInArray(inputTypelist.clone(), index, convert);
						generateCompatibleTypeLists(index + 1, lookupMode, compatibleTypeLists, newTypeList);
					}
				}
			}
		}
		
		return compatibleTypeLists;
	}
	
	@NotNull
	private static Set<Class<?>> collectRegisteredCompatibleTargetTypes(Class<?> fromType) {
		if (!CACHED_REGISTERED_COMPATIBLE_TARGET_TYPES.containsKey(fromType)) {
			CACHED_REGISTERED_COMPATIBLE_TARGET_TYPES.put(fromType, ValueConversionHelper.collectRegisteredCompatibleTargetTypes(fromType));
		}
		return CACHED_REGISTERED_COMPATIBLE_TARGET_TYPES.get(fromType);
	}
	
	@NotNull
	private static Set<Class<?>> collectCompatibleTargetTypes(Class<?> fromType) {
		if (!CACHED_COMPATIBLE_TARGET_TYPES.containsKey(fromType)) {
			CACHED_COMPATIBLE_TARGET_TYPES.put(fromType, ValueConversionHelper.collectCompatibleTargetTypes(fromType));
		}
		return CACHED_COMPATIBLE_TARGET_TYPES.get(fromType);
	}
	
	/**
	 * Emulates Java's Autoboxing feature; tries to convert a type to its (un)wrapped counter version.
	 *
	 * @param c The datatype to convert (autobox).
	 * @return The converted version of the specified type, or null.
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	public static Class<?> autobox(final Class<?> c) {
		// integer
		if (c == Integer.class) {
			return int.class;
		} else if (c == int.class) {
			return Integer.class;
		} else if (c == Boolean.class) {
			return boolean.class;
		} else if (c == boolean.class) {
			return Boolean.class;
		} else if (c == Character.class) {
			return char.class;
		} else if (c == char.class) {
			return Character.class;
		} else if (c == Byte.class) {
			return byte.class;
		} else if (c == byte.class) {
			return Byte.class;
		} else if (c == Short.class) {
			return short.class;
		} else if (c == short.class) {
			return Short.class;
		} else if (c == Long.class) {
			return long.class;
		} else if (c == long.class) {
			return Long.class;
		} else if (c == Float.class) {
			return float.class;
		} else if (c == float.class) {
			return Float.class;
		} else if (c == Double.class) {
			return double.class;
		} else if (c == double.class) {
			return Double.class;
		} else {
			return null;
		}
	}
	/**
	 * Returns the smallest class that can hold all of the specified numbers.
	 *
	 * @param numbers The list with numbers that all should fit in the <code>Number</code> container.
	 * @return The <code>Number</code> container that is just large enough for all specified numbers.
	 */
	@NotNull
	@SuppressWarnings("WeakerAccess")
	public static Class<?> widestNumberClass(final Number... numbers) {
		Integer widest = 0;
		Class<?> widestNumberType = Byte.class;
		for (final Number number : numbers) {
			final Integer size = numSizes.get(number.getClass());
			if (size > widest) {
				widestNumberType = number.getClass();
				widest = size;
			}
		}
		return widestNumberType;
	}
	
	/**
	 * Validates whether a string represents a valid package.
	 *
	 * @param name The string representing a list of packages.
	 * @return A boolean indicating whether name represents a valid package.
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean isPackage(final String name) {
		return name.equals("java") || Package.getPackage(name) != null;
	}

	/**
	 * @return Whether a given list of Annotation contains a certain annotation type.
	 * @see #findAnnotation(Collection, Class)
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean containsAnnotation(List<Annotation> myListOfAnnotations, Class<? extends Annotation> annotationClass) {
		return findAnnotation(myListOfAnnotations, annotationClass) != null;
	}

	/**
	 * @return Whether a given list of Annotation contains a certain annotation type.
	 * @see #findAnnotation(Annotation[], Class)
	 */
	public static boolean containsAnnotation(Annotation[] myListOfAnnotations, Class<? extends Annotation> annotationClass) {
		return findAnnotation(myListOfAnnotations, annotationClass) != null;
	}

    /**
     * @return Whether a given list of Annotation contains a certain annotation type.
	 * @see #findAnnotation(Annotation[], Class)
	 */
    @SuppressWarnings({"WeakerAccess"})
    @Nullable
    public static <T extends Annotation> T findAnnotation(Collection<Annotation> myListOfAnnotations, Class<T> annotationClass) {
		return findAnnotation(myListOfAnnotations.toArray(new Annotation[0]), annotationClass);
    }

    /**
     * @return Whether a given list of Annotation contains a certain annotation type.
     */
    @SuppressWarnings({"WeakerAccess", "unchecked"})
    @Nullable
    public static <T extends Annotation> T findAnnotation(Annotation[] myListOfAnnotations, Class<T> annotationClass) {
        for (Annotation annotation : myListOfAnnotations) {
            if (annotation.annotationType() == annotationClass) {
                return (T) annotation;
            }
        }
        return null;
    }
	
	/**
	 * Shortcut helper method that replaces an item in an array and returns the array itself.
	 *
	 * @param <T> The type of object that goes into the array.
	 * @param array The array that needs an item replaced.
	 * @param index The index at which the new value should be inserted.
	 * @param value The value to insert at the specified index in the specified array.
	 * @return The specified array with the item replaced at specified index.
	 */
	@NotNull
	static <T> T[] replaceInArray(final T[] array, final int index, final T value) {
		array[index] = value;
		return array;
	}
}
