package org.bbottema.javareflection.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumSet;

import static java.util.EnumSet.allOf;

@SuppressWarnings("unused")
public enum MethodModifier {
	PUBLIC(Modifier.PUBLIC),
	PROTECTED(Modifier.PROTECTED),
	PRIVATE(Modifier.PRIVATE),
	ABSTRACT(Modifier.ABSTRACT),
	DEFAULT(-1),
	STATIC(Modifier.STATIC),
	FINAL(Modifier.FINAL),
	SYNCHRONIZED(Modifier.SYNCHRONIZED),
	NATIVE(Modifier.NATIVE),
	STRICT(Modifier.STRICT);
	
	public static final EnumSet<MethodModifier> MATCH_ANY = allOf(MethodModifier.class);
	
	private final int modifierFlag;
	
	MethodModifier(int modifierFlag) {
		this.modifierFlag = modifierFlag;
	}
	
	public static boolean meetsModifierRequirements(Method method, EnumSet<MethodModifier> modifiers) {
		final int m = method.getModifiers();
		
		for (MethodModifier methodModifier : modifiers) {
			if (methodModifier != MethodModifier.DEFAULT) {
				if ((m & methodModifier.modifierFlag) != 0) {
					return true;
				}
			} else {
				if (!Modifier.isPrivate(m) && !Modifier.isProtected(m) && !Modifier.isPublic(m)) {
					return true;
				}
			}
		}
		return false;
	}
}