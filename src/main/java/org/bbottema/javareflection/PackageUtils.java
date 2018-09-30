package org.bbottema.javareflection;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.bbottema.javareflection.ClassUtils.locateClass;

@UtilityClass
public final class PackageUtils {
	
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