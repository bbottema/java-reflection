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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Attempts to convert a <code>Boolean</code> to the target datatype.
 * <p>
 * Conversions are as follows:
 * <ol>
 * <li><strong>Boolean (or boolean)</strong>: <code>value</code></li>
 * <li><strong>String</strong>: <code>value.toString()</code></li>
 * <li><strong>Number (or primitive number)</strong>: 0 or 1</li>
 * </ol>
 */
@UtilityClass
public final class BooleanConverters {
	
	public static final Collection<ValueFunction<Boolean, ?>> BOOLEAN_CONVERTERS = produceBooleanConverters();
	
	private static Collection<ValueFunction<Boolean, ?>> produceBooleanConverters() {
		ArrayList<ValueFunction<Boolean, ?>> converters = new ArrayList<>();
		converters.add(new ValueFunctionImpl<>(boolean.class, Boolean.class, Functions.<Boolean>identity()));
		converters.add(new ValueFunctionImpl<>(Boolean.class, boolean.class, Functions.<Boolean>identity()));
		converters.add(new ValueFunctionImpl<>(boolean.class, boolean.class, Functions.<Boolean>identity()));
		converters.add(new ValueFunctionImpl<>(Boolean.class, Boolean.class, Functions.<Boolean>identity()));
		
		converters.add(new ValueFunctionImpl<>(Boolean.class, String.class, Functions.<Boolean>simpleToString()));
		converters.add(new ValueFunctionImpl<>(Boolean.class, Number.class, new BooleanToNumberFunction()));
		return converters;
	}
	
	private static class BooleanToNumberFunction implements Function<Boolean, Number> {
		@Override
		public Number apply(Boolean value) {
			return value ? 1 : 0;
		}
	}
}
