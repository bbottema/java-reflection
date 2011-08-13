package org.codemonkey.javareflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A wrapper class that keeps the property and its setter/getter in one place.
 * 
 * @author Benny Bottema
 */
public class FieldWrapper {

	private final Field field;
	private final Method getter;
	private final Method setter;

	/**
	 * Initializes the wrapper with field, getter and setter, all optional.
	 * 
	 * @param field A {@link Field}.
	 * @param getter A getter {@link Method} for the field.
	 * @param setter A setter {@link Method} for the field.
	 */
	public FieldWrapper(final Field field, final Method getter, final Method setter) {
		this.field = field;
		this.getter = getter;
		this.setter = setter;
	}

	/**
	 * @return {@link #field}.
	 */
	public Field getField() {
		return field;
	}

	/**
	 * @return {@link #getter}.
	 */
	public Method getGetter() {
		return getter;
	}

	/**
	 * @return {@link #setter}.
	 */
	public Method getSetter() {
		return setter;
	}
}