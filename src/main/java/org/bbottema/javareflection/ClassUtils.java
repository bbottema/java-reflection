package org.bbottema.javareflection;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.model.MethodModifier;
import org.bbottema.javareflection.util.ExternalClassLoader;
import org.bbottema.javareflection.valueconverter.IncompatibleTypeException;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.bbottema.javareflection.LookupCaches.CLASS_CACHE;
import static org.bbottema.javareflection.util.MiscUtil.trustedNullableCast;

/**
 * Utility with convenience methods that operate on the class level.
 * <ul>
 * <li>With this helper class you can locate and/or load classes. An advanced <code>Class</code> lookup ({@link #locateClass(String, boolean,
 * ClassLoader)}), that allows a full scan (to try all
 * packages known) and an optional {@link ExternalClassLoader} instance that is able to actually compile a .java file on the fly and load its compile
 * .class file</li>
 * <li>create a new instance while handling all the exceptions</li>
 * <li>find fields or assign values to fields, using casting, autoboxing or type conversions</li>
 * <li>simply give back a list of field / method names</li>
 * </ul>
 */
@UtilityClass
@SuppressWarnings("WeakerAccess")
public final class ClassUtils {
	
	/**
	 * Searches the JVM and optionally all of its packages
	 *
	 * @param className The name of the class to locate.
	 * @param fullscan Whether a full scan through all available java packages is required.
	 * @param classLoader Optional user-provided classloader.
	 * @return The <code>Class</code> reference if found or <code>null</code> otherwise.
	 */
	@Nullable
	@SuppressWarnings({"WeakerAccess", "unchecked"})
	public static <T> Class<T> locateClass(final String className, final boolean fullscan, @Nullable final ClassLoader classLoader) {
		final String cacheKey = className + fullscan;
		if (CLASS_CACHE.containsKey(cacheKey)) {
			return (Class<T>) CLASS_CACHE.get(cacheKey);
		}
		Class<?> _class;
		if (fullscan) {
			_class = locateClass(className, null, classLoader);
		} else {
			// try standard package used for most common classes
			_class = locateClass(className, "java.lang", classLoader);
			if (_class == null) {
				_class = locateClass(className, "java.util", classLoader);
			}
			if (_class == null) {
				_class = locateClass(className, "java.math", classLoader);
			}
		}
		CLASS_CACHE.put(cacheKey, _class);
		return (Class<T>) _class;
	}
	
	@Nullable
	@SuppressWarnings({"WeakerAccess", "unchecked"})
	public static <T> Class<T> locateClass(final String className, @Nullable final String inPackage, @Nullable final ClassLoader classLoader) {
		final String cacheKey = className + inPackage;
		if (CLASS_CACHE.containsKey(cacheKey)) {
			return (Class<T>) CLASS_CACHE.get(cacheKey);
		}
		
		Class<?> _class = locateClass(className, classLoader);
		
		if (_class == null) {
			_class = PackageUtils.scanPackagesForClass(className, inPackage, classLoader);
		}
		
		CLASS_CACHE.put(cacheKey, _class);
		return (Class<T>) _class;
	}
	
