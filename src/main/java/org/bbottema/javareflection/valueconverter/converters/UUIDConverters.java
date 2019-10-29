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
package org.bbottema.javareflection.valueconverter.converters;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.util.Function;
import org.bbottema.javareflection.util.Function.Functions;
import org.bbottema.javareflection.valueconverter.ValueFunction;
import org.bbottema.javareflection.valueconverter.ValueFunction.ValueFunctionImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Nullable
@UtilityClass
public final class UUIDConverters {
	
	public static final Collection<ValueFunction<UUID, ?>> UUID_CONVERTERS = produceUUIDConverters();
	
	private static Collection<ValueFunction<UUID, ?>> produceUUIDConverters() {
		ArrayList<ValueFunction<UUID, ?>> converters = new ArrayList<>();
		converters.add(new ValueFunctionImpl<>(UUID.class, UUID.class, Functions.<UUID>identity()));
		converters.add(new ValueFunctionImpl<>(UUID.class, String.class, new UUIDToStringFunction()));
		return converters;
	}
	
	private static class UUIDToStringFunction implements Function<UUID, String> {
		@Override
		public String apply(UUID value) {
			return value.toString();
		}
	}
}