package org.bbottema.javareflection.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	
	@SuppressWarnings("unchecked")
	@NotNull
	public static <T> T trustedCast(@NotNull Object o) {
		return (T) o;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> T trustedNullableCast(@Nullable Object o) {
		return (T) o;
	}
}