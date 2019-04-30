package org.bbottema.javareflection;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.model.InvokableObject;
import org.bbottema.javareflection.model.LookupMode;
import org.bbottema.javareflection.model.MethodModifier;
import org.bbottema.javareflection.util.MiscUtil;
import org.bbottema.javareflection.valueconverter.IncompatibleTypeException;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.bbottema.javareflection.LookupCaches.METHOD_CACHE;
import static org.bbottema.javareflection.util.MiscUtil.trustedCast;
import static org.bbottema.javareflection.util.MiscUtil.trustedNullableCast;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This reflection tool is designed to perform advanced method or constructor lookups,
 * using a combination of {@link LookupMode} strategies.
 * <p>
 * It tries to find a constructor of a given datatype, with a given argument
 * datatypelist, where types do not have to match formal types (auto-boxing, supertypes, implemented interfaces and type conversions are allowed as
 * they are included in the lookup cycles). This expanded version tries a simple call first (exact match, which is provided natively by the Java) and
 * when this fails, it generates a list of datatype arrays (signatures) with all possible versions of any type in the original list possible, and
 * combinations thereof.
 * <p>
 * <strong>Observe the following (trivial) example:</strong>
 *
 * <pre>
 * 	interface Foo {
 * 		void foo(Double value, Fruit fruit, char c);
 *        }
 * 	abstract class A implements Foo {
 *    }
 * 	abstract class B extends A {
 *    }
 *
 * ClassUtils.findCompatibleJavaMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Pear.class, String.class)}
 * </pre>
 * <p>
 * In the above example, the method foo will be found by finding all methods named "Foo" on the interfaces implemented by supertype <code>A</code>,
 * and then foo's method signature will be matched using autoboxing on the <code>double</code> type, a cast to the <code>Fruit</code> supertype for
 * the <code>Pear</code> type and finally by attempting a common conversion from <code>String</code> to <code>char</code>. This will give you a Java
 * {@link Method}, but you won't be able to invoke it if it was found using a less strict lookup than one with a simple exact match. There are two
 * ways to do this: use {@link #invokeCompatibleMethod(Object, Class, String, Object...)} instead or perform the conversion yourself using {@link
 * ValueConversionHelper#convert(Object[], Class[], boolean)} prior to invoking the method. <code>ValueConverter.convert(args,
 * method.getParameterTypes())</code>.
 * <p>
 * A reverse lookup is also possible: given an ordered list of possible types, is a given <code>Method</code> compatible?
 * <p>
 * Because this lookup is potentially very expensive, a cache is present to store lookup results.
 */
@UtilityClass
public final class MethodUtils {
	
	private static final Logger LOGGER = getLogger(MethodUtils.class);

