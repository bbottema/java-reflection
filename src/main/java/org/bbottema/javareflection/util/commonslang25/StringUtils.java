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
package org.bbottema.javareflection.util.commonslang25;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "imported code as-is from Apache Commons Lang 2.5")
public class StringUtils {
	private StringUtils() {
	}
	
	/**
	 * <p>Capitalizes a String changing the first letter to title case as
	 * per {@link Character#toTitleCase(char)}. No other letters are changed.</p>
	 *
	 * <p>For a word based algorithm, see WordUtils#capitalize(String).
	 * A <code>null</code> input String returns <code>null</code>.</p>
	 *
	 * <pre>
	 * StringUtils.capitalize(null)  = null
	 * StringUtils.capitalize("")    = ""
	 * StringUtils.capitalize("cat") = "Cat"
	 * StringUtils.capitalize("cAt") = "CAt"
	 * </pre>
	 *
	 * @param str the String to capitalize, may be null
	 *
	 * @return the capitalized String, <code>null</code> if null String input
	 * @since 2.0
	 */
	public static String capitalize(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		return Character.toTitleCase(str.charAt(0)) + str.substring(1);
	}
}