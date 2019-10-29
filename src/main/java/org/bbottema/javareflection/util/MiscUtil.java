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
package org.bbottema.javareflection.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@UtilityClass
public final class MiscUtil {
	
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <T> List<T> newArrayList(T... o) {
		ArrayList<T> l = new ArrayList<>();
		Collections.addAll(l, o);
		return l;
	}
	
	@SuppressWarnings("unchecked")
	@NotNull
	public static <T> T trustedCast(Object o) {
		return (T) o;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> T trustedNullableCast(@Nullable Object o) {
		return (T) o;
	}
	
	@SuppressWarnings({"unchecked", "WeakerAccess"})
	@NotNull
	public static <T> T requireNonNullOfType(Object o, Class<T> type) {
		if (requireNonNull(o).getClass() != type) {
			throw new AssertionError(format("got type %s, expected type %s", o.getClass(), type));
		}
		return (T) o;
	}
}