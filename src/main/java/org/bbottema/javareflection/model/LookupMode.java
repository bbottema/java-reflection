package org.bbottema.javareflection.model;

import org.bbottema.javareflection.valueconverter.ValueConversionHelper;

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
	 * Indicates that looking for methods includes trying to find compatible signatures by automatically convert the specified arguments.
	 */
	COMMON_CONVERT
}
