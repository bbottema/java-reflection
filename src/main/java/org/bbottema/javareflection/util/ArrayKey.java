package org.bbottema.javareflection.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Arrays;

import static org.bbottema.javareflection.util.MiscUtil.requireNonNullOfType;

/**
 * Needed to make sure hashcode and equals are implemented properly for arrays as key in a map.
 */
public class ArrayKey {
	
	private final int hashCode;
	private final Class<?>[] array;
	
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Performance optmization")
	public ArrayKey(Class<?>[] array) {
		this.array = array.clone();
		this.hashCode = Arrays.hashCode(this.array);
	}
	
	@SuppressFBWarnings(value = "EQ_UNUSUAL", justification = "Equals is specifically implemented for performance reasons")
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(Object o) {
		return Arrays.equals(array, requireNonNullOfType(o, ArrayKey.class).array);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(array);
	}
}