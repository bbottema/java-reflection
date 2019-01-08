package org.bbottema.javareflection.valueconverter;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bbottema.javareflection.util.Function;

/**
 * Can be used to provide optional user converters. User converters also act as intermediate converters, ie. if a user converter can go to
 * <code>int</code>, <code>double</code> is automatically supported as well as common conversion.
 */
public interface ValueFunction<F, T> {
	@NonNull Class<F> getFromType();
	@NonNull Class<T> getTargetType();
	@NonNull T convertValue(@NonNull F value) throws IncompatibleTypeException;
	
	/**
	 * Helper class to quickly define a {@link ValueFunction} from a {@link Function}.
	 */
	@Getter
	@RequiredArgsConstructor
	@ToString(onlyExplicitlyIncluded = true)
	class ValueFunctionImpl<F, T> implements ValueFunction<F, T> {
		@NonNull @ToString.Include protected final Class<F> fromType;
		@NonNull @ToString.Include protected final Class<T> targetType;
		@NonNull private final Function<F, T> converter;
		@NonNull @Override
		public final T convertValue(@NonNull F value) throws IncompatibleTypeException {
			return converter.apply(value);
		}
	}
}