    @Nullable
    @SuppressWarnings({"unchecked"})
    public static <T> T invokeMethodSimple(final Method method, final Object subject, final Object... args) {
        try {
            return (T) method.invoke(subject, args);
        } catch (SecurityException e) {
            throw new RuntimeException("unable to invoke method; security problem", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("unable to access method", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("unable to invoke method", e);
        }
    }

	/**
     * Locates a method on an Object using serveral searchmodes for optimization. First of all a {@link Method} cache is being maintained to quickly
     * fetch heavily used methods. If not cached before and if a simple search (autoboxing and supertype casts) fails a more complex search is done
     * where all interfaces are searched for the method as well. If this fails as well, this method will try to autoconvert the types of the arguments
     * and find a matching signature that way.
     * 
     * @param context The object to call the method from (can be null).
     * @param datatype The class to find the method on.
     * @param identifier The name of the method to locate.
     * @param args A list of [non-formal] arguments.
     * @return The return value of the invoke method, if successful.
     * @throws NoSuchMethodException Thrown by {@link #findCompatibleMethod(Class, String, EnumSet, Class...)}.
     * @throws IllegalArgumentException Thrown by {@link Method#invoke(Object, Object...)}.
     * @throws IllegalAccessException Thrown by {@link Method#invoke(Object, Object...)}.
     * @throws InvocationTargetException Thrown by {@link Method#invoke(Object, Object...)}.
     */
    @SuppressWarnings({"WeakerAccess"})
	@Nullable
    public static <T> T invokeCompatibleMethod(@Nullable final Object context, final Class<?> datatype, final String identifier, final Object... args)
            throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        // determine the signature we want to find a compatible java method for
        final Class<?>[] parameterSignature = TypeUtils.collectTypes(args);

        // setup lookup procedure starting with simple search mode
        EnumSet<LookupMode> lookupMode = EnumSet.of(LookupMode.AUTOBOX, LookupMode.CAST_TO_SUPER);
		Set<InvokableObject<Method>> iMethods;

        // try to find a compatible Java method using various lookup modes
        try {
            iMethods = findCompatibleMethod(datatype, identifier, lookupMode, parameterSignature);
        } catch (final NoSuchMethodException e1) {
            try {
                // moderate search mode
                lookupMode.add(LookupMode.CAST_TO_INTERFACE);
                iMethods = findCompatibleMethod(datatype, identifier, lookupMode, parameterSignature);
            } catch (final NoSuchMethodException e2) {
				try {
					// limited conversions searchmode
					lookupMode.add(LookupMode.COMMON_CONVERT);
					iMethods = findCompatibleMethod(datatype, identifier, lookupMode, parameterSignature);
				} catch (NoSuchMethodException e3) {
					// full searchmode
					lookupMode.add(LookupMode.SMART_CONVERT);
					iMethods = findCompatibleMethod(datatype, identifier, lookupMode, parameterSignature);
				}
			}
        }

		for (InvokableObject<Method> iMethod : iMethods) {
			iMethod.getMethod().setAccessible(true);

			try {
				Object[] convertedArgs = ValueConversionHelper.convert(args, iMethod.getCompatibleSignature(), false);
				return trustedNullableCast(iMethod.getMethod().invoke(context, convertedArgs));
			} catch (IncompatibleTypeException e) {
				// keep trying conversion candidates...
			}
		}

		LOGGER.error(format("Was unable to find a suitable method on %s for the parameter signature %s", datatype, Arrays.toString(parameterSignature)));
		throw new NoSuchMethodException();
    }

    /**
     * Locates and invokes a {@link Constructor}using {@link #invokeConstructor(Class, Class[], Object[])}
     * 
     * @param <T> Used to parameterize the returned object so that the caller doesn't need to cast.
     * @param datatype The class to find the constructor for.
     * @param args A list of [non-formal] arguments.
     * @return The instantiated object of class datatype.
     * @throws IllegalAccessException Thrown by {@link #invokeConstructor(Class, Class[], Object[])}.
     * @throws InvocationTargetException Thrown by {@link #invokeConstructor(Class, Class[], Object[])}.
     * @throws InstantiationException Thrown by {@link #invokeConstructor(Class, Class[], Object[])}.
     * @throws NoSuchMethodException Thrown by {@link #invokeConstructor(Class, Class[], Object[])}.
     * @see java.lang.reflect.Constructor#newInstance(Object[])
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "unused"})
	@NotNull
    public static <T> T invokeCompatibleConstructor(final Class<T> datatype, final Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        final Class<?>[] parameterList = TypeUtils.collectTypes(args);
        return invokeConstructor(datatype, parameterList, args);
    }

    /**
     * Locates and invokes a {@link Constructor}, using a customized typelist. Avoids dynamically trying to find correct parameter type list. Can also
     * be used to force up/down casting (ie. passing a specific type of <code>List</code> into a generic type)
     * 
     * @param <T> Used to parameterize the returned object so that the caller doesn't need to cast.
     * @param datatype The class to find the constructor for.
     * @param parameterSignature The typelist used to find correct constructor.
     * @param args A list of [non-formal] arguments.
     * @return The instantiated object of class datatype.
     * @throws IllegalAccessException Thrown by {@link Constructor#newInstance(Object...)}.
     * @throws InvocationTargetException Thrown by {@link Constructor#newInstance(Object...)}.
     * @throws InstantiationException Thrown by {@link Constructor#newInstance(Object...)}.
     * @throws NoSuchMethodException Thrown by {@link #findCompatibleConstructor(Class, EnumSet, Class...)}.
     * @see java.lang.reflect.Constructor#newInstance(Object[])
     */
    @SuppressWarnings("WeakerAccess")
	@NotNull
    public static <T> T invokeConstructor(final Class<T> datatype, final Class<?>[] parameterSignature, final Object[] args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        // setup lookup procedure
        EnumSet<LookupMode> lookupMode = EnumSet.of(LookupMode.AUTOBOX, LookupMode.CAST_TO_SUPER);
        Set<InvokableObject<Constructor>> iConstructors;

        // try to find a compatible Java constructor
        try {
            iConstructors = findCompatibleConstructor(datatype, lookupMode, parameterSignature);
        } catch (final NoSuchMethodException e1) {
            try {
                lookupMode.add(LookupMode.CAST_TO_INTERFACE);
                iConstructors = findCompatibleConstructor(datatype, lookupMode, parameterSignature);
            } catch (final NoSuchMethodException e2) {
				try {
					lookupMode.add(LookupMode.COMMON_CONVERT);
					iConstructors = findCompatibleConstructor(datatype, lookupMode, parameterSignature);
	            } catch (final NoSuchMethodException e3) {
					lookupMode.add(LookupMode.SMART_CONVERT);
					iConstructors = findCompatibleConstructor(datatype, lookupMode, parameterSignature);
				}
            }
        }

		for (InvokableObject<Constructor> iConstructor : iConstructors) {
			try {
				Object[] convertedArgs = ValueConversionHelper.convert(args, iConstructor.getCompatibleSignature(), false);
				return trustedCast(iConstructor.getMethod().newInstance(convertedArgs));
			} catch (IncompatibleTypeException e) {
				// keep trying conversion candidates...
			}
		}

		LOGGER.error(format("Was unable to find a suitable constructor on %s for the parameter signature %s", datatype, Arrays.toString(parameterSignature)));
		throw new NoSuchMethodException();
    }

    /**
     * Tries to find a {@link Constructor} of a given type, with a given typelist, where types do not match due to formal types <!=> simple types.
     * This expanded version tries a simple call first and when it fails, it generates a list of type arrays with all possible (un)wraps of any type
     * in the original list possible, and combinations thereof.
     * 
     * @param <T> Used to parameterize the returned constructor.
     * @param datatype The class to get the constructor from.
     * @param lookupMode Flag indicating the search steps that need to be done.
     * @param signature The list of types as specified by the user.
     * @return The constructor if found, otherwise exception is thrown.
     * @exception NoSuchMethodException Thrown when the {@link Constructor} could not be found on the data type, even after performing optional
     *                conversions.
     */
    @SuppressWarnings({"WeakerAccess"})
	public static <T> Set<InvokableObject<Constructor>> findCompatibleConstructor(final Class<T> datatype, final EnumSet<LookupMode> lookupMode, final Class<?>... signature)
            throws NoSuchMethodException {
        // first try to find the constructor in the method cache
        Set<InvokableObject<Constructor>> iConstructors = getConstructorFromCache(datatype, datatype.getName(), signature);
        if (iConstructors != null) {
            return iConstructors;
        } else {
			iConstructors = new HashSet<>();

            try {
                // try standard call
                iConstructors.add(new InvokableObject<Constructor>(datatype.getConstructor(signature), signature, signature));
            } catch (final NoSuchMethodException e) {
				for (final Class<?>[] compatibleSignature : TypeUtils.generateCompatibleTypeLists(lookupMode, signature)) {
                    try {
						iConstructors.add(new InvokableObject<Constructor>(datatype.getConstructor(compatibleSignature), signature, compatibleSignature));
                    } catch (final NoSuchMethodException x) {
                        // do nothing
                    }
                }
            }
        }

        if (!iConstructors.isEmpty()) {
            return addMethodToCache(datatype, datatype.getName(), iConstructors, signature);
        } else {
            throw new NoSuchMethodException();
        }
    }

    /**
     * Delegates to {@link #findCompatibleMethod(Class, String, EnumSet, Class...)}, using strict lookupmode (no autoboxing, casting etc.) and
     * optional signature parameters.
     * 
     * @param datatype The class to get the constructor from.
     * @param methodName The name of the method to retrieve from the class.
     * @param signature The list of types as specified by the user.
     * @return <code>null</code> in case of a <code>NoSuchMethodException</code> exception.
     * @see #findCompatibleMethod(Class, String, EnumSet, Class...)
     */
    @NotNull
    @SuppressWarnings("WeakerAccess")
    public static Set<InvokableObject<Method>> findSimpleCompatibleMethod(final Class<?> datatype, final String methodName, final Class<?>... signature) {
        try {
            return findCompatibleMethod(datatype, methodName, EnumSet.noneOf(LookupMode.class), signature);
        } catch (final NoSuchMethodException e) {
            return new HashSet<>();
        }
    }

    /**
     * Same as <code>getConstructor()</code>, except for getting a {@link Method} of a classtype, using the name to indicate which method should be
     * located.
     * 
     * @param datatype The class to get the constructor from.
     * @param methodName The name of the method to retrieve from the class.
     * @param lookupMode Flag indicating the search steps that need to be done.
     * @param signature The list of types as specified by the user.
     * @return The method if found, otherwise exception is thrown.
     * @exception NoSuchMethodException Thrown when the {@link Method} could not be found on the data type, even after performing optional
     *                conversions.
     */
    @NotNull
    @SuppressWarnings("WeakerAccess")
    public static Set<InvokableObject<Method>> findCompatibleMethod(final Class<?> datatype, final String methodName, final EnumSet<LookupMode> lookupMode,
															   final Class<?>... signature) throws NoSuchMethodException {
        // first try to find the method in the method cache
        Set<InvokableObject<Method>> iMethods = getMethodFromCache(datatype, methodName, signature);
        if (iMethods != null) {
            return iMethods;
        } else {
        	iMethods = new HashSet<>();
            try {
                // try standard call
                iMethods.add(new InvokableObject<>(getMethod(datatype, methodName, signature), signature, signature));
            } catch (final NoSuchMethodException e) {
				for (final Class<?>[] compatibleSignature : TypeUtils.generateCompatibleTypeLists(lookupMode, signature)) {
                    try {
						iMethods.add(new InvokableObject<>(getMethod(datatype, methodName, compatibleSignature), signature, compatibleSignature));
                    } catch (final NoSuchMethodException x) {
                        // do nothing
                    }
                }
            }
        }

        if (!iMethods.isEmpty()) {
            return addMethodToCache(datatype, methodName, iMethods, signature);
        } else {
            throw new NoSuchMethodException();
        }
    }

    /**
     * Searches a specific class object for a {@link Method} using java reflect using a specific signature. This method will first search all
     * implemented interfaces for the method to avoid visibility problems.<br />
     * <br />
     * An example of such a problem is the <code>Iterator</code> as implemented by the <code>ArrayList</code>. The Iterator is implemented as a
     * private innerclass and as such not accessible by java reflect (even though the implemented methods are declared <i>public</i>), unlike the
     * interface's definition.
     * 
     * @param datatype The class reference to locate the method on.
     * @param name The name of the method to find.
     * @param signature The signature the method should match.
     * @return The Method found on the data type that matched the specified signature.
     * @exception NoSuchMethodException Thrown when the {@link Method} could not be found on the interfaces implemented by the given data type.
     * @see java.lang.Class#getMethod(String, Class[])
     */
    @SuppressWarnings("WeakerAccess")
	@NotNull
	public static Method getMethod(final Class<?> datatype, final String name, final Class<?>... signature) throws NoSuchMethodException {
        for (final Class<?> iface : datatype.getInterfaces()) {
            try {
                return iface.getMethod(name, signature);
            } catch (final NoSuchMethodException e) {
                // do nothing
            }
        }
        try {
            return datatype.getMethod(name, signature);
        } catch (final NoSuchMethodException e) {
            return datatype.getDeclaredMethod(name, signature);
        }
    }
    
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static boolean isMethodCompatible(Method method, EnumSet<LookupMode> lookupMode, final Class<?>... signature) {
		final Class<?>[] targetSignature = method.getParameterTypes();
		if (signature.length != targetSignature.length) {
			return false;
		}
		return TypeUtils.isTypeListCompatible(signature, targetSignature, lookupMode);
	}

    /**
     * Retrieves a {@link Method} from a cache.
     * 
     * @param datatype The owning {@link Class} of the <code>Method</code> being searched for.
     * @param method The name of the method that is being searched for.
     * @param signature The parameter list of the method we need to match if a method was found by name.
     * @return The <code>Method</code> found on the specified owner with matching name and signature.
     * @see LookupCaches#METHOD_CACHE
     * @see MethodUtils#addMethodToCache(Class, String, Set, Class[])
     */
    @Nullable
    private static <T> Set<InvokableObject> getInvokableObjectFromCache(final Class<T> datatype, final String method, final Class<?>... signature) {
        final Map<String, Map<Class<?>[], Set<InvokableObject>>> owner = METHOD_CACHE.get(datatype);
        // we know only methods with parameter list are stored in the cache
        if (signature.length > 0) {
            // get owner, its methods matching specified name and match their signatures
            if (owner != null && owner.containsKey(method)) {
            	return owner.get(method).get(signature);
            }
        }
        // method not found or known not to be stored due to absent parameter list
        return null;
    }
	
	@Nullable
	private static <T> Set<InvokableObject<Method>> getMethodFromCache(final Class<T> datatype, final String method, final Class<?>... signature) {
		return trustedNullableCast(getInvokableObjectFromCache(datatype, method, signature));
	}

	@Nullable
	private static <T> Set<InvokableObject<Constructor>> getConstructorFromCache(final Class<T> datatype, final String method, final Class<?>... signature) {
		return trustedNullableCast(getInvokableObjectFromCache(datatype, method, signature));
	}

    /**
     * Adds a specific <code>Method</code> to the cache.
     * 
     * @param datatype The <code>Class</code> that owns the <code>Method</code>.
     * @param method The <code>Method</code>'s name by which methods can be found on the specified owner.
     * @param methodInvocationCandidates The <code>Method</code> reference that's actually being stored in the cache.
     * @param signature The parameter list of the <code>Method</code> being stored.
     * @see LookupCaches#METHOD_CACHE
     * @see MethodUtils#getMethodFromCache(Class, String, Class...)
     */
	private static <T extends InvokableObject<T2>, T2 extends AccessibleObject> Set<T> addMethodToCache(final Class<?> datatype, final String method,
			final Set<T> methodInvocationCandidates, final Class<?>... signature) {
        // only store methods with a parameter list
        if (signature.length > 0) {
            // get or create owner entry
            Map<String, Map<Class<?>[], Set<InvokableObject>>> owner = METHOD_CACHE.get(datatype);
            owner = owner != null ? owner : new LinkedHashMap<String, Map<Class<?>[], Set<InvokableObject>>>();
            // get or create list of methods with specified method name
            Map<Class<?>[], Set<InvokableObject>> methods = owner.get(method);
            methods = methods != null ? methods : new LinkedHashMap<Class<?>[], Set<InvokableObject>>();
            // add or overwrite method entry
            methods.put(signature, MiscUtil.<Set<InvokableObject>>trustedCast(methodInvocationCandidates));
            // finally shelve all the stuff back
            methods.put(signature, MiscUtil.<Set<InvokableObject>>trustedCast(methodInvocationCandidates));
            owner.put(method, methods);
            METHOD_CACHE.put(datatype, owner);
        }
        return methodInvocationCandidates;
    }
	
	
	/**
	 * Delegates to {@link #findMatchingMethods(Class, Class, String, String...)}
	 */
	@SuppressWarnings({ "unused", "WeakerAccess" })
	public static Set<Method> findMatchingMethods(final Class<?> datatype, @Nullable Class<?> boundaryMarker, String methodName, List<String> paramTypeNames) {
		return findMatchingMethods(datatype, boundaryMarker, methodName, paramTypeNames.toArray(new String[0]));
	}
	
	/**
	 * @return Methods found using {@link ClassUtils#collectMethods(Class, Class, EnumSet)}
	 * and then filters based on the parameter <em>type</em> names.
	 */
	@SuppressWarnings({ "unused", "WeakerAccess" })
	public static Set<Method> findMatchingMethods(final Class<?> datatype, @Nullable Class<?> boundaryMarker, String methodName, String... paramTypeNames) {
    	Set<Method> matchingMethods = new HashSet<>();
		for (Method method : ClassUtils.collectMethods(datatype, boundaryMarker, MethodModifier.MATCH_ANY)) {
			Class<?>[] methodParameterTypes = method.getParameterTypes();
			if (method.getName().equals(methodName) &&
					methodParameterTypes.length == paramTypeNames.length &&
					typeNamesMatch(methodParameterTypes, paramTypeNames)) {
				matchingMethods.add(method);
			}
		}
		return matchingMethods;
	}
	
	private static boolean typeNamesMatch(Class<?>[] parameterTypes, String[] typeNamesToMatch) {
		for (int i = 0; i < parameterTypes.length; i++) {
			final Class<?> parameterType = parameterTypes[i];
			final String typeNameToMatch = typeNamesToMatch[i];
			if (parameterType.isArray()) {
				final String arrayTypeNameToMatch = typeNameToMatch.endsWith("...")
						? typeNameToMatch.substring(0, typeNameToMatch.indexOf("..."))
						: typeNameToMatch;
				if (typeNamesDontMatch(parameterType.getComponentType(), arrayTypeNameToMatch)) {
					return false;
				}
			} else if (typeNamesDontMatch(parameterType, typeNameToMatch)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean typeNamesDontMatch(Class<?> parameterType, String typeNameToMatch) {
		return !parameterType.getName().equals(typeNameToMatch) && !parameterType.getSimpleName().equals(typeNameToMatch);
	}
	
	/**
	 * @return True if the given method contains a parameter that is an array of an {@link Iterable}.
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean methodHasCollectionParameter(final Method m) {
		for (Class<?> parameterType : m.getParameterTypes()) {
			if (parameterType.isArray() ||
					Iterable.class.isAssignableFrom(parameterType) ||
					Map.class.isAssignableFrom(parameterType)) {
				return true;
			}
		}
		return false;
	}
}