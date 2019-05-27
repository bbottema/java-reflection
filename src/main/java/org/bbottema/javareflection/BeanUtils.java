package org.bbottema.javareflection;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.util.commonslang25.StringUtils;
import org.bbottema.javareflection.model.FieldWrapper;
import org.bbottema.javareflection.model.InvokableObject;
import org.bbottema.javareflection.valueconverter.IncompatibleTypeException;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.EnumSet.allOf;
import static java.util.EnumSet.of;
import static java.util.regex.Pattern.compile;
import static org.bbottema.javareflection.BeanUtils.BeanRestriction.YES_SETTER;
import static org.bbottema.javareflection.BeanUtils.BeanRestriction.YES_GETTER;

/**
 * A {@link Field} shorthand utility class used to collect fields from classes meeting Java Bean restrictions/requirements.
 * <p>
 * With this utility class you can perform field lookups, by combining lookup restriction criteria.
 * <p>
 * <strong>Example</strong><br />
 * "find all fields on a class <em>Apple</em>, not looking at its super classes, which should be protected, have a getter method, but not a setter
 * method"
 * 
 * <pre>
 * FieldUtils.collectFields(Apple.class, Apple.class, EnumSet.of(Visibility.PROTECTED),
 *         EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER));
 * </pre>
 *
 * @see #collectFields(Class, Class, EnumSet, EnumSet)
 */
@UtilityClass
public final class BeanUtils {
	
	/**
	 * Determines what visibility modifiers a field is allowed to have in {@link BeanUtils#collectFields(Class, Class, EnumSet, EnumSet)}.
	 */
	public enum Visibility {
		/**
		 * Visibility flag that corresponds with java's keyword <code>private</code>.
		 */
		PRIVATE(Modifier.PRIVATE),
		/**
		 * Visibility flag that corresponds with java's visibility modifier <code>default</code> (package protected).
		 */
		DEFAULT(-1), // no Java equivalent
		/**
		 * Visibility flag that corresponds with java's keyword <code>protected</code>.
		 */
		PROTECTED(Modifier.PROTECTED),
		/**
		 * Visibility flag that corresponds with java's keyword <code>public</code>.
		 */
		PUBLIC(Modifier.PUBLIC);
		
		private final int modifierFlag;

		Visibility(final int modifierFlag) {
			this.modifierFlag = modifierFlag;
		}
	}

	/**
	 * Indicates whether a field needs a Bean setter or getter, exactly none or any combination thereof. Determines what kind of fields are
	 * potential collection candidates.
	 */
	public enum BeanRestriction {
		/**
		 * Restriction flag that indicates a <em>getter</em> method is required.
		 */
		YES_GETTER,
		/**
		 * Restriction flag that indicates a <em>setter</em> method is required.
		 */
		YES_SETTER,
		/**
		 * Restriction flag that indicates no <em>setter</em> must be available.
		 */
		NO_SETTER,
		/**
		 * Restriction flag that indicates a <em>getter</em> must be available.
		 */
		NO_GETTER
	}
	
	/**
	 * Verifies is a given method occurs as setter or getter in the declaring class chain. Lookup works by finding actual properties with
	 * their respective getters/setters that follow bean convention.
	 * <p>
	 * Note that this is a strict lookup and interface methods are not considered bean methods. To include interfaces and their methods,
	 * use {@link #isBeanMethod(Method, Class, EnumSet, boolean)} with <em>checkBeanLikeForInterfaces</em> set to {@code true}.
	 * <p>
	 * Lookup can be configured to check only against specific visibility.
	 *
	 * @param method The method to match against getters/setters of a certain visibility
	 * @param boundaryMarker The last <code>class></code> or <code>interface</code> implementing class that methods are matched against. Can
	 *            be used to prevent matching methods on a super class.
	 * @param visibility A set of visibility requirements (ie. {@link Visibility#PROTECTED} indicates a *field* for which getter/setter are checked
	 *                      is allowed to have <code>protected</code> visibility). Note: the visibility modifiers for methods are ignored.
	 * @return Whether given method is a setter / getter within given restriction boundaries.
	 */
	@SuppressWarnings({"unused", "WeakerAccess"})
	public static boolean isBeanMethod(final Method method, final Class<?> boundaryMarker,
									   final EnumSet<Visibility> visibility) {
		return isBeanMethod(method, boundaryMarker, visibility, false);
	}
	
	/**
	 * @return Same as {@link #isBeanMethod(Method, Class, EnumSet)}, but may consider methods declared on interfaces as well.
	 */
	public static boolean isBeanMethod(Method method, Class<?> boundaryMarker,
										EnumSet<Visibility> visibility, boolean checkBeanLikeForInterfaces) {
		return method.getDeclaringClass().isInterface()
				? checkBeanLikeForInterfaces && methodIsBeanlike(method)
				: isBeanMethodForField(method, boundaryMarker, visibility);
	}
	
