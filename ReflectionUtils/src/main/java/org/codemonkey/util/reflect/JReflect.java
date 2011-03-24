package org.codemonkey.util.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.codemonkey.util.reflect.ValueConverter.IncompatibleTypeException;

/**
 * This class utilizes the functionality of the Java class <code>java.lang.reflect</code>. In addition to specific requirements for the
 * Nibble scriptengine, all sorts of little toolfunctions have been added to the reflect package.<br />
 * <br />
 * For example, an expanded version of <code>getConstructor</code> is implemented that tries to find a constructor of a given datatype, with
 * a given argument datatypelist, where types do not have to match formal types (auto-boxing, supertypes, implemented interfaces and type
 * conversions). This expanded version tries a simple call first and when it fails, it generates a list of datatype arrays with all possible
 * version of any type in the original list possible, and combinations thereof.<br />
 * <br />
 * Types that are candidates for Autoboxing:
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
 * <br />
 * Also contains a classloader that can compile/load/cache java sourcefiles.
 * 
 * @author Benny Bottema
 */
/**
 * @author Benny Bottema
 *
 */
/**
 * @author Benny Bottema
 */
public final class JReflect {

	/**
	 * bitflag; indicates that looking for methods includes trying to find compatible signatures by autoboxing the specified arguments.
	 */
	public static final int LM_AUTOBOX = 1;

	/**
	 * bitflag; indicates that looking for methods includes trying to find compatible signatures by casting the specified arguments to a
	 * super type.
	 */
	public static final int LM_CASTSUPER = 2;

	/**
	 * bitflag; indicates that looking for methods includes trying to find compatible signatures by casting the specified arguments to an
	 * implemented interface.
	 */
	public static final int LM_CASTINTERFACE = 4;

	/**
	 * bitflag; indicates that looking for methods includes trying to find compatible signatures by automatically convert the specified
	 * arguments.
	 */
	public static final int LM_AUTOCONVERT = 8;

	/**
	 * {@link Method} cache categorized by owning <code>Classes</code> (since several owners can have a method with the same name and
	 * signature). Methods are stored based on <code>Method</code> reference along with their unique signature (per owner), so multiple
	 * methods on one owner with the same name can coexist.<br />
	 * <br />
	 * This cache is being maintained to reduce lookup times when trying to find signature compatible Java methods. The possible signature
	 * combinations using autoboxing and/or automatic common conversions can become very large (7000+ with only three parameters) and can
	 * become a real problem. The more frequently a method is being called the larger the performance gain, especially for methods with long
	 * parameter lists
	 * 
	 * @see JReflect#addJavaMethodToCache(Class, String, AccessibleObject, Class[])
	 * @see JReflect#getJavaMethodFromCache(Class, String, Class[])
	 */
	private final static Map<Class<?>, Map<String, Map<AccessibleObject, Class<?>[]>>> methodCache = new LinkedHashMap<Class<?>, Map<String, Map<AccessibleObject, Class<?>[]>>>();

	/**
	 * A list with Number types in ascending order to widenes (or size) of each type (ie. double is wider than integer).
	 */
	private static final Map<Class<?>, Integer> numSizes;

	static {
		numSizes = new LinkedHashMap<Class<?>, Integer>();
		numSizes.put(byte.class, 1);
		numSizes.put(short.class, 2);
		numSizes.put(int.class, 3);
		numSizes.put(long.class, 4);
		numSizes.put(float.class, 5);
		numSizes.put(double.class, 6);
		numSizes.put(Byte.class, 1);
		numSizes.put(Short.class, 2);
		numSizes.put(Integer.class, 3);
		numSizes.put(Long.class, 4);
		numSizes.put(Float.class, 5);
		numSizes.put(Double.class, 6);
	}

	/**
	 * Private constrcutor to prevent creating instances of this class.
	 */
	private JReflect() {
	}

