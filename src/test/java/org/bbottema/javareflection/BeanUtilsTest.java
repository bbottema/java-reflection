package org.bbottema.javareflection;

import org.bbottema.javareflection.BeanUtils.BeanRestriction;
import org.bbottema.javareflection.BeanUtils.Visibility;
import org.bbottema.javareflection.model.FieldWrapper;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static java.util.EnumSet.allOf;
import static java.util.EnumSet.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SuppressWarnings("javadoc")
public class BeanUtilsTest {
	
	@Test
	public void testCollectFieldsInheritanceAndOnlyGetters() {
		final Map<Class<?>, List<FieldWrapper>> fields = BeanUtils.collectFields(FieldsTestClass.class, FieldsTestClassGrandparent.class,
				of(Visibility.PROTECTED), of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER));
		assertThat(fields.keySet()).containsExactlyInAnyOrder(
				FieldsTestClass.class, FieldsTestClassParent.class, FieldsTestClassGrandparent.class);
		
		List<FieldWrapper> fieldWrappers = fields.get(FieldsTestClass.class);
		assertThat(fieldWrappers).hasSize(1);
		assertThat(fieldWrappers.get(0).getField().getName()).isEqualTo("field5");
		assertThat(fieldWrappers.get(0).getSetter()).isNull();
		assertThat(fieldWrappers.get(0).getGetter()).isNotNull();
		fieldWrappers = fields.get(FieldsTestClassParent.class);
		assertThat(fieldWrappers).hasSize(2);
		assertThat(fieldWrappers.get(0).getField().getName()).isEqualTo("field4");
		assertThat(fieldWrappers.get(0).getSetter()).isNull();
		assertThat(fieldWrappers.get(0).getGetter()).isNotNull();
		assertThat(fieldWrappers.get(1).getField().getName()).isEqualTo("fieldA");
		assertThat(fieldWrappers.get(1).getSetter()).isNull();
		assertThat(fieldWrappers.get(1).getGetter()).isNotNull();
		fieldWrappers = fields.get(FieldsTestClassGrandparent.class);
		assertThat(fieldWrappers).hasSize(2);
		assertThat(fieldWrappers.get(0).getField().getName()).isEqualTo("field1");
		assertThat(fieldWrappers.get(0).getSetter()).isNull();
		assertThat(fieldWrappers.get(0).getGetter()).isNotNull();
		assertThat(fieldWrappers.get(1).getField().getName()).isEqualTo("fieldA");
		assertThat(fieldWrappers.get(1).getSetter()).isNull();
		assertThat(fieldWrappers.get(1).getGetter()).isNotNull();
	}
	
	@Test
	public void testIsBeanMethodVariousScenarios() throws NoSuchMethodException {
		Method notABeanSetter = FieldsTestClass.class.getDeclaredMethod("notABeanSetter");
		assertThat(BeanUtils.isBeanMethod(notABeanSetter, FieldsTestClassGrandparent.class, allOf(Visibility.class))).isFalse();
		assertThat(BeanUtils.isBeanMethod(notABeanSetter, FieldsTestClassGrandparent.class, of(Visibility.PROTECTED))).isFalse();
		assertThat(BeanUtils.isBeanMethod(notABeanSetter, FieldsTestClassGrandparent.class, of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.isBeanMethod(notABeanSetter, FieldsTestClassGrandparent.class, of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.isBeanMethod(notABeanSetter, FieldsTestClassGrandparent.class, of(Visibility.PUBLIC))).isFalse();
		
		Method notABeanGetter = FieldsTestClass.class.getDeclaredMethod("notABeanGetter");
		assertThat(BeanUtils.isBeanMethod(notABeanGetter, FieldsTestClassGrandparent.class, allOf(Visibility.class))).isFalse();
		assertThat(BeanUtils.isBeanMethod(notABeanGetter, FieldsTestClassGrandparent.class, of(Visibility.PROTECTED))).isFalse();
		assertThat(BeanUtils.isBeanMethod(notABeanGetter, FieldsTestClassGrandparent.class, of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.isBeanMethod(notABeanGetter, FieldsTestClassGrandparent.class, of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.isBeanMethod(notABeanGetter, FieldsTestClassGrandparent.class, of(Visibility.PUBLIC))).isFalse();
		
		Method getField5 = FieldsTestClass.class.getDeclaredMethod("getField5");
		assertThat(BeanUtils.isBeanMethod(getField5, FieldsTestClassGrandparent.class, allOf(Visibility.class))).isTrue();
		assertThat(BeanUtils.isBeanMethod(getField5, FieldsTestClassGrandparent.class, of(Visibility.PROTECTED))).isTrue();
		assertThat(BeanUtils.isBeanMethod(getField5, FieldsTestClassGrandparent.class, of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.isBeanMethod(getField5, FieldsTestClassGrandparent.class, of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.isBeanMethod(getField5, FieldsTestClassGrandparent.class, of(Visibility.PUBLIC))).isFalse();
		
		Method setField2 = FieldsTestClassGrandparent.class.getDeclaredMethod("setField2", Object.class);
		assertThat(BeanUtils.isBeanMethod(setField2, FieldsTestClassGrandparent.class, allOf(Visibility.class))).isTrue();
		assertThat(BeanUtils.isBeanMethod(setField2, FieldsTestClassGrandparent.class, of(Visibility.PROTECTED))).isTrue();
		assertThat(BeanUtils.isBeanMethod(setField2, FieldsTestClassGrandparent.class, of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.isBeanMethod(setField2, FieldsTestClassGrandparent.class, of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.isBeanMethod(setField2, FieldsTestClassGrandparent.class, of(Visibility.PUBLIC))).isFalse();
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	public class FieldsTestClassGrandparent {
		protected Object field1; // yes
		protected Object field2; // no, has setter
		protected Object fieldA; // yes

		public Object getField1() {
			return field1;
		}

		public Object getField2() {
			return field2;
		}

		public void setField2(final Object field2) {
			this.field2 = field2;
		}

		public Object getFieldA() {
			return fieldA;
		}
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	public class FieldsTestClassParent extends FieldsTestClassGrandparent {
		protected Object field2; // no, parent has setter
		protected Object field3; // no, no getter
		protected Object field4; // yes
		protected Object fieldA; // yes

		@Override
		public Object getField2() {
			return field2;
		}

		public Object getField4() {
			return field4;
		}

		@Override
		public Object getFieldA() {
			return fieldA;
		}
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	public class FieldsTestClass extends FieldsTestClassParent {
		protected Object field5; // yes
		public Object field6; // no, not protected
		
		public Object getField5() {
			return field5;
		}
		
		public void notABeanSetter() { }
		public String notABeanGetter() { return "moo"; }
	}

	@Test
	public void testCollectFieldsSimpleButOnlySetter() {
		final Map<Class<?>, List<FieldWrapper>> fields = BeanUtils.collectFields(FieldsTestClassOnlySetter.class,
				FieldsTestClassOnlySetter.class, allOf(Visibility.class), of(BeanRestriction.YES_SETTER));
		assertThat(fields.keySet()).containsExactly(FieldsTestClassOnlySetter.class);
		final List<FieldWrapper> fieldWrappers = fields.get(FieldsTestClassOnlySetter.class);
		assertThat(fieldWrappers).hasSize(1);
		assertThat(fieldWrappers.get(0).getField().getName()).isEqualTo("field1");
		assertThat(fieldWrappers.get(0).getGetter()).as("field1").isNull();
		assertThat(fieldWrappers.get(0).getSetter()).as("field1").isNotNull();
	}
	
	@SuppressWarnings({"unused", "WeakerAccess"})
	public class FieldsTestClassOnlySetter {
		protected Object field1; // yes

		public void setField1(final Object field1) {
			this.field1 = field1;
		}
	}

	@Test
	public void testMeetVisibilityRequirements()
			throws SecurityException, NoSuchFieldException {
		Field field = FieldModifiers.class.getDeclaredField("_private");
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PRIVATE))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PROTECTED))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PRIVATE, Visibility.PUBLIC))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PROTECTED, Visibility.DEFAULT))).isFalse();

		field = FieldModifiers.class.getDeclaredField("_protected");
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PROTECTED))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PRIVATE, Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PROTECTED, Visibility.DEFAULT))).isTrue();

		field = FieldModifiers.class.getDeclaredField("_public");
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PUBLIC))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PROTECTED))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PRIVATE, Visibility.PUBLIC))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PROTECTED, Visibility.DEFAULT))).isFalse();

		field = FieldModifiers.class.getDeclaredField("_default");
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.DEFAULT))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PROTECTED))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PRIVATE, Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, of(Visibility.PROTECTED, Visibility.DEFAULT))).isTrue();
	}
	
	@SuppressWarnings({"unused", "WeakerAccess"})
	private static class FieldModifiers {
		Object _default;
		private Object _private;
		protected Object _protected;
		public Object _public;
	}

	@Test
	public void testResolveBeanProperty()
			throws SecurityException, NoSuchFieldException {
		Field field = BeanFields.class.getDeclaredField("withGetter");
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_SETTER)), true, false);
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER)), true, false);
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER)),
				true, false);
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER))).isNull();

		field = BeanFields.class.getDeclaredField("withGetterAndSetter");
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER)), true, true);
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_SETTER)), true, true);
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER)),
				true, true);

		field = BeanFields.class.getDeclaredField("withSetter");
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER)), false, true);
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_SETTER)), false, true);
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER)),
				false, true);
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER))).isNull();

		field = BeanFields.class.getDeclaredField("withNone");
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER)), false, false);
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_SETTER)), false, false);
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER)),
				false, false);
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER))).isNull();
	}
	
	@Test
	public void testResolvePrimitiveBooleanProperty()
			throws SecurityException, NoSuchFieldException {
		final Field field = BeanFields.class.getDeclaredField("primitiveBoolean");
		final FieldWrapper resolvedBeanProperty = BeanUtils.resolveBeanProperty(field, EnumSet.noneOf(BeanRestriction.class));
		assertNotNullProperty(resolvedBeanProperty, true, true);
	}

	private void assertNotNullProperty(final FieldWrapper resolvedBeanProperty, final boolean hasGetter, final boolean hasSetter) {
		assertThat(resolvedBeanProperty).isNotNull();
		assertThat(resolvedBeanProperty.getGetter() != null).isEqualTo(hasGetter);
		assertThat(resolvedBeanProperty.getSetter() != null).isEqualTo(hasSetter);
	}

	@Test
	public void testResolveBeanPropertyExceptions()
			throws SecurityException, NoSuchFieldException {
		Field field = BeanFields.class.getDeclaredField("withGetter");
		try {
			BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		field = BeanFields.class.getDeclaredField("withGetterAndSetter");
		try {
			BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		field = BeanFields.class.getDeclaredField("withSetter");
		try {
			BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		field = BeanFields.class.getDeclaredField("withNone");
		try {
			BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			BeanUtils.resolveBeanProperty(field, of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
	}

	@Test
	public void testInvokeBeanSetter_SimpleSuccess() {
		BeanFields subject = new BeanFields();

		assertThat(BeanUtils.invokeBeanSetter(subject, "withSetter", null)).isEqualTo(null);
		assertThat(BeanUtils.invokeBeanSetter(subject, "withSetter", 123)).isEqualTo(123);
		assertThat(BeanUtils.invokeBeanSetter(subject, "withGetterAndSetter", true)).isEqualTo(true);
		assertThat(BeanUtils.invokeBeanSetter(subject, "primitiveBoolean", true)).isEqualTo(true);

		assertThat(subject.withSetter).isEqualTo(123);
		assertThat(subject.getWithGetterAndSetter()).isEqualTo(true);
		assertThat(subject.isPrimitiveBoolean()).isTrue();
	}

	@Test
	public void testInvokeBeanSetter_WithConversionSuccess() {
		BeanFields subject = new BeanFields();

		assertThat(BeanUtils.invokeBeanSetter(subject, "primitiveBoolean", "true")).isEqualTo(true);
		assertThat(subject.isPrimitiveBoolean()).isTrue();
		assertThat(BeanUtils.invokeBeanSetter(subject, "primitiveBoolean", "false")).isEqualTo(false);
		assertThat(subject.isPrimitiveBoolean()).isFalse();
	}

	@Test
	public void testInvokeBeanSetter_NoSetterForField() {
		BeanFields subject = new BeanFields();

		try {
			BeanUtils.invokeBeanSetter(subject, "withGetter", 123);
			fail("expected exception");
		} catch (RuntimeException e) {
			assertThat(e.getCause()).isInstanceOf(NoSuchMethodException.class);
			assertThat(e.getCause().getMessage()).isEqualTo("Bean setter for withGetter");
		}
	}

	@Test
	public void testInvokeBeanSetter_NoSetterForFieldWithCorrectType() {
		BeanFields subject = new BeanFields();

		try {
			BeanUtils.invokeBeanSetter(subject, "primitiveBoolean", new Thread());
			fail("expected exception");
		} catch (RuntimeException e) {
			assertThat(e.getCause()).isInstanceOf(NoSuchMethodException.class);
			assertThat(e.getCause().getMessage()).contains("error: unable to convert value");
		}

		try {
			BeanUtils.invokeBeanSetter(subject, "primitiveBoolean", null);
			fail("expected exception");
		} catch (RuntimeException e) {
			assertThat(e).isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void testInvokeBeanGetter_SimpleSuccess() {
		BeanFields subject = new BeanFields();
		subject.setWithGetterAndSetter(true);
		subject.setPrimitiveBoolean(true);

		assertThat(BeanUtils.invokeBeanGetter(subject, "withGetterAndSetter")).isEqualTo(true);
		assertThat(BeanUtils.invokeBeanGetter(subject, "primitiveBoolean")).isEqualTo(true);
	}

	@Test
	public void testInvokeBeanGetter_NoGetterForField() {
		BeanFields subject = new BeanFields();
		subject.setWithSetter(true);

		try {
			BeanUtils.invokeBeanGetter(subject, "withSetter");
			fail("expected exception");
		} catch (RuntimeException e) {
			assertThat(e.getCause()).isInstanceOf(NoSuchMethodException.class);
			assertThat(e.getCause().getMessage()).contains("Bean getter for withSetter");
		}
	}
	
	@SuppressWarnings("unused")
	private static class BeanFields {
		private Object withGetter;
		private Object withGetterAndSetter;
		private Object withSetter;
		private Object withNone;
		private boolean primitiveBoolean;

		public Object getWithGetterAndSetter() {
			return withGetterAndSetter;
		}

		public void setWithGetterAndSetter(final Object withGetterAndSetter) {
			this.withGetterAndSetter = withGetterAndSetter;
		}

		public Object getWithGetter() {
			return withGetter;
		}

		public void setWithSetter(@Nullable final Object withSetter) {
			this.withSetter = withSetter;
		}

		public boolean isPrimitiveBoolean() {
			return primitiveBoolean;
		}

		public void setPrimitiveBoolean(final boolean primitiveBoolean) {
			this.primitiveBoolean = primitiveBoolean;
		}
	}
	
	@Test
	public void testMethodIsBeanlike() {
		for (Method method : ValidBeanlikeMethods.class.getMethods()) {
			assertThat(BeanUtils.methodIsBeanlike(method)).describedAs("method IS a beanlike: %s", method).isTrue();
		}
		for (Method method : InvalidBeanlikeMethods.class.getMethods()) {
			assertThat(BeanUtils.methodIsBeanlike(method)).describedAs("method IS NOT beanlike: %s", method).isFalse();
		}
	}
	
	@SuppressWarnings("unused")
	public interface ValidBeanlikeMethods {
		// valid beanlike setters
		void setBooleanPrimitive(boolean b);
		void setBooleanBoxed(Boolean b);
		void setObject(Object b);
		void setInteger(Integer b);
		// valid beanlike getters
		boolean isBooleanPrimitive();
		Boolean getBooleanBoxed();
		Object getObject();
		Integer getInteger();
	}
	
	@SuppressWarnings("unused")
	public interface InvalidBeanlikeMethods {
		// invalid beanlike setters
		boolean setBooleanPrimitive(boolean b);
		Object setBooleanBoxed(Boolean b);
		void setBooleanBoxed(Boolean b, Boolean b2);
		void setObject();
		// invalid beanlike getters
		Boolean isBooleanPrimitive();
		Boolean getBooleanBoxed(boolean b);
		void getObject();
	}
}