	/**
	 * This function dynamically tries to locate a class. First it searches the class-cache list, then it tries to get it from the Virtual Machine
	 * using {@code Class.forName(String)}.
	 *
	 * @param fullClassName The <code>Class</code> that needs to be found.
	 * @param classLoader Optional user-provided classloader.
	 * @return The {@code Class} object found from cache or VM.
	 */
	@SuppressWarnings({"WeakerAccess", "unchecked"})
	@Nullable
	public static <T> Class<T> locateClass(final String fullClassName, @Nullable final ClassLoader classLoader) {
		try {
			Class<?> _class = null;
			if (classLoader != null) {
				_class = classLoader.loadClass(fullClassName);
			}
			if (_class == null) {
				_class = Class.forName(fullClassName);
			}
			return (Class<T>) _class;
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
			return ConstructorFactory.obtainConstructor(_class).newInstance();
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

	// Workaround: mockito does not support mocking Class.class, so getConstructor() cannot be mocked and we mock this factory method instead.
	static class ConstructorFactory {
		static <T> Constructor<T> obtainConstructor(Class<T> _class) throws NoSuchMethodException {
			return _class.getConstructor();
		}
	}

	/**
	 * Gets value from the field returned by {@link #solveField(Object, String)}.;
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	public static <T> T solveFieldValue(final Object object, final String fieldName) {
		final Field field = solveField(object, fieldName);
		if (field == null) {
			throw new RuntimeException(new NoSuchFieldException());
		}
		field.setAccessible(true);
		try {
			return trustedNullableCast(field.get(object));
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Was unable to retrieve value from field %s", e);
		}
	}

	/**
	 * Delegates to {@link #solveField(Class, String)} by using the class of given object <code>object</code> or if <code>object</code> itself if it ss a class.
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	public static Field solveField(final Object object, final String fieldName) {
		return object.getClass().equals(Class.class)
				? solveField((Class<?>) object, fieldName) // Java static field
				: solveField(object.getClass(), fieldName); // Java instance field
	}

	/**
	 * Returns a field from the given Class that goes by the name of <code>fieldName</code>. Will search for fields on implemented interfaces and superclasses.
	 *
	 * @param _class The reference to the Class to fetch the field from.
	 * @param fieldName The identifier or name of the member field/property.
	 * @return The value of the <code>Field</code>.
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	public static Field solveField(final Class<?> _class, final String fieldName) {
		Field resolvedField = null;
		try {
			resolvedField = _class.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			for (int i = 0; resolvedField == null && i < _class.getInterfaces().length; i++) {
				resolvedField = solveField(_class.getInterfaces()[i], fieldName);
			}
			for (Class<?> superclass = _class.getSuperclass(); resolvedField == null && superclass != null; superclass = superclass.getSuperclass()) {
				resolvedField = solveField(superclass, fieldName);
			}
		}
		return resolvedField;
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
			field.setAccessible(true);
			Object assignedValue = value;
			try {
				field.set(o, value);
			} catch (final IllegalArgumentException ie) {
				try {
					assignedValue = ValueConversionHelper.convert(value, field.getType());
				} catch (IncompatibleTypeException e) {
					throw new NoSuchFieldException(e.getMessage());
				}
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
	 * @return Returns the result of {@link #collectMethods(Class, Class, EnumSet)} mapped to the method names.
	 */
	@SuppressWarnings("WeakerAccess")
	public static Set<String> collectMethodNames(Class<?> dataType, Class<?> boundaryMarker, EnumSet<MethodModifier> methodModifiers) {
		Set<String> methodNames = new HashSet<>();
		for (Method m : collectMethods(dataType, boundaryMarker, methodModifiers)) {
			methodNames.add(m.getName());
		}
		return methodNames;
	}
	
	/**
	 * @return The result of {@link #collectMethodsMappingToName(Class, Class, EnumSet)} filtered on method name.
	 */
	@SuppressWarnings("WeakerAccess")
	public static List<Method> collectMethodsByName(final Class<?> type, Class<?> boundaryMarker, EnumSet<MethodModifier> methodModifiers, final String methodName) {
		LinkedHashMap<String, List<Method>> methodsByName = collectMethodsMappingToName(type, boundaryMarker, methodModifiers);
		return methodsByName.containsKey(methodName) ? methodsByName.get(methodName) : new ArrayList<Method>();
	}

	/**
	 * @return Whether {@link #collectMethodsMappingToName(Class, Class, EnumSet)} contains a method with the given name.
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean hasMethodByName(final Class<?> type, Class<?> boundaryMarker, EnumSet<MethodModifier> methodModifiers, final String methodName) {
		LinkedHashMap<String, List<Method>> methodsByName = collectMethodsMappingToName(type, boundaryMarker, methodModifiers);
		return methodsByName.containsKey(methodName) && !methodsByName.get(methodName).isEmpty();
	}

	/**
	 * @return The first result of {@link #collectMethodsByName(Class, Class, EnumSet, String)}.
	 * 			<strong>Note: </strong> methods are ordered in groups (see {@link #collectMethods(Class, Class, EnumSet)})).
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	public static Method findFirstMethodByName(final Class<?> type, Class<?> boundaryMarker, EnumSet<MethodModifier> methodModifiers, final String methodName) {
		List<Method> methods = collectMethodsByName(type, boundaryMarker, methodModifiers, methodName);
		return methods.isEmpty() ? null : methods.iterator().next();
	}
	
	/**
	 * @return The result of {@link #collectMethods(Class, Class, EnumSet)} filtered on method name,
	 * 				ordered in groups (see {@link #collectMethods(Class, Class, EnumSet)})).
	 */
	@SuppressWarnings("WeakerAccess")
	public static LinkedHashMap<String, List<Method>> collectMethodsMappingToName(Class<?> type, Class<?> boundaryMarker, EnumSet<MethodModifier> methodModifiers) {
		LinkedHashMap<String, List<Method>> methodsMappedToName = new LinkedHashMap<>();
		for (Method method : collectMethods(type, boundaryMarker, methodModifiers)) {
			if (!methodsMappedToName.containsKey(method.getName())) {
				methodsMappedToName.put(method.getName(), new ArrayList<Method>());
			}
			methodsMappedToName.get(method.getName()).add(method);
		}
		return methodsMappedToName;
	}
	
	/**
	 * Returns a list of names that represent the methods on an <code>Object</code>.
	 * <p>
	 * Methods are ordered by their declaring type in the inheritance chain, but unordered for methods of the same type.
	 * In other words, considering type A, B and C each with methods 1, 2 and 3, the methods might be ordered as follows:
	 * {@code [A2,A1,B1,B2,C2,C1]}.
	 *
	 * @param methodModifiers List of method modifiers that will match any method that has one of them.
	 * @param boundaryMarker Optional type to limit (including) how far back up the inheritance chain we go for discovering methods.
	 * @return Returns a list with methods, either {@link Method}s.
	 */
	@SuppressWarnings("WeakerAccess")
	public static List<Method> collectMethods(Class<?> dataType, Class<?> boundaryMarker, EnumSet<MethodModifier> methodModifiers) {
		final List<Method> allMethods = new ArrayList<>();
		
		for (Method declaredMethod : dataType.getDeclaredMethods()) {
			if (MethodModifier.meetsModifierRequirements(declaredMethod, methodModifiers)) {
				allMethods.add(declaredMethod);
			}
		}

		for (Class<?> implementedInterface : dataType.getInterfaces()) {
			allMethods.addAll(collectMethods(implementedInterface, boundaryMarker, methodModifiers));
		}
		
		if (dataType != boundaryMarker && dataType.getSuperclass() != null) {
			allMethods.addAll(collectMethods(dataType.getSuperclass(), boundaryMarker, methodModifiers));
		}
		return allMethods;
	}
}