package org.bbottema.javareflection.util;

import java.util.HashSet;
import java.util.Set;

public final class MiscUtil {
	private MiscUtil() {
	}
	
	
	public static void assumeTrue(boolean expression) {
		if (!expression) {
			throw new IllegalArgumentException("The validated expression is false");
		}
	}
	
	@SuppressWarnings("SameParameterValue")
	public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
		final Set<T> intersection = new HashSet<>(set1);
		intersection.retainAll(set2);
		return intersection;
	}
}
