package org.bbottema.javareflection.testmodel.reflectionutils;

import org.bbottema.javareflection.ReflectionUtils;

public class Bob<TResponse> extends Pleb {
	public final Class<Object> responseType;

	public Bob() {
		this.responseType = ReflectionUtils.findParameterType(getClass(), Bob.class, 0);
	}
}