package org.bbottema.javareflection.testmodel.reflectionutils;

import org.bbottema.javareflection.ReflectionUtils;

public class Moo<T> extends Schmoo<T> {
	public final Class<T> responseType;

	public Moo() {
		this.responseType = ReflectionUtils.findParameterType(getClass(), Bob.class, 0);
	}
};