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