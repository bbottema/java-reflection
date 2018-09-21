package org.bbottema.javareflection.util;

import org.assertj.core.api.iterable.Extractor;
import org.bbottema.javareflection.MethodUtils;

public class MethodCallingExtractor<T> implements Extractor<Object, T> {
	private final String methodName;
	
	public MethodCallingExtractor(String methodName) {
		this.methodName = methodName;
	}
	
	@Override
	public T extract(Object input) {
		try {
			return MethodUtils.invokeCompatibleMethod(input, input.getClass(), methodName);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
