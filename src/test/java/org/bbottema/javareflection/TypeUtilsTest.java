package org.bbottema.javareflection;

import org.bbottema.javareflection.testmodel.Fruit;
import org.bbottema.javareflection.testmodel.Pear;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeUtilsTest {
	
	/**
	 * Test for {@link TypeUtils#autobox(Class)}.
	 */
	@Test
	public void testAutobox() {
		assertThat(TypeUtils.autobox(boolean.class)).isEqualTo(Boolean.class);
		assertThat(TypeUtils.autobox(char.class)).isEqualTo(Character.class);
		assertThat(TypeUtils.autobox(byte.class)).isEqualTo(Byte.class);
		assertThat(TypeUtils.autobox(short.class)).isEqualTo(Short.class);
		assertThat(TypeUtils.autobox(int.class)).isEqualTo(Integer.class);
		assertThat(TypeUtils.autobox(long.class)).isEqualTo(Long.class);
		assertThat(TypeUtils.autobox(float.class)).isEqualTo(Float.class);
		assertThat(TypeUtils.autobox(double.class)).isEqualTo(Double.class);
		assertThat(TypeUtils.autobox(Boolean.class)).isEqualTo(boolean.class);
		assertThat(TypeUtils.autobox(Character.class)).isEqualTo(char.class);
		assertThat(TypeUtils.autobox(Byte.class)).isEqualTo(byte.class);
		assertThat(TypeUtils.autobox(Short.class)).isEqualTo(short.class);
		assertThat(TypeUtils.autobox(Integer.class)).isEqualTo(int.class);
		assertThat(TypeUtils.autobox(Long.class)).isEqualTo(long.class);
		assertThat(TypeUtils.autobox(Float.class)).isEqualTo(float.class);
		assertThat(TypeUtils.autobox(Double.class)).isEqualTo(double.class);
		assertThat(TypeUtils.autobox(Fruit.class)).isNull();
	}
	
	/**
	 * Test for {@link TypeUtils#collectTypes(Object[])}.
	 */
	@Test
	public void testCollectTypes() {
		final Class<?>[] expectedTypeList = new Class<?>[]{Pear.class, String.class, Object.class, Double.class};
		final Object[] objectList = new Object[]{new Pear(), "foo", null, 4d};
		assertThat(TypeUtils.collectTypes(objectList)).isEqualTo(expectedTypeList);
	}
	
	/**
	 * Test for {@link TypeUtils#widestNumberClass(Number...)}.
	 */
	@Test
	public void testWidestNumberClass() {
		byte b = 1;
		short s = 1;
		int i = 1;
		long l = 1;
		float f = 1;
		double d = 1;
		Byte B = 1;
		Short S = 1;
		Integer I = 1;
		Long L = 1L;
		Float F = 1f;
		Double D = 1d;
		assertThat(TypeUtils.widestNumberClass(s, b)).isEqualTo(Short.class);
		assertThat(TypeUtils.widestNumberClass(b, B)).isEqualTo(Byte.class);
		assertThat(TypeUtils.widestNumberClass(B, b)).isEqualTo(Byte.class);
		assertThat(TypeUtils.widestNumberClass(b, s, i)).isEqualTo(Integer.class);
		assertThat(TypeUtils.widestNumberClass(b, s, i, l, f, d)).isEqualTo(Double.class);
		assertThat(TypeUtils.widestNumberClass(B, S, I, L, F, D)).isEqualTo(Double.class);
		assertThat(TypeUtils.widestNumberClass(L)).isEqualTo(Long.class);
		assertThat(TypeUtils.widestNumberClass(i, D)).isEqualTo(Double.class);
	}
	
	/**
	 * Test for {@link TypeUtils#isPackage(String)}.
	 */
	@Test
	public void testIsPackage() {
		assertThat(TypeUtils.isPackage("java")).isTrue();
		assertThat(TypeUtils.isPackage("java.util")).isTrue();
		assertThat(TypeUtils.isPackage("org.bbottema.javareflection")).isTrue();
		assertThat(TypeUtils.isPackage("java.lang.reflect")).isTrue();
		assertThat(TypeUtils.isPackage("org.junit")).isTrue();
		assertThat(TypeUtils.isPackage("donkey.cake")).isFalse();
		assertThat(TypeUtils.isPackage("org.bbottema")).isFalse();
	}
	
	/**
	 * Test for {@link TypeUtils#replaceInArray(Object[], int, Object)}.
	 */
	@Test
	public void testReplaceInArray() {
		Integer[] initial = new Integer[]{1, 2, 3, 4};
		Integer[] second = TypeUtils.replaceInArray(initial, 2, 2);
		assertThat(second).isEqualTo(initial);
		assertThat(second).isEqualTo(new Integer[]{1, 2, 2, 4});
	}
}