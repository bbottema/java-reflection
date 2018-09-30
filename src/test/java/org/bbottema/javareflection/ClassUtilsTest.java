package org.bbottema.javareflection;

import org.bbottema.javareflection.testmodel.A;
import org.bbottema.javareflection.testmodel.C;
import org.bbottema.javareflection.testmodel.Meta;
import org.bbottema.javareflection.testmodel.Moo;
import org.bbottema.javareflection.testmodel.Pear;
import org.bbottema.javareflection.testmodel.Shmoo;
import org.bbottema.javareflection.util.MetaAnnotationExtractor;
import org.bbottema.javareflection.valueconverter.IncompatibleTypeException;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.EnumSet.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.bbottema.javareflection.model.MethodModifier.MATCH_ANY;
import static org.bbottema.javareflection.model.MethodModifier.PUBLIC;

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
		Collection<String> objectProperties = ClassUtils.collectMethodNames(subject.getClass(), Object.class, of(PUBLIC));
		final C subject1 = new C(new Pear());
		Collection<String> cProperties = ClassUtils.collectMethodNames(subject1.getClass(), Object.class, of(PUBLIC));
		assertThat(objectProperties).isNotEmpty();
		assertThat(cProperties).hasSize(objectProperties.size() + 2);
		cProperties.removeAll(objectProperties);
		assertThat(cProperties).containsExactlyInAnyOrder("foo", "bar");
	}
	
	@Test
	public void testCollectAllMethodNames() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Collection<String> objectProperties = ClassUtils.collectMethodNames(Object.class, Object.class, MATCH_ANY);
		Collection<String> cProperties = ClassUtils.collectMethodNames(C.class, Object.class, MATCH_ANY);
		assertThat(objectProperties).isNotEmpty();
		assertThat(cProperties).hasSize(objectProperties.size() + 4);
		cProperties.removeAll(objectProperties);
		assertThat(cProperties).containsExactlyInAnyOrder("foo", "bar", "protectedMethod", "privateMethod");
		MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "privateMethod");
	}
	
	@Test
	public void testCollectMethods() {
		Set<Method> methodsOnC = ClassUtils.collectMethods(C.class, A.class, of(PUBLIC));
		assertThat(methodsOnC).extracting("name").containsExactlyInAnyOrder("foo", "bar");
	}
	
	@Test
	public void testCollectMethodsByName() {
		assertThat(ClassUtils.collectMethodsByName(C.class, C.class, MATCH_ANY, "foo"))
				.extracting("name").containsExactlyInAnyOrder("foo");
		assertThat(ClassUtils.collectMethodsByName(C.class, Object.class, MATCH_ANY, "protectedMethod"))
				.extracting("name").containsExactlyInAnyOrder("protectedMethod", "protectedMethod", "protectedMethod");
	}
	
	@Test
	public void testCollectMethodsMappingToName() {
		Map<String, Set<Method>> methodsByNames = ClassUtils.collectMethodsMappingToName(Moo.class, Shmoo.class, MATCH_ANY);
		
		assertThat(methodsByNames).containsOnlyKeys("method1", "method2");
		
		assertThat(methodsByNames.get("method1"))
				.extracting(new MetaAnnotationExtractor<>(Meta.class))
				.extractingResultOf("value")
				.containsExactlyInAnyOrder(
						"Moo.method1-A", "Moo.method1-B", "Moo.method1-C",
						"Shmoo.method1-A", "Shmoo.method1-B", "Shmoo.method1-C");
		
		assertThat(methodsByNames.get("method2"))
				.extracting(new MetaAnnotationExtractor<>(Meta.class))
				.extractingResultOf("value")
				.containsExactlyInAnyOrder("Moo.method2-A", "Shmoo.method2-A");
	}
}