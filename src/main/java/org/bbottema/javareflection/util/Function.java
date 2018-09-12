package org.bbottema.javareflection.util;

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
		
		@SuppressWarnings("unchecked")
		public static <FT> Function<FT, FT> identity() {
			return (Function<FT, FT>) IDENTITY_FUNCTION;
		}
		
		@SuppressWarnings("unchecked")
		public static <F> Function<F, String> simpleToString() {
			return (Function<F, String>) TOSTRING_FUNCTION;
		}
	}
}