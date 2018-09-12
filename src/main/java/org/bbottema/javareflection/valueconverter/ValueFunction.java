package org.bbottema.javareflection.valueconverter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bbottema.javareflection.util.Function;

import javax.annotation.Nonnull;

/**
 * Can be used to provide optional user converters. User converters also act as intermediate converters, ie. if a user converter can go to
 * <code>int</code>, <code>double</code> is automatically supported as well as common conversion.
 */
public interface ValueFunction<F, T> {
	@Nonnull Class<F> getFromType();
	@Nonnull Class<T> getTargetType();
	@Nonnull T convertValue(@Nonnull F value);
	
	/**
	 * Helper class to quickly define a {@link ValueFunction} from a {@link Function}.
	 */
	@Getter
	@RequiredArgsConstructor
	@ToString(onlyExplicitlyIncluded = true)
	class ValueFunctionImpl<F, T> implements ValueFunction<F, T> {
		@Nonnull @ToString.Include protected final Class<F> fromType;
		@Nonnull @ToString.Include protected final Class<T> targetType;
		@Nonnull private final Function<F, T> converter;
		@Nonnull @Override
		public final T convertValue(@Nonnull F value) {
			return converter.apply(value);
		}
	}
}