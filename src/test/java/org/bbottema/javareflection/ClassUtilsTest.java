package org.bbottema.javareflection;

import org.bbottema.javareflection.testmodel.A;
import org.bbottema.javareflection.testmodel.C;
import org.bbottema.javareflection.testmodel.Meta;
import org.bbottema.javareflection.testmodel.Moo;
import org.bbottema.javareflection.testmodel.Pear;
import org.bbottema.javareflection.testmodel.Shmoo;
import org.bbottema.javareflection.util.MetaAnnotationExtractor;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.EnumSet.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.bbottema.javareflection.model.MethodModifier.MATCH_ANY;
import static org.bbottema.javareflection.model.MethodModifier.PUBLIC;

public class ClassUtilsTest {
	
	@BeforeEach
	public void resetStaticCaches() {
		ValueConversionHelper.resetDefaultConverters();
	}
	
	@Test
	public void testLocateClass() {
		assertThat(ClassUtils.locateClass("Locale", false, null)).isEqualTo(Locale.class);
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
		assertThat(ClassUtils.solveField(new C(new Pear()), "numberB")).isEqualTo(C.class.getField("numberB"));
		assertThat(ClassUtils.solveField(C.class, "numberB_static")).isEqualTo(C.class.getField("numberB_static"));
		assertThat(ClassUtils.solveField(C.class, "number_privateC")).isEqualTo(C.class.getDeclaredField("number_privateC"));
	}

	@Test
	public void testSolveFieldValue() {
		assertThat(ClassUtils.<Integer>solveFieldValue(Integer.class, "MAX_VALUE")).isEqualTo(Integer.MAX_VALUE);
		final C instance = new C(new Pear());
		assertThat(ClassUtils.<Integer>solveFieldValue(instance, "numberC")).isNull();
		instance.updateNumberC(100);
		assertThat(ClassUtils.<Integer>solveFieldValue(instance, "numberC")).isEqualTo(100);
		instance.updateNumber_privateC(1234);
		assertThat(ClassUtils.<Integer>solveFieldValue(instance, "number_privateC")).isEqualTo(1234);
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
		} catch (NoSuchFieldException e) {
			assertThat(e.getMessage()).contains("unable to convert value");
		}
		try {
			ClassUtils.assignToField(new C(new Pear()), "number_privateB", new Pear());
			fail("IllegalAccessException expected due to incompatible types");
		} catch (NoSuchFieldException e) {
			assertThat(e.getMessage()).contains("unable to convert value");
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
		assertThat(cProperties).hasSize(objectProperties.size() + 4);
		cProperties.removeAll(objectProperties);
		assertThat(cProperties).containsExactlyInAnyOrder("foo", "bar", "updateNumberC", "updateNumber_privateC");
	}
	
	@Test
	public void testCollectAllMethodNames() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Collection<String> objectProperties = ClassUtils.collectMethodNames(Object.class, Object.class, MATCH_ANY);
		Collection<String> cProperties = ClassUtils.collectMethodNames(C.class, Object.class, MATCH_ANY);
		assertThat(objectProperties).isNotEmpty();
		assertThat(cProperties).hasSize(objectProperties.size() + 6);
		cProperties.removeAll(objectProperties);
		assertThat(cProperties).containsExactlyInAnyOrder("foo", "bar", "protectedMethod", "privateMethod", "updateNumberC", "updateNumber_privateC");
		MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "privateMethod");
	}
	
	@Test
	public void testCollectMethods() {
		List<Method> methodsOnC = ClassUtils.collectMethods(C.class, A.class, of(PUBLIC));
		// the second foo and bar occurrence come from the interface Foo that A implements (Foo extends Bar)
		assertThat(methodsOnC).extracting("name").containsExactlyInAnyOrder("foo", "foo", "bar", "bar", "updateNumberC", "updateNumber_privateC");
	}

	@Test
	public void testCollectMethodsByName() {
		assertThat(ClassUtils.collectMethodsByName(C.class, C.class, MATCH_ANY, "foo"))
				.extracting("name").containsExactlyInAnyOrder("foo");
		assertThat(ClassUtils.collectMethodsByName(C.class, Object.class, MATCH_ANY, "protectedMethod"))
				.extracting("name").containsExactlyInAnyOrder("protectedMethod", "protectedMethod", "protectedMethod");
		assertThat(ClassUtils.collectMethodsByName(C.class, C.class, MATCH_ANY, "nonexistantMethod")).isEmpty();
	}

	@Test
	public void testFindFirstMethodsByName() {
		assertThat(ClassUtils.findFirstMethodByName(C.class, C.class, MATCH_ANY, "foo"))
				.extracting("name").isEqualTo("foo");
		assertThat(ClassUtils.findFirstMethodByName(C.class, Object.class, MATCH_ANY, "protectedMethod"))
				.extracting("name").isEqualTo("protectedMethod");
	}

	@Test
	public void testHasMethodByName() {
		assertThat(ClassUtils.hasMethodByName(C.class, C.class, MATCH_ANY, "foo")).isTrue();
		assertThat(ClassUtils.hasMethodByName(C.class, Object.class, MATCH_ANY, "protectedMethod")).isTrue();
		assertThat(ClassUtils.hasMethodByName(C.class, Object.class, MATCH_ANY, "lalala")).isFalse();
	}
	
	@Test
	public void testCollectMethodsMappingToName() {
		Map<String, List<Method>> methodsByNames = ClassUtils.collectMethodsMappingToName(Moo.class, Shmoo.class, MATCH_ANY);
		
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