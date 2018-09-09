package org.bbottema.javareflection.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UtilityClass
public final class MiscUtil {
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
	
	@SafeVarargs
	public static <T> List<T> newArrayList(T... o) {
		ArrayList<T> l = new ArrayList<>();
		Collections.addAll(l, o);
		return l;
	}
}
