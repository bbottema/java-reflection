package org.bbottema.javareflection;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.commonslang25.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link Field} shorthand utility class mainly used to collect fields from classes meeting certain restrictions/requirements.
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
 * @author Benny Bottema
 * @see #collectFields(Class, Class, EnumSet, EnumSet)
 */
@UtilityClass
public final class FieldUtils {

	/**
	 * Determines what visibility modifiers a field is allowed to have in {@link FieldUtils#collectFields(Class, Class, EnumSet, EnumSet)}.
	 * 
	 * @author Benny Bottema
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
	 * 
	 * @author Benny Bottema
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
	 * Returns a pool of {@link Field} wrappers including optional relevant setter/getter methods, collected from the given class tested
	 * against the given visibility and Bean restriction requirements.
	 * <p>
	 * The returned fields are mapped against the classes they were found on, since field names can be declared multiple times with the same
	 * name.
	 * 
	 * @param _class The class (and chain) to harvest fields from.
	 * @param boundaryMarker The last <code>class></code> or <code>interface</code> implementing class that fields are collected from. Can
	 *            be used to prevent finding fields on a supper class.
	 * @param visibility A set of visibility requirements (ie. {@link Visibility#PROTECTED} indicates a field is allowed to have
	 *            <code>protected</code> visibility).
	 * @param beanRestrictions A set of Bean restriction requirements indicating a field should or shouldn't have a setter, getter or both.
	 * @return A Map per class in the chain with the fields declared by that class.
	 * @see #meetsVisibilityRequirements(Field, EnumSet)
	 * @see #resolveBeanProperty(Field, EnumSet)
	 */
	@Nonnull
	public static Map<Class<?>, List<FieldWrapper>> collectFields(final Class<?> _class, final Class<?> boundaryMarker,
			final EnumSet<Visibility> visibility, final EnumSet<BeanRestriction> beanRestrictions) {
		final Map<Class<?>, List<FieldWrapper>> fields = new HashMap<>();
		final Field[] allFields = _class.getDeclaredFields();
		final List<FieldWrapper> filteredFields = new LinkedList<>();
		for (final Field field : allFields) {
			if (meetsVisibilityRequirements(field, visibility)) {
				final FieldWrapper property = resolveBeanProperty(field, beanRestrictions);
				if (property != null) {
					filteredFields.add(property);
				}
			}
			fields.put(_class, filteredFields);
		}
		// determine if we need to look deeper
		final List<Class<?>> interfaces = Arrays.asList(_class.getInterfaces());
		if (_class.equals(boundaryMarker) || interfaces.contains(boundaryMarker)) {
			return fields;
		} else {
			fields.putAll(collectFields(_class.getSuperclass(), boundaryMarker, visibility, beanRestrictions));
			return fields;
		}
	}

	/**
	 * Determines if the visibility modifiers of a given {@link Field} is included in the set of flags.
	 * 
	 * @param field The field who's visibility modifiers we want to test.
	 * @param visibility List of {@link Visibility} flags to test against.
	 * @return Whether a given field has one of the specified visibility flags.
	 */
	static boolean meetsVisibilityRequirements(final Field field, final EnumSet<Visibility> visibility) {
		for (final Visibility visibilityModifier : visibility) {
			final int m = field.getModifiers();
			if (!visibilityModifier.equals(Visibility.DEFAULT)) {
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
		final Method writeMethod = JReflect.findSimpleCompatibleMethod(field.getDeclaringClass(), setterName, field.getType());
		final Method readMethod = JReflect.findSimpleCompatibleMethod(field.getDeclaringClass(), getterName);

		if (!((readMethod != null && beanRestrictions.contains(BeanRestriction.NO_GETTER)) //
				|| (readMethod == null && beanRestrictions.contains(BeanRestriction.YES_GETTER)) //
				|| (writeMethod != null && beanRestrictions.contains(BeanRestriction.NO_SETTER)) //
		|| (writeMethod == null && beanRestrictions.contains(BeanRestriction.YES_SETTER)))) {
			return new FieldWrapper(field, readMethod, writeMethod);
		} else {
			return null;
		}
	}
}