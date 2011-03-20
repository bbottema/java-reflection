package org.codemonkey.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Benny Bottema
 */
public class FieldWrapper {

	private final Field field;
	private final Method getter;
	private final Method setter;

	public FieldWrapper(final Field field, final Method getter, final Method setter) {
		this.field = field;
		this.getter = getter;
		this.setter = setter;
	}

	public Field getField() {
		return field;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}
}