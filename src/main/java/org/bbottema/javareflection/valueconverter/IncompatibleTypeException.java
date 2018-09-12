package org.bbottema.javareflection.valueconverter;

import static java.lang.String.format;

/**
 * This exception can be thrown in any of the conversion methods of {@link ValueConversionHelper}, to indicate a value could not be converted into the
 * target datatype. It doesn't mean a failed attempt at a conversion, it means that there was no way to convert the input value to begin with.
 *
 * @author Benny Bottema
 */
public final class IncompatibleTypeException extends RuntimeException {
	public IncompatibleTypeException(Object value, Class<?> fromType, Class<?> targetType) {
		this(value, fromType, targetType, null);
	}
	
	public IncompatibleTypeException(Object value, Class<?> fromType, Class<?> targetType, Throwable cause) {
		super(format("error: unable to convert value '%s': '%s' to '%s'", value, fromType, targetType), cause);
	}
}