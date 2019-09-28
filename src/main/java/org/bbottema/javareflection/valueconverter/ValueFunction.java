/*
 * Copyright (C) ${project.inceptionYear} Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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