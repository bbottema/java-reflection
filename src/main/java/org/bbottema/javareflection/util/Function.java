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