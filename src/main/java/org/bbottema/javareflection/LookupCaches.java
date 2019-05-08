package org.bbottema.javareflection;

import org.bbottema.javareflection.model.InvokableObject;
import org.bbottema.javareflection.model.LookupMode;
import org.bbottema.javareflection.util.ArrayKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * For internal use for improving repeated lookup performances.
 */
public class LookupCaches {
	
	/**
	 * {@link Class} cache optionally used when looking up classes with {@link ClassUtils#locateClass(String, boolean, ClassLoader)}.
	 */
	final static Map<String, Class<?>> CLASS_CACHE = new HashMap<>();
	
	/**
	 * {@link Method} cache categorized by owning <code>Classes</code> (since several owners can have a method with the same name and signature).
	 * Methods are stored based on <code>Method</code> reference along with their unique signature (per owner), so multiple methods on one owner with
	 * the same name can coexist.<br />
	 * <br />
	 * This cache is being maintained to reduce lookup times when trying to find signature compatible Java methods. The possible signature
	 * combinations using autoboxing and/or automatic common conversions can become very large (7000+ with only three parameters) and can become a
	 * real problem. The more frequently a method is being called the larger the performance gain, especially for methods with long parameter lists
	 *
	 * @see MethodUtils#addMethodToCache(Class, String, Set, Class[])
	 * @see MethodUtils#getMethodFromCache(Class, String, Class[])
	 */
	final static Map<Class<?>, Map<String, Map<Class<?>[], Set<InvokableObject>>>> METHOD_CACHE = new LinkedHashMap<>();
	
	static final Map<Class<?>, Set<Class<?>>> CACHED_REGISTERED_COMPATIBLE_TARGET_TYPES = new HashMap<>();
	static final Map<Class<?>, Set<Class<?>>> CACHED_COMPATIBLE_TARGET_TYPES = new HashMap<>();
	static final Map<Set<LookupMode>, Map<ArrayKey, List<Class<?>[]>>> CACHED_COMPATIBLE_TYPE_LISTS = new HashMap<>();
	
	@SuppressWarnings({"WeakerAccess", "unused"})
	public static void resetCache() {
		CLASS_CACHE.clear();
		METHOD_CACHE.clear();
		CACHED_REGISTERED_COMPATIBLE_TARGET_TYPES.clear();
		CACHED_COMPATIBLE_TARGET_TYPES.clear();
		CACHED_COMPATIBLE_TYPE_LISTS.clear();
	}
	
	@Nullable
	static List<Class<?>[]> getCachedCompatibleSignatures(Set<LookupMode> lookupMode, ArrayKey arrayKey) {
		final Map<ArrayKey, List<Class<?>[]>> cachedCompatibleSignatures = CACHED_COMPATIBLE_TYPE_LISTS.get(lookupMode);
		if (cachedCompatibleSignatures != null) {
			final List<Class<?>[]> cachedResult = cachedCompatibleSignatures.get(arrayKey);
			if (cachedResult != null) {
				return cachedResult;
			}
		}
		return null;
	}
	
	@NotNull
	static List<Class<?>[]> addCompatiblesignaturesToCache(Set<LookupMode> lookupMode, ArrayKey arrayKey, List<Class<?>[]> compatibleTypeLists) {
		Map<ArrayKey, List<Class<?>[]>> cachedCompatibleSignatures = CACHED_COMPATIBLE_TYPE_LISTS.get(lookupMode);
		if (cachedCompatibleSignatures == null) {
			CACHED_COMPATIBLE_TYPE_LISTS.put(lookupMode, cachedCompatibleSignatures = new HashMap<>());
		}
		cachedCompatibleSignatures.put(arrayKey, compatibleTypeLists);
		return compatibleTypeLists;
	}
}