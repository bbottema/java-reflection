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

import static org.bbottema.javareflection.util.MiscUtil.trustedCast;

public interface Function<F, T> {
	T apply(F value);
	
	class Functions {
		
		private static final Function<?, ?> IDENTITY_FUNCTION = new Function<Object, Object>() {
			@Override
			public Object apply(Object value) {
				return value;
			}
		};
		
		private static final Function<?, String> TOSTRING_FUNCTION = new Function<Object, String>() {
			@Override
			public String apply(Object value) {
				return value.toString();
			}
		};
		
		public static <FT> Function<FT, FT> identity() {
			return trustedCast(IDENTITY_FUNCTION);
		}
		
		public static <F> Function<F, String> simpleToString() {
			return trustedCast(TOSTRING_FUNCTION);
		}
	}
}