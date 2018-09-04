package org.bbottema.javareflection.valueconverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Can be used to provide optional user converters. User converters also act as intermediate converters, ie. if a user converter can go to
 * <code>int</code>, <code>double</code> is automatically supported as well as common conversion.
 */
public interface ValueFunction<F, T> {
	@Nonnull Class<F> fromType();
	@Nonnull Class<T> targetType();
	@Nonnull Class<T> convertValue(@Nonnull F value);
}
