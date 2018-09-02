package org.bbottema.javareflection.valueconverter;

/**
 * This exception can be thrown in any of the conversion methods of {@link ValueConversionHelper}, to indicate a value could not be converted
 * into the target datatype. It doesn't mean a failed attempt at a conversion, it means that there was no way to convert the input value
 * to begin with.
 *
 * @author Benny Bottema
 */
public final class IncompatibleTypeException extends RuntimeException {
	private static final long serialVersionUID = -9234872336546L;

	private static final String pattern = "error: unable to convert value '%s': '%s' to '%s'";

	/**
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	IncompatibleTypeException(final String message, final Exception e) {
		super(message, e);
	}

	/**
	 * @see RuntimeException#RuntimeException(String)
	 */
	public IncompatibleTypeException(final Object value, final String className, final String targetName) {
		super(String.format(pattern, value, className, targetName));
	}

	/**
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public IncompatibleTypeException(final Object value, final String className, final String targetName,
									 final Exception nestedException) {
		super(String.format(pattern, value, className, targetName), nestedException);
	}
}
