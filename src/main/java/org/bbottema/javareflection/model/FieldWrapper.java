package org.bbottema.javareflection.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A wrapper class that keeps a property ({@link Field}) and its setter/getter method(s) in one place.
 */
@Value
@SuppressFBWarnings(justification = "Generated code")
public class FieldWrapper {

	@NotNull
	private final Field field;
	@Nullable
	private final Method getter;
	@Nullable
	private final Method setter;
}