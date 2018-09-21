package org.bbottema.javareflection;

import org.bbottema.javareflection.testmodel.C;
import org.bbottema.javareflection.testmodel.Pear;
import org.bbottema.javareflection.valueconverter.IncompatibleTypeException;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClassUtilsTest {
	
	@Before
	public void resetStaticCaches() {
		ClassUtils.resetCache();
		ValueConversionHelper.resetDefaultConverters();
	}
	
	@Test
	public void testLocateClass() {
		assertThat(ClassUtils.locateClass("Math", false, null)).isEqualTo(Math.class);
		assertThat(ClassUtils.locateClass("Mathh", false, null)).isNull();
		assertThat(ClassUtils.locateClass("ClassUtils", false, null)).isNull();
		assertThat(ClassUtils.locateClass("ClassUtils", true, null)).isEqualTo(ClassUtils.class);
		assertThat(ClassUtils.locateClass("Socket", false, null)).isNull();
		assertThat(ClassUtils.locateClass("Socket", true, null)).isEqualTo(Socket.class);
	}
	
	@Test
	public void testNewInstanceHappyFlow() {
		assertThat(ClassUtils.newInstanceSimple(Object.class).getClass()).isEqualTo(Object.class);
	}
	
	@Test
	public void testSolveField()
			throws NoSuchFieldException {
		Field f1 = ClassUtils.solveField(new C(new Pear()), "numberB");
		assertThat(f1).isNotNull();
		assertThat(f1).isEqualTo(C.class.getField("numberB"));
		Field f2 = ClassUtils.solveField(C.class, "numberB_static");
		assertThat(f2).isNotNull();
		assertThat(f2).isEqualTo(C.class.getField("numberB_static"));
	}
	
	@Test
	public void testAssignToField()
			throws IllegalAccessException, NoSuchFieldException {
		assertThat(ClassUtils.assignToField(new C(new Pear()), "numberB", 50)).isEqualTo(50);
		assertThat(ClassUtils.assignToField(new C(new Pear()), "numberB", "50")).isEqualTo(50);
		assertThat(ClassUtils.assignToField(new C(new Pear()), "numberB", 50d)).isEqualTo(50);
		try {
			ClassUtils.assignToField(new C(new Pear()), "numberB", new Pear());
			fail("IllegalAccessException expected due to incompatible types");
		} catch (IncompatibleTypeException e) {
			// ok
		}
		try {
			ClassUtils.assignToField(new C(new Pear()), "number_privateB", new Pear());
			fail("IllegalAccessException expected due to incompatible types");
		} catch (NoSuchFieldException e) {
			// ok
		}
	}
	
	@Test
	public void testCollectPropertyNames() {
		assertThat(ClassUtils.collectPropertyNames(new C(new Pear())))
				.containsExactlyInAnyOrder("numberA", "numberB", "numberB_static", "numberC");
	}
	
	@Test
	public void testCollectPublicMethodNames() {
		final Object subject = new Object();
		Collection<String> objectProperties = ClassUtils.collectMethodNames(subject.getClass(), true);
		final C subject1 = new C(new Pear());
		Collection<String> cProperties = ClassUtils.collectMethodNames(subject1.getClass(), true);
		assertThat(objectProperties).isNotEmpty();
		assertThat(cProperties).hasSize(objectProperties.size() + 2);
		cProperties.removeAll(objectProperties);
		assertThat(cProperties).containsExactlyInAnyOrder("foo", "bar");
	}
	
	@Test
	public void testCollectAllMethodNames() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Collection<String> objectProperties = ClassUtils.collectMethodNames(Object.class, false);
		Collection<String> cProperties = ClassUtils.collectMethodNames(C.class, false);
		assertThat(objectProperties).isNotEmpty();
		assertThat(cProperties).hasSize(objectProperties.size() + 4);
		cProperties.removeAll(objectProperties);
		assertThat(cProperties).containsExactlyInAnyOrder("foo", "bar", "protectedMethod", "privateMethod");
		MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "privateMethod");
	}
}