package org.codemonkey.javareflection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A wrapper class that keeps a property ({@link Field}) and its setter/getter method(s) in one place.
 * 
 * @author Benny Bottema
 */
public class FieldWrapper {

	@Nonnull
	private final Field field;
	@Nullable
	private final Method getter;
	@Nullable
	private final Method setter;

	/**
	 * Initializes the wrapper with field, getter and setter, all optional.
	 * 
	 * @param field A {@link Field}.
	 * @param getter A getter {@link Method} for the field.
	 * @param setter A setter {@link Method} for the field.
	 */
	public FieldWrapper(@Nonnull Field field, @Nullable Method getter, @Nullable Method setter) {
		this.field = field;
		this.getter = getter;
		this.setter = setter;
	}

	/**
	 * @return {@link #field}.
	 */
	@Nonnull
	public Field getField() {
		return field;
	}

	/**
	 * @return {@link #getter}.
	 */
	@Nullable
	public Method getGetter() {
		return getter;
	}

	/**
	 * @return {@link #setter}.
	 */
	@Nullable
	public Method getSetter() {
		return setter;
	}
}