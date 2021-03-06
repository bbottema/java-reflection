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
