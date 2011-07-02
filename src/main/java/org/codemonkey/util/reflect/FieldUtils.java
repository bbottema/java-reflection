package org.codemonkey.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A {@link Field} shorthand utility class mainly used to collect fields from classes meeting certain restrictions/requirements.
 * 
 * @author Benny Bottema
 * @see #collectFields(Class, Class, EnumSet, EnumSet)
 */
public final class FieldUtils {
	private FieldUtils() {
	}

	/**
	 * Determines what visibility modifiers a field is allowed to have in {@link FieldUtils#collectFields(Class, Class, EnumSet, EnumSet)}.
	 * 
	 * @author Benny Bottema
	 */
	public enum Visibility {
		PRIVATE(Modifier.PRIVATE), //
		DEFAULT(-1), // no Java equivalent
		PROTECTED(Modifier.PROTECTED), //
		PUBLIC(Modifier.PUBLIC);
		private int modifierFlag;

		private Visibility(final int modifierFlag) {
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
		YES_GETTER, // getter required
		YES_SETTER, // setter required
		NO_SETTER, // setter not allowed
		NO_GETTER; // getter not allowed
	}

	/**
	 * Returns a pool of {@link Field} wrappers including optional relevant setter/getter methods, collected from the given class tested
	 * against the given visibility and Bean restriction requirements.
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
	public static Map<Class<?>, List<FieldWrapper>> collectFields(final Class<?> _class, final Class<?> boundaryMarker,
			final EnumSet<Visibility> visibility, final EnumSet<BeanRestriction> beanRestrictions) {
		final Map<Class<?>, List<FieldWrapper>> fields = new HashMap<Class<?>, List<FieldWrapper>>();
		final Field[] allFields = _class.getDeclaredFields();
		final List<FieldWrapper> filteredFields = new LinkedList<FieldWrapper>();
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
				|| (!(readMethod != null) && beanRestrictions.contains(BeanRestriction.YES_GETTER)) //
				|| (writeMethod != null && beanRestrictions.contains(BeanRestriction.NO_SETTER)) //
		|| (!(writeMethod != null) && beanRestrictions.contains(BeanRestriction.YES_SETTER)))) {
			return new FieldWrapper(field, readMethod, writeMethod);
		} else {
			return null;
		}
	}
}