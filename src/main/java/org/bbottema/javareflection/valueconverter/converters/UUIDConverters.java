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