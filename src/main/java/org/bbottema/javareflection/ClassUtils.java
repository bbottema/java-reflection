package org.bbottema.javareflection;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.util.ExternalClassLoader;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility with convenience methods that operate on the class level.
 * <ul>
 * <li>With this helper class you can locate and/or load classes. An advanced <code>Class</code> lookup ({@link #locateClass(String, boolean,
 * ExternalClassLoader)}), that allows a full scan (to try all
 * packages known) and an optional {@link ExternalClassLoader} instance that is able to actually compile a .java file on the fly and load its compile
 * .class file</li>
 * <li>create a new instance while handling all the exceptions</li>
 * <li>find fields or assign values to fields, using casting, autoboxing or type conversions</li>
 * <li>simply give back a list of field / method names</li>
 * </ul>
 */
@UtilityClass
public final class ClassUtils {
	
	/**
	 * {@link Class} cache optionally used when looking up classes with {@link #locateClass(String, boolean, ExternalClassLoader)}.
	 */
	private final static Map<String, Class<?>> classCache = new HashMap<>();
	
	
	@SuppressWarnings("WeakerAccess")
	public static void resetCache() {
		classCache.clear();
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
	 * Returns a list of names that represent the fields on an <code>Object</code>.
	 *
	 * @param subject The <code>Object</code> who's properties/fields need to be reflected.
	 * @return A list of names that represent the fields on the given <code>Object</code>.
	 */
	@NotNull
	@SuppressWarnings("WeakerAccess")
	public static Collection<String> collectPropertyNames(final Object subject) {
		final Collection<String> properties = new LinkedHashSet<>();
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
}
