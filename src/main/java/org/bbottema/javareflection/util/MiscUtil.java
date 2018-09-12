package org.bbottema.javareflection.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public final class MiscUtil {
	
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <T> List<T> newArrayList(T... o) {
		ArrayList<T> l = new ArrayList<>();
		Collections.addAll(l, o);
		return l;
	}
}
