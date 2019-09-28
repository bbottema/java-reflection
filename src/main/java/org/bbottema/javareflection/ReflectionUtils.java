/*
 * Copyright (C) ${project.inceptionYear} Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bbottema.javareflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * This util is able to find Generic types as Class instances. This is useful for determining class types in runtime, which is (mostly) used by
 * converters.
 */
public class ReflectionUtils {

	@SuppressWarnings("FieldCanBeLocal")
	private static final String INVALID_GENERIC_TYPE_DEFINITION = "Unable to determine generic type, probably due to type erasure. Make sure the type is part of a class signature (it can not be a field or variable, or a nested generic type such as List<NestedType>)";

	/**
	 * Inspects a inheritance chain of classes until the <em>classOfInterest</em> is found and then will look for the Generic type declared for the given (zero-based) index.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> findParameterType(Class<?> instanceClass, Class<?> classOfInterest, int parameterIndex) {
		Map<Type, Type> typeMap = new HashMap<>();
		while (classOfInterest != instanceClass.getSuperclass()) {
			extractTypeArguments(typeMap, instanceClass);
			instanceClass = instanceClass.getSuperclass();
			if (instanceClass == null)
				throw new IllegalArgumentException();
		}

		ParameterizedType parameterizedType = (ParameterizedType) instanceClass.getGenericSuperclass();
		Type actualType = parameterizedType.getActualTypeArguments()[parameterIndex];
		if (typeMap.containsKey(actualType)) {
			actualType = typeMap.get(actualType);
		}
		if (actualType instanceof Class) {
			return (Class<T>) actualType;
		} else {
			try {
				return (Class<T>) ((ParameterizedType) actualType).getRawType();
			} catch (ClassCastException e) {
				throw new IllegalStateException(INVALID_GENERIC_TYPE_DEFINITION, e);
			}
		}
	}

	private static void extractTypeArguments(Map<Type, Type> typeMap, Class<?> clazz) {
		Type genericSuperclass = clazz.getGenericSuperclass();
		if (!(genericSuperclass instanceof ParameterizedType)) {
			return;
		}

		ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
		Type[] typeParameter = ((Class<?>) parameterizedType.getRawType()).getTypeParameters();
		Type[] actualTypeArgument = parameterizedType.getActualTypeArguments();
		for (int i = 0; i < typeParameter.length; i++) {
			if (typeMap.containsKey(actualTypeArgument[i])) {
				actualTypeArgument[i] = typeMap.get(actualTypeArgument[i]);
			}
			typeMap.put(typeParameter[i], actualTypeArgument[i]);
		}
	}
}
