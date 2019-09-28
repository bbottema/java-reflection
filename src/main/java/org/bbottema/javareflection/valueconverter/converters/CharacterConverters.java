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
 * Attempts to convert a <code>Character</code> to the target datatype.
 * <p>
 * Conversions are as follows:
 * <ol>
 * <li><strong>String</strong>: <code>value.toString()</code></li>
 * <li><strong>Character (or primitive character)</strong>: <code>value</code></li>
 * <li><strong>Number (or primitive number)</strong>: Deferred to ({@link Character#getNumericValue(char)}) or cast to {@code int} if not in number
 * 0-9 range.</li>
 * <li><strong>Boolean (or boolean)</strong>: <code>!value.equals('0')</code></li>
 * </ol>
 */
@UtilityClass
public final class CharacterConverters {
	
	public static final Collection<ValueFunction<Character, ?>> CHARACTER_CONVERTERS = produceCharacterConverters();
	
	private static Collection<ValueFunction<Character, ?>> produceCharacterConverters() {
		ArrayList<ValueFunction<Character, ?>> converters = new ArrayList<>();
		converters.add(new ValueFunctionImpl<>(char.class, Character.class, Functions.<Character>identity()));
		converters.add(new ValueFunctionImpl<>(Character.class, char.class, Functions.<Character>identity()));
		converters.add(new ValueFunctionImpl<>(char.class, char.class, Functions.<Character>identity()));
		converters.add(new ValueFunctionImpl<>(Character.class, Character.class, Functions.<Character>identity()));
		converters.add(new ValueFunctionImpl<>(Character.class, String.class, Functions.<Character>simpleToString()));
		converters.add(new ValueFunctionImpl<>(Character.class, Number.class, new CharacterToNumberFunction()));
		converters.add(new ValueFunctionImpl<>(Character.class, Boolean.class, new CharacterToBooleanFunction()));
		return converters;
	}
	
	private static class CharacterToNumberFunction implements Function<Character, Number> {
		@Override
		public Number apply(Character value) {
			int numericValue = Character.getNumericValue(value);
			return numericValue == -1 || numericValue > 9
					? (int) value
					: numericValue;
		}
	}
	
	private static class CharacterToBooleanFunction implements Function<Character, Boolean> {
		@Override
		public Boolean apply(Character value) {
			return !value.equals('0');
		}
	}
}