	private static boolean isBeanMethodForField(Method method, Class<?> boundaryMarker, EnumSet<Visibility> visibility) {
		Map<Class<?>, List<FieldWrapper>> fields = collectFields(method.getDeclaringClass(), boundaryMarker, visibility,
				EnumSet.noneOf(BeanRestriction.class));
		for (List<FieldWrapper> fieldWrappers : fields.values()) {
			for (FieldWrapper fieldWrapper : fieldWrappers) {
				if (method.equals(fieldWrapper.getGetter()) || method.equals(fieldWrapper.getSetter())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Determines if the method <em>could</em> be a bean method by looking just at its name, parameters and presence of return type.
	 *
	 * @return True, is the method starts with set/get/is, has exactly one parameter and in case of
	 * a primitive boolean the method should start with "isAbc"
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean methodIsBeanlike(Method method) {
		final Pattern SET_PATTERN = compile("set[A-Z].*?");
		final Pattern GET_PATTERN = compile("get[A-Z].*?");
		final Pattern IS_PATTERN = compile("is[A-Z].*?");
		
		final String name = method.getName();
		final int paramCount = method.getParameterTypes().length;
		final Class<?> rt = method.getReturnType();
		
		return
			(rt == boolean.class && IS_PATTERN.matcher(name).matches() && paramCount == 0) ||
			(rt != void.class && rt != boolean.class && GET_PATTERN.matcher(name).matches() && paramCount == 0) ||
			(rt == void.class && SET_PATTERN.matcher(name).matches() && paramCount == 1);
	}
	
	/**
	 * Returns a pool of {@link Field} wrappers including optional relevant setter/getter methods, collected from the given class tested
	 * against the given visibility and Bean restriction requirements.
	 * <p>
	 * The returned fields are mapped against the classes they were found on, since field names can be declared multiple times with the same
	 * name.
	 * 
	 * @param _class The class (and chain) to harvest fields from.
	 * @param boundaryMarker The last <code>class></code> or <code>interface</code> implementing class that fields are collected from. Can
	 *            be used to prevent finding fields on a super class.
	 * @param visibility A set of visibility requirements (ie. {@link Visibility#PROTECTED} indicates a field is allowed to have
	 *            <code>protected</code> visibility).
	 * @param beanRestrictions A set of Bean restriction requirements indicating a field should or shouldn't have a setter, getter or both.
	 * @return A Map per class in the chain with the fields declared by that class.
	 * @see #meetsVisibilityRequirements(Field, EnumSet)
	 * @see #resolveBeanProperty(Field, EnumSet)
	 */
	@SuppressWarnings("WeakerAccess")
	@NotNull
	public static LinkedHashMap<Class<?>, List<FieldWrapper>> collectFields(final Class<?> _class, final Class<?> boundaryMarker,
																  final EnumSet<Visibility> visibility, final EnumSet<BeanRestriction> beanRestrictions) {
		final LinkedHashMap<Class<?>, List<FieldWrapper>> fields = new LinkedHashMap<>();
		final Field[] allFields = _class.getDeclaredFields();
		final List<FieldWrapper> filteredFields = new LinkedList<>();
		for (final Field field : allFields) {
			if (meetsVisibilityRequirements(field, visibility)) {
				final FieldWrapper property = resolveBeanProperty(field, beanRestrictions);
				if (property != null) {
					filteredFields.add(property);
				}
			}
		}
		fields.put(_class, filteredFields);
		// determine if we need to look deeper
		final List<Class<?>> interfaces = Arrays.asList(_class.getInterfaces());
		if (!_class.equals(boundaryMarker) && !interfaces.contains(boundaryMarker)) {
			fields.putAll(collectFields(_class.getSuperclass(), boundaryMarker, visibility, beanRestrictions));
		}
		return fields;
	}

	/**
	 * Determines if the visibility modifiers of a given {@link Field} is included in the set of flags.
	 * 
	 * @param field The field who's visibility modifiers we want to test.
	 * @param visibility List of {@link Visibility} flags to test against.
	 * @return Whether a given field has one of the specified visibility flags.
	 */
	static boolean meetsVisibilityRequirements(final Field field, final EnumSet<Visibility> visibility) {
		final int m = field.getModifiers();
		
		for (Visibility visibilityModifier : visibility) {
			if (visibilityModifier != Visibility.DEFAULT) {
				if ((m & visibilityModifier.modifierFlag) != 0) {
					return true;
				}
			} else {
				if (!Modifier.isPrivate(m) && !Modifier.isProtected(m) && !Modifier.isPublic(m)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Determines if a given <code>Field</code> meets the specified Bean restriction requirements and returns the field as a BeanProperty
	 * with optional getter/setter.
	 * 
	 * @param field The field to test.
	 * @param beanRestrictions The Bean restrictions to apply (should/shouldn't have setter/getter).
	 * @return Whether the field fits the restrictions.
	 */
	@Nullable
	static FieldWrapper resolveBeanProperty(final Field field, final EnumSet<BeanRestriction> beanRestrictions) {
		if (beanRestrictions.containsAll(EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER)) //
				|| beanRestrictions.containsAll(EnumSet.of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER))) {
			throw new IllegalArgumentException("cannot both include and exclude a setter/getter requirement");
		}

		// since PropertyUtilsBean#getPropertyDescriptors(...) doesn't detect setters without getters (Bean convention)
		// we'll just find setter/getters manually
		final String setterName = "set" + StringUtils.capitalize(field.getName());
		final String getterName;
		if (field.getType().equals(boolean.class)) {
			getterName = "is" + StringUtils.capitalize(field.getName());
		} else {
			getterName = "get" + StringUtils.capitalize(field.getName());
		}
		final Set<InvokableObject<Method>> iWriteMethod = MethodUtils.findSimpleCompatibleMethod(field.getDeclaringClass(), setterName, field.getType());
		final Set<InvokableObject<Method>> iReadMethod = MethodUtils.findSimpleCompatibleMethod(field.getDeclaringClass(), getterName);

		if (!((!iReadMethod.isEmpty() && beanRestrictions.contains(BeanRestriction.NO_GETTER)) //
				|| (iReadMethod.isEmpty() && beanRestrictions.contains(BeanRestriction.YES_GETTER)) //
				|| (!iWriteMethod.isEmpty() && beanRestrictions.contains(BeanRestriction.NO_SETTER)) //
				|| (iWriteMethod.isEmpty() && beanRestrictions.contains(BeanRestriction.YES_SETTER)))) {
			Method readMethod = !iReadMethod.isEmpty() ? iReadMethod.iterator().next().getMethod() : null;
			Method writeMethod = !iWriteMethod.isEmpty() ? iWriteMethod.iterator().next().getMethod() : null;
			return new FieldWrapper(field, readMethod, writeMethod);
		} else {
			return null;
		}
	}

	/**
	 * Calls the setter for the first field in the inheritance chain that matches given fieldName.
	 * Attempts to convert the value in case the type is incorrect.
	 *
	 * @return The actual value used in the bean setter.
	 */
	@SuppressWarnings("ConstantConditions")
	@Nullable
	static public Object invokeBeanSetter(Object o, String fieldName, @Nullable Object value) {
		for (List<FieldWrapper> fieldWrappers : collectFields(o.getClass(), Object.class, allOf(Visibility.class), of(YES_SETTER)).values()) {
			for (FieldWrapper fieldWrapper : fieldWrappers) {
				if (fieldWrapper.getField().getName().equals(fieldName) ) {
					Object assignedValue = value;
					try {
						MethodUtils.invokeMethodSimple(fieldWrapper.getSetter(), o, value);
					} catch (final IllegalArgumentException ie) {
						try {
							assignedValue = ValueConversionHelper.convert(value, fieldWrapper.getField().getType());
						} catch (IncompatibleTypeException e) {
							throw new RuntimeException(new NoSuchMethodException(e.getMessage()));
						}
						MethodUtils.invokeMethodSimple(fieldWrapper.getSetter(), o, assignedValue);
					}
					return assignedValue;
				}
			}
		}
		throw new RuntimeException(new NoSuchMethodException("Bean setter for " + fieldName));
	}

	/**
	 * Calls the getter for the first field in the inheritance chain that matches given fieldName.
	 *
	 * @see #collectFields(Class, Class, EnumSet, EnumSet)
	 */
	@SuppressWarnings("ConstantConditions")
	static public Object invokeBeanGetter(Object o, String fieldName) {
		for (List<FieldWrapper> fieldWrappers : collectFields(o.getClass(), Object.class, allOf(Visibility.class), of(YES_GETTER)).values()) {
			for (FieldWrapper fieldWrapper : fieldWrappers) {
				if (fieldWrapper.getField().getName().equals(fieldName) ) {
					return MethodUtils.invokeMethodSimple(fieldWrapper.getGetter(), o);
				}
			}
		}
		throw new RuntimeException(new NoSuchMethodException("Bean getter for " + fieldName));
	}
}