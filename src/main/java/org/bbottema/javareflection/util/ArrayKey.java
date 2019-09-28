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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Arrays;

import static org.bbottema.javareflection.util.MiscUtil.requireNonNullOfType;

/**
 * Needed to make sure hashcode and equals are implemented properly for arrays as key in a map.
 */
public class ArrayKey {
	
	private final int hashCode;
	private final Class<?>[] array;
	
	public ArrayKey(Class<?>[] array) {
		this.array = array.clone();
		this.hashCode = Arrays.hashCode(this.array);
	}
	
	@SuppressFBWarnings(value = "EQ_UNUSUAL", justification = "Equals is specifically implemented for performance reasons")
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(Object o) {
		return Arrays.equals(array, requireNonNullOfType(o, ArrayKey.class).array);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(array);
	}
}