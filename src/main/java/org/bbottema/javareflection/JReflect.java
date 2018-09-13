package org.bbottema.javareflection;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class utilizes functionality of the Java class <code>java.lang.reflect</code>. Specifically, this reflection tool is designed to perform
 * advanced method or constructor lookups, using a combination of {@link LookupMode} strategies.
 * <p>
 * Aside from that there are some nifty methods for things such as:
 * <ul>
 * <li>finding out if a given string matches a package name ({@link #isPackage(String)}),</li>
 * <li>returning the widest number type that will fit a any number on a given list of numbers ({@link #widestNumberClass(Number...)}),</li>
 * <li>a method to autobox a given value to its counterpart ({@link #autobox(Class)}),</li>
 * <li>A couple of methods to resolve a direct property of a given object (though they may move to {@link FieldUtils}),</li>
 * <li>Various handy methods for collecting information on a given <code>Object</code>, such as list of methods, properties or types</li>
 * <li>An advanced <code>Class</code> lookup ({@link #locateClass(String, boolean, ExternalClassLoader)}), that allows a full scan (to try all
 * packages known) and an optional {@link ExternalClassLoader} instance that is able to actually compile a .java file on the fly and load its compile
 * .class file</li>
 * </ul>
 * <strong>About the method / constructor lookup facility:</strong>
 * <p>
 * An expanded version of <code>getConstructor</code> is implemented that tries to find a constructor of a given datatype, with a given argument
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
 * 	JReflect.findCompatibleJavaMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Pear.class, String.class)}
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
 * <p>
 *
 * @author Benny Bottema
 * @see ValueConversionHelper
 * @see FieldUtils
 * @see ExternalClassLoader
 */

@UtilityClass
public final class JReflect {

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
     * 
     * @author Benny Bottema
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

    /**
     * {@link Method} cache categorized by owning <code>Classes</code> (since several owners can have a method with the same name and signature).
     * Methods are stored based on <code>Method</code> reference along with their unique signature (per owner), so multiple methods on one owner with
     * the same name can coexist.<br />
     * <br />
     * This cache is being maintained to reduce lookup times when trying to find signature compatible Java methods. The possible signature
     * combinations using autoboxing and/or automatic common conversions can become very large (7000+ with only three parameters) and can become a
     * real problem. The more frequently a method is being called the larger the performance gain, especially for methods with long parameter lists
     * 
     * @see JReflect#addMethodToCache(Class, String, AccessibleObject, Class[])
     * @see JReflect#getMethodFromCache(Class, String, Class[])
     */
    private final static Map<Class<?>, Map<String, Map<AccessibleObject, Class<?>[]>>> methodCache = new LinkedHashMap<>();

    /**
     * {@link Class} cache optionally used when looking up classes with {@link #locateClass(String, boolean, ExternalClassLoader)}.
     */
    private final static Map<String, Class<?>> classCache = new HashMap<>();

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
    
    @SuppressWarnings("WeakerAccess")
    public static void resetCaches() {
    	classCache.clear();
    	methodCache.clear();
	}
	
	/**
	 * Searches the JVM and optionally all of its packages
	 *
	 * @param className The name of the class to locate.
	 * @param fullscan Whether a full scan through all available java packages is required.
	 * @param classLoader Optional user-provided classloader.
	 * @return The <code>Class</code> reference if found or <code>null</code> otherwise.
	 */
    @Nullable
    @SuppressWarnings("WeakerAccess")
    public static Class<?> locateClass(final String className, final boolean fullscan, @Nullable final ExternalClassLoader classLoader) {
		final String cacheKey = className + fullscan;
		if (classCache.containsKey(cacheKey)) {
			return classCache.get(cacheKey);
		}
		Class<?> _class = null;
		if (fullscan) {
			// cycle through all packages and try allocating dynamically
			final Package[] ps = Package.getPackages();
			for (int i = 0; i < ps.length && _class == null; i++) {
				_class = locateClass(ps[i].getName() + "." + className, classLoader);
			}
		} else {
			// try standard package used for most common classes
			_class = locateClass("java.lang." + className, classLoader);
		}
		classCache.put(cacheKey, _class);
		return _class;
	}
	
	/**
     * This function dynamically tries to locate a class. First it searches the class-cache list, then it tries to get it from the Virtual Machine
     * using {@code Class.forName(String)}.
     * 
     * @param fullClassName The <code>Class</code> that needs to be found.
     * @param classLoader Optional user-provided classloader.
     * @return The {@code Class} object found from cache or VM.
     */
    @SuppressWarnings("WeakerAccess")
	@Nullable
	public static Class<?> locateClass(final String fullClassName, @Nullable final ExternalClassLoader classLoader) {
        try {
            Class<?> _class = null;
            if (classLoader != null) {
                _class = classLoader.loadClass(fullClassName);
            }
            if (_class == null) {
                _class = Class.forName(fullClassName);
            }
            return _class;
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Simply calls {@link Class#newInstance()} and hides the exception handling boilerplate code.
     * 
     * @param _class The datatype for which we need to create a new instance of.
     * @param <T> Type used to parameterize the return instance.
     * @return A new parameterized instance of the given type.
     */
    @NotNull
    @SuppressWarnings("WeakerAccess")
    public static <T> T newInstanceSimple(final Class<T> _class) {
        try {
			return _class.getConstructor().newInstance();
		} catch (SecurityException e) {
            throw new RuntimeException("unable to invoke parameterless constructor; security problem", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("unable to complete instantiation of object", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("unable to access parameterless constructor", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("unable to invoke parameterless constructor", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("unable to find parameterless constructor (not public?)", e);
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
    @SuppressWarnings({"unchecked", "WeakerAccess"})
	@Nullable
    public static <T> T invokeCompatibleMethod(final Object context, final Class<?> datatype, final String identifier, final Object... args)
            throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        // determine the signature we want to find a compatible java method for
        final Class<?>[] signature = JReflect.collectTypes(args);

        // setup lookup procedure starting with simple search mode
        EnumSet<LookupMode> lookupMode = EnumSet.of(LookupMode.AUTOBOX, LookupMode.CAST_TO_SUPER);
        Method method;

        // try to find a compatible Java method using various lookup modes
        try {
            method = findCompatibleMethod(datatype, identifier, lookupMode, signature);
        } catch (final NoSuchMethodException e1) {
            try {
                // moderate search mode
                lookupMode.add(LookupMode.CAST_TO_INTERFACE);
                method = findCompatibleMethod(datatype, identifier, lookupMode, signature);
            } catch (final NoSuchMethodException e2) {
                // full searchmode
                lookupMode.add(LookupMode.COMMON_CONVERT);
                method = findCompatibleMethod(datatype, identifier, lookupMode, signature);
            }
        }

        method.setAccessible(true);

        // try to invoke the method with unconverted arguments or convert them if needed
        try {
            return (T) method.invoke(context, args);
        } catch (final IllegalArgumentException e) {
            final Object[] convertedArgs = ValueConversionHelper.convert(args, method.getParameterTypes(), true);
            return (T) method.invoke(context, convertedArgs);
        }
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
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
	@NotNull
    public static <T> T invokeCompatibleConstructor(final Class<T> datatype, final Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        final Class<?>[] signature = JReflect.collectTypes(args);
        return invokeConstructor(datatype, signature, args);
    }

    /**
     * Locates and invokes a {@link Constructor}, using a customized typelist. Avoids dynamically trying to find correct parameter type list. Can also
     * be used to force up/down casting (ie. passing a specific type of <code>List</code> into a generic type)
     * 
     * @param <T> Used to parameterize the returned object so that the caller doesn't need to cast.
     * @param datatype The class to find the constructor for.
     * @param signature The typelist used to find correct constructor.
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
    public static <T> T invokeConstructor(final Class<T> datatype, final Class<?>[] signature, final Object[] args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        // setup lookup procedure
        EnumSet<LookupMode> lookupMode = EnumSet.of(LookupMode.AUTOBOX, LookupMode.CAST_TO_SUPER);
        Constructor<T> constructor;

        // try to find a compatible Java constructor
        try {
            // simple search mode
            constructor = findCompatibleConstructor(datatype, lookupMode, signature);
        } catch (final NoSuchMethodException e1) {
            try {
                // moderate search mode
                lookupMode.add(LookupMode.CAST_TO_INTERFACE);
                constructor = findCompatibleConstructor(datatype, lookupMode, signature);
            } catch (final NoSuchMethodException e2) {
                // full searchmode
                lookupMode.add(LookupMode.COMMON_CONVERT);
                constructor = findCompatibleConstructor(datatype, lookupMode, signature);
            }
        }

        // try to invoke the constructor with unconverted arguments or convert them if needed
        try {
            return constructor.newInstance(args);
        } catch (final IllegalArgumentException e) {
            final Object[] convertedArgs = ValueConversionHelper.convert(args, constructor.getParameterTypes(), true);
            return constructor.newInstance(convertedArgs);
        }
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
            types[i] = o != null ? o.getClass() : Object.class;
        }
        return types;
    }

    /**
     * Tries to find a {@link Constructor} of a given type, with a given typelist, where types do not match due to formal types <!=> simple types.
     * This expanded version tries a simple call first and when it fails, it generates a list of type arrays with all possible (un)wraps of any type
     * in the original list possible, and combinations thereof.
     * 
     * @param <T> Used to parameterize the returned constructor.
     * @param datatype The class to get the constructor from.
     * @param lookupMode Flag indicating the search steps that need to be done.
     * @param types The list of types as specified by the user.
     * @return The constructor if found, otherwise exception is thrown.
     * @exception NoSuchMethodException Thrown when the {@link Constructor} could not be found on the data type, even after performing optional
     *                conversions.
     */
    @SuppressWarnings({"unchecked", "WeakerAccess"})
	@NotNull
    public static <T> Constructor<T> findCompatibleConstructor(final Class<T> datatype, final EnumSet<LookupMode> lookupMode, final Class<?>... types)
            throws NoSuchMethodException {
        // first try to find the constructor in the method cache
        Constructor<T> constructor = (Constructor<T>) getMethodFromCache(datatype, datatype.getName(), types);
        if (constructor != null) {
            return constructor;
        } else {
            try {
                // try standard call
                constructor = datatype.getConstructor(types);
            } catch (final NoSuchMethodException e) {
                // failed, try all possible wraps/unwraps
                final List<Class<?>[]> typeslist = generateCompatibleSignatures(lookupMode, types);
                for (final Class<?>[] aTypeslist : typeslist) {
                    try {
                        constructor = datatype.getConstructor(aTypeslist);
                        break;
                    } catch (final NoSuchMethodException x) {
                        // do nothing
                    }
                }
            }
        }

        // if a constructor was found (and it wasn't in the cache, because method would've returned already)
        if (constructor != null) {
            addMethodToCache(datatype, datatype.getName(), constructor, types);
            return constructor;
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
    @Nullable
    @SuppressWarnings("WeakerAccess")
    public static Method findSimpleCompatibleMethod(final Class<?> datatype, final String methodName, final Class<?>... signature) {
        try {
            return findCompatibleMethod(datatype, methodName, EnumSet.noneOf(LookupMode.class), signature);
        } catch (final NoSuchMethodException e) {
            return null;
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
    public static Method findCompatibleMethod(final Class<?> datatype, final String methodName, final EnumSet<LookupMode> lookupMode,
            final Class<?>... signature) throws NoSuchMethodException {
        // first try to find the method in the method cache
        Method method = (Method) getMethodFromCache(datatype, methodName, signature);
        if (method != null) {
            return method;
        } else {
            try {
                // try standard call
                method = getMethod(datatype, methodName, signature);
            } catch (final NoSuchMethodException e) {
                // failed, try all possible wraps/unwraps
                final List<Class<?>[]> signatures = generateCompatibleSignatures(lookupMode, signature);
                for (final Class<?>[] compatibleSignature : signatures) {
                    try {
                        method = getMethod(datatype, methodName, compatibleSignature);
                        break;
                    } catch (final NoSuchMethodException x) {
                        // do nothing
                    }
                }
            }
        }

        // if a method was found (and it wasn't in the cache, because method would've returned already)
        if (method != null) {
            addMethodToCache(datatype, methodName, method, signature);
            return method;
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
		return isSignatureCompatible(signature, targetSignature, lookupMode);
	}
	
    @SuppressWarnings({"unused", "WeakerAccess"})
	public static boolean isSignatureCompatible(Class<?>[] signature, Class<?>[] exactTargetSignature, EnumSet<LookupMode> lookupMode) {
		List<Class<?>[]> derivableSignatures = generateCompatibleSignatures(lookupMode, signature);
		
		for (Class<?>[] derivableSignature : derivableSignatures) {
			boolean currentSignatureCompatible = true;
			for (int i = 0; i < derivableSignature.length && currentSignatureCompatible; i++) {
				if (!derivableSignature[i].equals(exactTargetSignature[i])) {
					currentSignatureCompatible = false;
				}
			}
			if (currentSignatureCompatible) {
				return true;
			}
		}
		return false;
	}
	
	/**
     * Initializes the list with type-arrays and starts generating beginning from index 0. This method is used for (un)wrapping.
     * 
     * @param lookupMode Flag indicating the search steps that need to be done.
     * @param signature The list with original user specified types.
     * @return The list with converted type-arrays.
     */
    @NotNull
    public static List<Class<?>[]> generateCompatibleSignatures(final EnumSet<LookupMode> lookupMode, final Class<?>... signature) {
        final List<Class<?>[]> signatures = new ArrayList<>();
        generateCompatibleSignatures(0, lookupMode, signatures, signature);
        return signatures;
    }

    /**
     * Recursively generates a complete list of all possible (un)wraps (autoboxing), supertypes, implemented interfaces, type conversions and any
     * combination thereof with the specified signature's elements (the individual parameter types).<br />
     * <br />
     * The combination signatures are generated in the following order:
     * <ol>
     * <li>no conversion; highest priority as it comes closest to user's requirement/specification</li>
     * <li>autoboxing; the autoboxed counterversion comes closest to the original datatype</li>
     * <li>interface; where methods can't be found using original type, interface placeholders are attempted</li>
     * <li>supertype; where methods can't be found using implemented interfaces, supertype placeholders are attempted</li>
     * <li>conversions; if all else fails, try to convert the datatype for common types (ie. int to String)</li>
     * </ol>
     * 
     * @param index The current index to start mutating from.
     * @param lookupMode Flag indicating the search steps that need to be done.
     * @param signatures The central storage list for new type-arrays.
     * @param signature The list with current types, to mutate further upon.
     */
    private static void generateCompatibleSignatures(final int index, final EnumSet<LookupMode> lookupMode, final List<Class<?>[]> signatures,
													 final Class<?>... signature) {
        // if new type array is completed
        if (index == signature.length) {
            signatures.add(signature);
        } else {
            // generate new array of types
            final Class<?> original = signature[index];

            // 1. don't generate compatible list; just try the normal type first
            // remember, in combinations types should be allowed to be converted)
            generateCompatibleSignatures(index + 1, lookupMode, signatures, signature.clone());

            // 2. generate type in which the original can be (un)wrapped
            if (lookupMode.contains(LookupMode.AUTOBOX)) {
                final Class<?> autoboxed = autobox(original);
                if (autoboxed != null) {
                    final Class<?>[] newSignature = replaceInArray(signature.clone(), index, autoboxed);
                    generateCompatibleSignatures(index + 1, lookupMode, signatures, newSignature);
                }
            }

            // autocast to supertype or interface?
            if (lookupMode.contains(LookupMode.CAST_TO_INTERFACE)) {
                // 3. generate implemented interfaces the original value could be converted (cast) into
                for (final Class<?> iface : original.getInterfaces()) {
                    final Class<?>[] newSignature = replaceInArray(signature.clone(), index, iface);
                    generateCompatibleSignatures(index + 1, lookupMode, signatures, newSignature);
                }
            }

            if (lookupMode.contains(LookupMode.CAST_TO_SUPER)) {
                // 4. generate supertypes the original value could be converted (cast) into
                Class<?> supertype = original;
                while ((supertype = supertype.getSuperclass()) != null) {
                    final Class<?>[] newSignature = replaceInArray(signature.clone(), index, supertype);
                    generateCompatibleSignatures(index + 1, lookupMode, signatures, newSignature);
                }
            }

            // 5. generate types the original value could be converted into
            if (lookupMode.contains(LookupMode.COMMON_CONVERT)) {
                for (final Class<?> convert : ValueConversionHelper.collectCompatibleTargetTypes(original)) {
                    final Class<?>[] newSignature = replaceInArray(signature.clone(), index, convert);
                    generateCompatibleSignatures(index + 1, lookupMode, signatures, newSignature);
                }
            }
        }
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
     * Returns a field from the given object that goes by the name of <code>fieldName</code>. If <code>o</code> is a Class object, a static field will
     * be returned.
     * 
     * @param o The reference to the object to fetch the property value from.
     * @param fieldName The identifier or name of the member field/property.
     * @return The value of the <code>Field</code>.
     */
    @Nullable
    @SuppressWarnings("WeakerAccess")
    public static Field solveField(final Object o, final String fieldName) {
        try {
            if (o.getClass().equals(Class.class)) {
                // Java static field
                return ((Class<?>) o).getField(fieldName);
            } else {
                // Java instance field
                return o.getClass().getField(fieldName);
            }
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Retrieves a {@link Method} from a cache.
     * 
     * @param datatype The owning {@link Class} of the <code>Method</code> being searched for.
     * @param method The name of the method that is being searched for.
     * @param signature The parameter list of the method we need to match if a method was found by name.
     * @return The <code>Method</code> found on the specified owner with matching name and signature.
     * @see JReflect#methodCache
     * @see JReflect#addMethodToCache(Class, String, AccessibleObject, Class...)
     */
    @Nullable
    private static <T> AccessibleObject getMethodFromCache(final Class<T> datatype, final String method, final Class<?>... signature) {
        final Map<String, Map<AccessibleObject, Class<?>[]>> owner = methodCache.get(datatype);
        // we know only methods with parameter list are stored in the cache
        if (signature.length > 0) {
            // get owner, its methods matching specified name and match their signatures
            if (owner != null) {
                final Map<AccessibleObject, Class<?>[]> signatures = owner.get(method);
                if (signatures != null) {
                    for (final Map.Entry<AccessibleObject, Class<?>[]> entry : signatures.entrySet()) {
                        if (Arrays.equals(entry.getValue(), signature)) {
                            return entry.getKey();
                        }
                    }
                }
            }
        }
        // method not found or known not to be stored due to absent parameter list
        return null;
    }

    /**
     * Adds a specific <code>Method</code> to the cache.
     * 
     * @param datatype The <code>Class</code> that owns the <code>Method</code>.
     * @param method The <code>Method</code>'s name by which methods can be found on the specified owner.
     * @param methodRef The <code>Method</code> reference that's actually being stored in the cache.
     * @param signature The parameter list of the <code>Method</code> being stored.
     * @see JReflect#methodCache
     * @see JReflect#getMethodFromCache(Class, String, Class...)
     */
    private static void addMethodToCache(final Class<?> datatype, final String method, final AccessibleObject methodRef,
                                         final Class<?>... signature) {
        // only store methods with a parameter list
        if (signature.length > 0) {
            // get or create owner entry
            Map<String, Map<AccessibleObject, Class<?>[]>> owner = methodCache.get(datatype);
            owner = owner != null ? owner : new LinkedHashMap<String, Map<AccessibleObject, Class<?>[]>>();
            // get or create list of methods with specified method name
            Map<AccessibleObject, Class<?>[]> methods = owner.get(method);
            methods = methods != null ? methods : new LinkedHashMap<AccessibleObject, Class<?>[]>();
            // add or overwrite method entry
            methods.put(methodRef, signature);
            // finally shelve all the stuff back
            methods.put(methodRef, signature);
            owner.put(method, methods);
            methodCache.put(datatype, owner);
        }
    }

    /**
     * Assigns a value to a field <code>id</code> on the given object <code>o</code>. If a simple assignment fails, a common conversion will be
     * attempted.
     * 
     * @param o The object to find the field on.
     * @param property The name of the field we're assigning the value to.
     * @param value The value to assign to the field, may be converted to the field's type through common conversion.
     * @return The actual value that was assigned (the original or the converted value).
     * @throws IllegalAccessException Thrown by {@link Field#set(Object, Object)}
     * @throws NoSuchFieldException Thrown if the {@link Field} could not be found, even after trying to convert the value to the target type.
     * @see ValueConversionHelper#convert(Object, Class)
     */
    @Nullable
    @SuppressWarnings("WeakerAccess")
    public static Object assignToField(final Object o, final String property, final Object value) throws IllegalAccessException, NoSuchFieldException {
        final Field field = solveField(o, property);
        if (field != null) {
        	Object assignedValue = value;
            try {
                field.set(o, value);
            } catch (final IllegalArgumentException ie) {
                assignedValue = ValueConversionHelper.convert(value, field.getType());
                field.set(o, assignedValue);
            }
            return assignedValue;
        } else {
            throw new NoSuchFieldException();
        }
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
     * Returns the smallest class that can hold all of the specified numbers.
     * 
     * @param numbers The list with numbers that all should fit in the <code>Number</code> container.
     * @return The <code>Number</code> container that is just large enough for all specified numbers.
     */
    @NotNull
    @SuppressWarnings("WeakerAccess")
    public static Class<?> widestNumberClass(final Number... numbers) {
        // find widest number
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
     * Returns a list of names that represent the fields on an <code>Object</code>.
     * 
     * @param subject The <code>Object</code> who's properties/fields need to be reflected.
     * @return A list of names that represent the fields on the given <code>Object</code>.
     */
    @NotNull
    @SuppressWarnings("WeakerAccess")
    public static Collection<String> collectProperties(final Object subject) {
        final Collection<String> properties = new LinkedHashSet<>();
        // collect properties/fields
        final Field[] fields = subject.getClass().getFields();
        for (final Field f : fields) {
            properties.add(f.getName());
        }
        return properties;
    }

    /**
     * Returns a list of names that represent the methods on an <code>Object</code>
     * 
     * @param subject The <code>Object</code> who's methods need to be reflected.
     * @param publicOnly Indicates whether only public (albeit inherited) members should be returned. Else also private and protected methods will be
     *            included
     * @return Returns a list with methods, either {@link Method}s.
     */
    @NotNull
    @SuppressWarnings("WeakerAccess")
    public static Set<String> collectMethods(final Object subject, final boolean publicOnly) {
        final Set<String> methodNames = new LinkedHashSet<>();
        // collect methods
        final Set<Method> allMethods = new HashSet<>(Arrays.asList(subject.getClass().getMethods()));
        if (!publicOnly) {
            Class<?> _class = subject.getClass();
            while (_class != null) {
                allMethods.addAll(Arrays.asList(_class.getDeclaredMethods()));
                _class = _class.getSuperclass();
            }
        }
        for (final Method m : allMethods) {
            methodNames.add(m.getName());
        }
        return methodNames;
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
    @SuppressWarnings("WeakerAccess")
    public static <T> T[] replaceInArray(final T[] array, final int index, final T value) {
        array[index] = value;
        return array;
    }
}