	/**
	 * Searches the JVM and optionally all of its packages
	 * 
	 * @param className The name of the class to locate.
	 * @param fullscan Whether a full scan through all available java packages is required.
	 * @param classLoader Optional user-provided classloader.
	 * @return The <code>Class</code> reference if found or <code>null</code> otherwise.
	 */
	public static Class<?> locateClass(final String className, final boolean fullscan, final ExternalClassLoader classLoader) {
		if (fullscan) {
			Class<?> _class = null;
			// cycle through all packages and try allocating dynamically
			final Package[] ps = Package.getPackages();
			for (int i = 0; i < ps.length && _class == null; i++) {
				_class = locateClass(ps[i].getName() + "." + className, classLoader);
			}
			return _class;
		} else {
			// try standard package used for most common classes
			return locateClass("java.lang." + className, classLoader);
		}
	}

	/**
	 * This function dynamically tries to locate a class. First it searches the class cachelist, then it tries to get it from the Virtual
	 * Machine using {@code Class.forName(String)}.
	 * 
	 * @param fullClassName The <code>Class</code> that needs to be found.
	 * @param classLoader Optional user-provided classloader.
	 * @return The {@code Class} object found from cache or VM.
	 */
	public static Class<?> locateClass(final String fullClassName, final ExternalClassLoader classLoader) {
		try {
			// try user class-cache or the standard approach otherwise\
			final Class<?> c = classLoader != null ? classLoader.loadClass(fullClassName) : null;
			return c != null ? c : Class.forName(fullClassName, false, null);
		} catch (final ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Simply calls {@link Class#newInstance()} and hides the exception handling boilerplate code.
	 */
	public static <T> T newInstanceSimple(final Class<T> _class) {
		try {
			return (T) _class.getConstructor().newInstance();
		} catch (IllegalArgumentException e) {
			assert false : "we don't pass in arguments";
			throw new RuntimeException("unable to invoke parameterless constructor", e);
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
	 * Locates a method on an Object using serveral searchmodes for optimization. First of all a {@link Method} cache is being maintained to
	 * quickly fetch heavily used methods. If not cached before and if a simple search (autoboxing and supertype casts) fails a more complex
	 * search is done where all interfaces are searched for the method as well. If this fails as well, this method will try to autoconvert
	 * the types of the arguments and find a matching signature that way.
	 * 
	 * @param context The object to call the method from (can be null).
	 * @param datatype The class to find the method on.
	 * @param identifier The name of the method to locate.
	 * @param args A list of [non-formal] arguments.
	 * @return The return value of the invoke method, if succesful.
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object invokeCompatibleMethod(final Object context, final Class<?> datatype, final String identifier,
			final Object... args)
			throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// determine the signature we want to find a compatible java method for
		final Class<?>[] signature = JReflect.collectTypes(args);

		// setup lookup procedure starting with simple search mode
		int lookupMode = LM_AUTOBOX | LM_CASTSUPER;
		Method method;

		// try to fina a compatible Java method using various lookup modes
		try {
			method = findCompatibleJavaMethod(datatype, identifier, signature, lookupMode);
		} catch (final NoSuchMethodException e1) {
			try {
				// moderate search mode
				lookupMode |= LM_CASTINTERFACE;
				method = findCompatibleJavaMethod(datatype, identifier, signature, lookupMode);
			} catch (final NoSuchMethodException e2) {
				// full searchmode
				lookupMode |= LM_AUTOCONVERT;
				method = findCompatibleJavaMethod(datatype, identifier, signature, lookupMode);
			}
		}

		// try to invoke the method with unconverted arguments or convert them if needed
		try {
			return method.invoke(context, args);
		} catch (final IllegalArgumentException e) {
			ValueConverter.convert(args, method.getParameterTypes());
			return method.invoke(context, args);
		}
	}

	/**
	 * Locates and invokes a {@link Constructor}.
	 * 
	 * @param datatype The class to find the constructor for.
	 * @param args A list of [non-formal] arguments.
	 * @return The instantiated object of class datatype.
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @see java.lang.reflect.Constructor#newInstance(Object[])
	 */
	public static <T> T invokeCompatibleConstructor(final Class<T> datatype, final Object... args)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		final Class<?>[] signature = JReflect.collectTypes(args);
		return invokeConstructor(datatype, signature, args);
	}

	/**
	 * Locates and invokes a {@link Constructor}, using a customized typelist. Avoids dynamically trying to find correct parameter type
	 * list. Can also be used to force up/down casting (ie. passing a specific type of <code>List</code> into a generic type)
	 * 
	 * @param datatype The class to find the constructor for.
	 * @param signature The typelist used to find correct constructor.
	 * @param args A list of [non-formal] arguments.
	 * @return The instantiated object of class datatype.
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @see java.lang.reflect.Constructor#newInstance(Object[])
	 */
	public static <T> T invokeConstructor(final Class<T> datatype, final Class<?>[] signature, final Object[] args)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		// setup lookup procedure
		int lookupMode = LM_AUTOBOX | LM_CASTSUPER;
		Constructor<T> constructor;

		// try to find a compatible Java constructor
		try {
			// simple search mode
			constructor = findCompatibleJavaConstructor(datatype, signature, lookupMode);
		} catch (final NoSuchMethodException e1) {
			try {
				// moderate search mode
				lookupMode |= LM_CASTINTERFACE;
				constructor = findCompatibleJavaConstructor(datatype, signature, lookupMode);
			} catch (final NoSuchMethodException e2) {
				// full searchmode
				lookupMode |= LM_AUTOCONVERT;
				constructor = findCompatibleJavaConstructor(datatype, signature, lookupMode);
			}
		}

		// try to invoke the constructor with unconverted arguments or convert them if needed
		try {
			return constructor.newInstance(args);
		} catch (final IllegalArgumentException e) {
			ValueConverter.convert(args, constructor.getParameterTypes());
			return constructor.newInstance(args);
		}
	}

	/**
	 * Creates a new array of class objects harvested from an array of objects.
	 * 
	 * @param objects The array of objects to harvest classtypes from.
	 * @return The array with the harvested classtypes.
	 */
	public static Class<?>[] collectTypes(final Object[] objects) {
		// collect classtypes of the arguments
		final Class<?>[] types = new Class[objects.length];
		for (int i = 0; i < objects.length; i++) {
			final Object o = objects[i];
			types[i] = o != null ? o.getClass() : Object.class;
		}
		return types;
	}

	/**
	 * Tries to find a {@link Constructor} of a given type, with a given typelist, where types do not match due to formal types <!=>
	 * simpletypes. This expanded version tries a simple call first and when it fails, it generates a list of type arrays with all possible
	 * (un)wraps of any type in the original list possible, and combinations thereof.
	 * 
	 * @param datatype The class to get the constructor from.
	 * @param types The list of types as specified by the user.
	 * @return The constructor if found, otherwise exception is thrown.
	 * @exception NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> findCompatibleJavaConstructor(final Class<T> datatype, final Class<?>[] types, final int lookupMode)
			throws NoSuchMethodException {
		// first try to find the constructor in the method cache
		Constructor<T> constructor = (Constructor<T>) getJavaMethodFromCache(datatype, datatype.getName(), types);
		if (constructor != null) {
			return constructor;
		} else {
			try {
				// try standard call
				constructor = datatype.getConstructor(types);
			} catch (final NoSuchMethodException e) {
				// failed, try all possible wraps/unwraps
				final List<Class<?>[]> typeslist = generateCompatibleSignatures(types, lookupMode);
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
			addJavaMethodToCache(datatype, datatype.getName(), constructor, types);
			return constructor;
		} else {
			throw new NoSuchMethodException();
		}
	}

	/**
	 * Delegates to {@link #findCompatibleJavaMethod(Class, String, Class[], int)}, using strict lookupmode (no autoboxing, casting etc.)
	 * and optional signature.<br />
	 * <br />
	 * Returns <code>null</code> in case of a <code>NoSuchMethodException</code> exception.
	 * 
	 * @see #findCompatibleJavaMethod(Class, String, Class[], int)
	 */
	public static Method findSimpleCompatibleJavaMethod(final Class<?> datatype, final String methodName, final Class<?>... signature) {
		try {
			return findCompatibleJavaMethod(datatype, methodName, signature, 0);
		} catch (final NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * Same as <code>getConstructor()</code>, except for getting a {@link Method} of a classtype, using the name to indicate which method
	 * should be located.
	 * 
	 * @param datatype The class to get the constructor from.
	 * @param name The name of the method to retrieve from the class.
	 * @param signature The list of types as specified by the user.
	 * @param lookupMode Combined bitflag indicating the search steps that need to be done.
	 * @return The method if found, otherwise exception is thrown.
	 * @exception NoSuchMethodException
	 */
	public static Method findCompatibleJavaMethod(final Class<?> datatype, final String name, final Class<?>[] signature,
			final int lookupMode)
			throws NoSuchMethodException {
		// first try to find the method in the method cache
		Method method = (Method) getJavaMethodFromCache(datatype, name, signature);
		if (method != null) {
			return method;
		} else {
			try {
				// try standard call
				method = getJavaMethod(datatype, name, signature);
			} catch (final NoSuchMethodException e) {
				// failed, try all possible wraps/unwraps
				final List<Class<?>[]> signatures = generateCompatibleSignatures(signature, lookupMode);
				for (final Class<?>[] compatibleSignature : signatures) {
					try {
						method = getJavaMethod(datatype, name, compatibleSignature);
						break;
					} catch (final NoSuchMethodException x) {
						// do nothing
					}
				}
			}
		}

		// if a method was found (and it wasn't in the cache, because method would've returned already)
		if (method != null) {
			addJavaMethodToCache(datatype, name, method, signature);
			return method;
		} else {
			throw new NoSuchMethodException();
		}
	}

	/**
	 * Searches a specific class object for a {@link Method} using java reflect using a specific signature. This method will first search
	 * all implemented interfaces for the method to avoid visiblity problems.<br />
	 * <br />
	 * An example of such a problem is the <code>Iterator</code> as implemented by the <code>ArrayList</code>. The Iterator is implemented
	 * as a private innerclass and as such not accessible by java reflect (even though the implemented methods are declared <i>public</i>),
	 * unlike the interface's definition.
	 * 
	 * @param datatype The class reference to locate the method on.
	 * @param name The name of the method to find.
	 * @param signature The signature the method should match.
	 * @return The Method found on the datatype that matched the specified signature.
	 * @throws NoSuchMethodException
	 * @see java.lang.Class#getMethod(String, Class[])
	 */
	public static Method getJavaMethod(final Class<?> datatype, final String name, final Class<?>[] signature)
			throws NoSuchMethodException {
		for (final Class<?> iface : datatype.getInterfaces()) {
			try {
				return iface.getMethod(name, signature);
			} catch (final NoSuchMethodException e) {
				// do nothing
			}
		}
		return datatype.getMethod(name, signature);
	}

	/**
	 * Initializes the list with type-arrays and starts generating beginning from index 0. This method is used for (un)wrapping.
	 * 
	 * @param signature The list with original user specified types.
	 * @param lookupMode Combined bitflag indicating the search steps that need to be done.
	 * @return The list with converted type-arrays.
	 */
	private static List<Class<?>[]> generateCompatibleSignatures(final Class<?>[] signature, final int lookupMode) {
		final List<Class<?>[]> signatures = new ArrayList<Class<?>[]>();
		generateCompatibleSignatures(signatures, signature, 0, lookupMode);
		return signatures;
	}

	/**
	 * Recursively generates a complete list of all possible (un)wraps (autoboxing), supertypes, implemented interfaces, type conversions
	 * and any combination thereof with the specified signature's elements (the individual parameter types).<br />
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
	 * @param signatures The central storage list for new type-arrays.
	 * @param signature The list with current types, to mutate further upon.
	 * @param index The current index to start mutating from.
	 * @param lookupMode Combined bitflag indicating the search steps that need to be done.
	 */
	private static void generateCompatibleSignatures(final List<Class<?>[]> signatures, final Class<?>[] signature, final int index,
			final int lookupMode) {
		// if new type array is completed
		if (index == signature.length) {
			signatures.add(signature);
		} else {
			// generate new array of types
			final Class<?> original = signature[index];

			// 1. don't generate compatible list; just try the normal type first
			// remember, in combinations types should be allowed to be converted)
			generateCompatibleSignatures(signatures, signature.clone(), index + 1, lookupMode);

			// 2. generate type in which the original can be (un)wrapped
			if ((lookupMode & JReflect.LM_AUTOBOX) != 0) {
				final Class<?> autoboxed = autobox(original);
				if (autoboxed != null) {
					final Class<?>[] newSignature = replaceInArray(signature.clone(), index, autoboxed);
					generateCompatibleSignatures(signatures, newSignature, index + 1, lookupMode);
				}
			}

			// autocast to supertype or interface?
			if ((lookupMode & JReflect.LM_CASTINTERFACE) != 0) {
				// 3. generate implemented interfaces the original value could be converted (cast) into
				for (final Class<?> iface : original.getInterfaces()) {
					final Class<?>[] newSignature = replaceInArray(signature.clone(), index, iface);
					generateCompatibleSignatures(signatures, newSignature, index + 1, lookupMode);
				}
			}

			if ((lookupMode & JReflect.LM_CASTSUPER) != 0) {
				// 4. generate supertypes the original value could be converted (cast) into
				Class<?> supertype = original;
				while ((supertype = supertype.getSuperclass()) != null) {
					final Class<?>[] newSignature = replaceInArray(signature.clone(), index, supertype);
					generateCompatibleSignatures(signatures, newSignature, index + 1, lookupMode);
				}
			}

			// 5. generate types the original value could be converted into
			if ((lookupMode & JReflect.LM_AUTOCONVERT) != 0) {
				for (final Class<?> convert : ValueConverter.collectCompatibleTypes(original)) {
					final Class<?>[] newSignature = replaceInArray(signature.clone(), index, convert);
					generateCompatibleSignatures(signatures, newSignature, index + 1, lookupMode);
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
	private static Class<?> autobox(final Class<?> c) {
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
	 * Overloaded version of <code>solveProperty(Object, String)</code>, where a property of type <code>Object</code> is attempted to be
	 * converted to a string for property name first.
	 * 
	 * @throws IllegalAccessException
	 * @throws IncompatibleTypeException
	 * @throws IllegalArgumentException
	 * @see JReflect#solveProperty(Object, String)
	 */
	public static Object solveProperty(final Object o, final Object property)
			throws IllegalArgumentException, IncompatibleTypeException, IllegalAccessException {
		return solveProperty(o, (String) ValueConverter.convert(property, String.class));
	}

	/**
	 * Solves a property field with specified identifier <code>id</code> on object <code>o</code>. First fetches the field object and then
	 * returns contentvalue, projected on object <code>o</code>. If no field found, a method with name <code>id</code> will be searched for.
	 * 
	 * @param o The reference to the object to fetch the propertyvalue from.
	 * @param id The identifier or name of the member field.
	 * @return The value contained in the field on the specified object.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object solveProperty(final Object o, final String id)
			throws IllegalArgumentException, IllegalAccessException {
		final Object value = solveField(o, id);
		return value != null ? ((Field) value).get(o) : solveMethod(o, id);
	}

	/**
	 * <ul>
	 * <li>In case subject is a Java Object, returns the value of {@link Field}.</li>
	 * <li>In case subject is a {@link AbstractDynamic}, returns a property.</li>
	 * </ul>
	 * 
	 * @param o The reference to the object to fetch the property value from.
	 * @param id The identifier or name of the member field/property.
	 * @return The value of the <code>Field</code> or in case of a Dynamic the property value.
	 */
	public static Object solveField(final Object o, final String id) {
		try {
			if (o.getClass().equals(Class.class)) {
				// Java static field
				return ((Class<?>) o).getField(id);
			} else {
				// Java instance field
				return o.getClass().getField(id);
			}
		} catch (final NoSuchFieldException e) {
			return null;
		}
	}

	/**
	 * Returns a method of the specified subject.
	 * <ul>
	 * <li>In case subject is a Java Object, returns a {@link ScopedJavaMethod}.</li>
	 * <li>In case subject is a Dynamic, returns a {@link DefaultDynamicMethod}, unless the method required is defined by
	 * {@link DynamicExposure}</li>
	 * </ul>
	 * 
	 * @param o The reference to the object to fetch the method value from.
	 * @param id The identifier or name of the member method.
	 * @return {@link ScopedJavaMethod} or {@link DefaultDynamicMethod}.
	 */
	public static Method solveMethod(final Object o, final String id) {
		// find Java Method
		final Method[] methods = o.getClass().getMethods();
		for (final Method m : methods) {
			if (m.getName().equals(id)) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Retrieves a {@link Method} from a cache.
	 * 
	 * @param datatype The owning {@link Class} of the <code>Method</code> being searched for.
	 * @param method The name of the method that is being searched for.
	 * @param signature The parameter list of the method we need to match if a method was found by name.
	 * @return The <code>Method</code> found on the specified owner with matching name and signature.
	 * @see JReflect#methodCache
	 * @see JReflect#addJavaMethodToCache(Class, String, AccessibleObject, Class[])
	 */
	private final static <T> AccessibleObject getJavaMethodFromCache(final Class<T> datatype, final String method,
			final Class<?>[] signature) {
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
	 * @see JReflect#getJavaMethodFromCache(Class, String, Class[])
	 */
	private final static void addJavaMethodToCache(final Class<?> datatype, final String method, final AccessibleObject methodRef,
			final Class<?>[] signature) {
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
	 * Overloaded version of <code>assignToField(Object, String, Object)</code>, where a property of type <code>Object</code> is attempted
	 * to be converted to a string for property name first.
	 * 
	 * @see JReflect#assignToField(Object, String, Object)
	 */
	public static void assignToField(final Object o, final Object property, final Object value)
			throws IllegalAccessException {
		final String propName = (String) ValueConverter.convert(property, String.class);
		assignToField(o, propName, value);
	}

	/**
	 * Assigns a value to a field <code>id</code> on the given object <code>o</code>.<br />
	 * If object <code>o</code> is a {@link AbstractDynamic}, the value can be either a dynamic property or a {@link DynamicMethod}.
	 * 
	 * @param o The object to locate the field on.
	 * @param property The name of the field to locate.
	 * @param value The value to assign to the field.
	 * @return The actual value that was assigned.
	 * @throws IllegalAccessException
	 */
	public static Object assignToField(final Object o, final String property, final Object value)
			throws IllegalAccessException {
		Object assignedValue = value;
		final Field field = (Field) solveField(o, property);
		try {
			field.set(o, value);
		} catch (final IllegalArgumentException ie) {
			assignedValue = ValueConverter.convert(value, field.getType());
			field.set(o, assignedValue);
		}
		return assignedValue;
	}

	/**
	 * Validates whether a string represents a valid package.
	 * 
	 * @param name The string representing a list of packages.
	 * @return A boolean indicating whether name represents a valid package.
	 */
	public static boolean isPackage(final String name) {
		return name.equals("java") || Package.getPackage(name) != null;
	}

	/**
	 * Returns the smallest class that can hold all of the specified numbers.
	 * 
	 * @param numbers The list with numbers that all should fit in the <code>Number</code> container.
	 * @return The <code>Number</code> container that is just large enough for all specified numbers.
	 */
	public static Class<?> widestNumberClass(final Number... numbers) {
		// find widest number
		Integer widest = 0;
		Class<?> c = Byte.class;
		for (final Number n : numbers) {
			final Integer size = numSizes.get(n.getClass());
			if (size > widest) {
				c = n.getClass();
				widest = size;
			}
		}
		return c;
	}

	/**
	 * Returns a list of names that represent the fields on an <code>Object</code>. For Dynamics, the stored properties are returned (not
	 * the Java fields of the class Dynamic).
	 * 
	 * @param subject The <code>Object</code> or <code>Dynamic</code> who's properties/fields need to be reflected.
	 * @return A list of names that represent the fields on an <code>Object</code>.
	 */
	public static Collection<String> collectProperties(final Object subject) {
		final Collection<String> properties = new LinkedHashSet<String>();
		// collect properties/fields
		final Field[] fields = subject.getClass().getFields();
		for (final Field f : fields) {
			properties.add(f.getName());
		}
		return properties;
	}

	/**
	 * Returns a list of names that represent the methods on an <code>Object</code>. For Dynamics, the stored methods are returned (not the
	 * Java methods of the class Dynamic).
	 * 
	 * @param subject The <code>Object</code> or <code>Dynamic</code> who's methods need to be reflected.
	 * @return Returns a list with methods, either {@link Method}s, or <code>Dynamic</code> methods.
	 */
	public static Collection<String> collectMethods(final Object subject) {
		final Collection<String> methods = new LinkedHashSet<String>();
		// collect methods
		final Method[] methodlist = subject.getClass().getMethods();
		for (final Method m : methodlist) {
			methods.add(m.getName());
		}
		return methods;
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
	public static <T> T[] replaceInArray(final T[] array, final int index, final T value) {
		array[index] = value;
		return array;
	}
}