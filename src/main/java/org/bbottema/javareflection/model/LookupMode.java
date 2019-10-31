package org.bbottema.javareflection.model;

import org.bbottema.javareflection.valueconverter.ValueConversionHelper;

import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static java.util.EnumSet.allOf;
import static java.util.EnumSet.of;

/**
 * Defines lookup modes for matching Java methods and constructors. Each time a lookup failed on signature type, a less strict lookup
 * is performed, in the following order (signature means: the list of parameters defined for a method or constructor):
 * <ol>
 * <li><strong>exact matching</strong>: the given type should exactly match the found types during lookup. This lookup cycle is always performed
 * first.</li>
 * <li><strong>autobox matching</strong>: the given type should match its boxed/unboxed version.</li>
 * <li><strong>polymorphic interface matching</strong>: the given type should match one of the implemented interfaces</li>
 * <li><strong>polymorphic superclass matching</strong>: the given type should match one of the super classes (for each superclass, the cycle is
 * repeated, so exact and interface matching come first again before the next superclass up in the chain)</li>
 * <li><strong>common conversion matching</strong>: if all other lookups fail, one last resort is to try to convert the given type, if a common
 * type, to any other common type and then try to find a matching method or constructor. See {@link ValueConversionHelper} for more on the possibilities.
 * </li>
 * </ol>
 */
public enum LookupMode {
	/**
	 * Indicates that looking for methods includes trying to find compatible signatures by autoboxing the specified arguments.
	 */
	AUTOBOX,
	/**
	 * Indicates that looking for methods includes trying to find compatible signatures by casting the specified arguments to a super type.
	 */
	CAST_TO_SUPER,
	/**
	 * Indicates that looking for methods includes trying to find compatible signatures by casting the specified arguments to an implemented
	 * interface.
	 */
	CAST_TO_INTERFACE,
	/**
	 * Indicates that looking for methods includes trying to find compatible signatures by automatically converting the specified arguments.
	 */
	COMMON_CONVERT,
	/**
	 * Like {@link #COMMON_CONVERT}, but now takes the registered converters and continues finding a conversion path based on the previous outcomes.
	 * <p>
	 * Examples:
	 * <ul>
	 *     <li>Joda date object -> String -> Java8 date object. This would require only the toJava8 date object converter to work</li>
	 *     <li>Calendar -> String -> char. Calendar is compatible with String and String with char, but conversion will fail of course.</li>
	 *     <li>Boolean -> Character -> long. This simply works out of the box, resulting in 0 or 1.</li>
	 * </ul>
	 */
	SMART_CONVERT;

	/**
	 * Defines a simple method lookup configuration that goes as far as casting and autoboxing, but no actual conversions are done to the values.
	 */
	public static final Set<LookupMode> SIMPLE = of(AUTOBOX, CAST_TO_SUPER, CAST_TO_INTERFACE);

	/**
	 * Defines a complete method lookup configuration that combines all possible lookup modes.
	 */
	public static final Set<LookupMode> FULL = unmodifiableSet(allOf(LookupMode.class));
}
