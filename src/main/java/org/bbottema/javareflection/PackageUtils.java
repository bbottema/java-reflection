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

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("WeakerAccess")
@UtilityClass
public final class PackageUtils {
	
	@Nullable
	static Class<?> scanPackagesForClass(String className, @Nullable String inPackage, @Nullable ClassLoader classLoader) {
		// cycle through all sub-packages and try allocating class dynamically
		for (Package currentPackage : Package.getPackages()) {
			final String packageName = currentPackage.getName();
			if (inPackage == null || packageName.startsWith(inPackage)) {
				final Class<?> _class = ClassUtils.locateClass(packageName + "." + className, classLoader);
				if (_class != null) {
					return _class;
				}
			}
		}
		return null;
	}
}