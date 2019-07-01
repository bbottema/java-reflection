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