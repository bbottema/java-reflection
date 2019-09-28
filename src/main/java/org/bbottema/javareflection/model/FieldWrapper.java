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