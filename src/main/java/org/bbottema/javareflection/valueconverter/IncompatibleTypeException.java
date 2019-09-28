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

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * This exception can be thrown in any of the conversion methods of {@link ValueConversionHelper}, to indicate a value could not be converted into the
 * target datatype. It doesn't mean a failed attempt at a conversion, it means that there was no way to convert the input value to begin with.
 */
@SuppressWarnings("serial")
public final class IncompatibleTypeException extends RuntimeException {
	private final List<IncompatibleTypeException> causes = new ArrayList<>();
	
	public IncompatibleTypeException(Object value, Class<?> fromType, Class<?> targetType) {
		this(value, fromType, targetType, (Throwable) null);
	}
	
	public IncompatibleTypeException(Object value, Class<?> fromType, Class<?> targetType, @Nullable Throwable cause) {
		super(format("error: unable to convert value '%s': '%s' to '%s'", value, fromType, targetType), cause);
	}
	
	public IncompatibleTypeException(Object value, Class<?> fromType, Class<?> targetType, List<IncompatibleTypeException> causes) {
		this(value, fromType, targetType, (Throwable) null);
		this.causes.addAll(causes);
	}
	
	public List<IncompatibleTypeException> getCauses() {
		return causes;
	}
}