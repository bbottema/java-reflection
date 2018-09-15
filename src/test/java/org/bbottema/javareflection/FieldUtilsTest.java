package org.bbottema.javareflection;

import org.bbottema.javareflection.BeanUtils.BeanRestriction;
import org.bbottema.javareflection.BeanUtils.Visibility;
import org.bbottema.javareflection.model.FieldWrapper;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Junit test for {@link BeanUtils}.
 */
@SuppressWarnings("javadoc")
public class FieldUtilsTest {

	/**
	 * Test method for {@link BeanUtils#collectFields(Class, Class, EnumSet, EnumSet)}.
	 */
	@Test
	public void testCollectFieldsInheritanceAndOnlyGetters() {
		final Map<Class<?>, List<FieldWrapper>> fields = BeanUtils.collectFields(FieldsTestClass.class, FieldsTestClassGrandparent.class,
				EnumSet.of(Visibility.PROTECTED), EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER));
		assertThat(fields.size()).isEqualTo(3);
		assertThat(fields.keySet().contains(FieldsTestClass.class)).isTrue();
		assertThat(fields.keySet().contains(FieldsTestClassParent.class)).isTrue();
		assertThat(fields.keySet().contains(FieldsTestClassGrandparent.class)).isTrue();

		List<FieldWrapper> fieldWrappers = fields.get(FieldsTestClass.class);
		assertThat(fieldWrappers.size()).isEqualTo(1);
		assertThat(fieldWrappers.get(0).getField().getName()).isEqualTo("field5");
		assertThat(fieldWrappers.get(0).getSetter()).isNull();
		assertThat(fieldWrappers.get(0).getGetter()).isNotNull();
		fieldWrappers = fields.get(FieldsTestClassParent.class);
		assertThat(fieldWrappers.size()).isEqualTo(2);
		assertThat(fieldWrappers.get(0).getField().getName()).isEqualTo("field4");
		assertThat(fieldWrappers.get(0).getSetter()).isNull();
		assertThat(fieldWrappers.get(0).getGetter()).isNotNull();
		assertThat(fieldWrappers.get(1).getField().getName()).isEqualTo("fieldA");
		assertThat(fieldWrappers.get(1).getSetter()).isNull();
		assertThat(fieldWrappers.get(1).getGetter()).isNotNull();
		fieldWrappers = fields.get(FieldsTestClassGrandparent.class);
		assertThat(fieldWrappers.size()).isEqualTo(2);
		assertThat(fieldWrappers.get(0).getField().getName()).isEqualTo("field1");
		assertThat(fieldWrappers.get(0).getSetter()).isNull();
		assertThat(fieldWrappers.get(0).getGetter()).isNotNull();
		assertThat(fieldWrappers.get(1).getField().getName()).isEqualTo("fieldA");
		assertThat(fieldWrappers.get(1).getSetter()).isNull();
		assertThat(fieldWrappers.get(1).getGetter()).isNotNull();
	}

	/**
	 * Test subject with specific combinations of fields, visibilities and setters/getters availability.
	 */
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

	/**
	 * Test subject with specific combinations of fields, visibilities and setters/getters availability.
	 */
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

	/**
	 * Test subject with specific combinations of fields, visibilities and setters/getters availability.
	 */
	@SuppressWarnings({"unused", "WeakerAccess"})
	public class FieldsTestClass extends FieldsTestClassParent {
		protected Object field5; // yes
		public Object field6; // no, not protected

		public Object getField5() {
			return field5;
		}
	}

	/**
	 * Test method for {@link BeanUtils#collectFields(Class, Class, EnumSet, EnumSet)}.
	 */
	@Test
	public void testCollectFieldsSimplButOnlySetter() {
		final Map<Class<?>, List<FieldWrapper>> fields = BeanUtils.collectFields(FieldsTestClassOnlySetter.class,
				FieldsTestClassOnlySetter.class, EnumSet.allOf(Visibility.class), EnumSet.of(BeanRestriction.YES_SETTER));
		assertThat(fields.size()).isEqualTo(1);
		assertThat(fields.keySet().contains(FieldsTestClassOnlySetter.class)).isTrue();
		final List<FieldWrapper> fieldWrappers = fields.get(FieldsTestClassOnlySetter.class);
		assertThat(fieldWrappers.size()).isEqualTo(1);
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

	/**
	 * Test method for {@link BeanUtils#meetsVisibilityRequirements(Field, EnumSet)}.
	 * 
	 * @throws SecurityException Can be thrown by JDK, but won't since it is our own test class.
	 * @throws NoSuchFieldException Can be thrown by JDK, but won't since it is our own test class.
	 */
	@Test
	public void testMeetVisibilityRequirements()
			throws SecurityException, NoSuchFieldException {
		Field field = FieldModifiers.class.getDeclaredField("_private");
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE, Visibility.PUBLIC))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED, Visibility.DEFAULT))).isFalse();

		field = FieldModifiers.class.getDeclaredField("_protected");
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE, Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED, Visibility.DEFAULT))).isTrue();

		field = FieldModifiers.class.getDeclaredField("_public");
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PUBLIC))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.DEFAULT))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE, Visibility.PUBLIC))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED, Visibility.DEFAULT))).isFalse();

		field = FieldModifiers.class.getDeclaredField("_default");
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.DEFAULT))).isTrue();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE, Visibility.PUBLIC))).isFalse();
		assertThat(BeanUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED, Visibility.DEFAULT))).isTrue();
	}
	
	@SuppressWarnings({"unused", "WeakerAccess"})
	private static class FieldModifiers {
		Object _default;
		private Object _private;
		protected Object _protected;
		public Object _public;
	}

	/**
	 * Test method for {@link BeanUtils#resolveBeanProperty(Field, EnumSet)}.
	 * 
	 * @throws SecurityException Can be thrown by JDK, but won't since it is our own test class.
	 * @throws NoSuchFieldException Can be thrown by JDK, but won't since it is our own test class.
	 */
	@Test
	public void testResolveBeanProperty()
			throws SecurityException, NoSuchFieldException {
		Field field = BeanFields.class.getDeclaredField("withGetter");
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER)), true, false);
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER)), true, false);
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER)),
				true, false);
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER))).isNull();

		field = BeanFields.class.getDeclaredField("withGetterAndSetter");
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER)), true, true);
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_SETTER)), true, true);
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER)),
				true, true);

		field = BeanFields.class.getDeclaredField("withSetter");
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER)), false, true);
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_SETTER)), false, true);
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER)),
				false, true);
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER))).isNull();

		field = BeanFields.class.getDeclaredField("withNone");
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER)), false, false);
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER)), false, false);
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_SETTER))).isNull();
		assertNotNullProperty(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER)),
				false, false);
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER))).isNull();
		assertThat(BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER))).isNull();
	}
	/**
	 * Test method for {@link BeanUtils#resolveBeanProperty(Field, EnumSet)}.
	 * 
	 * @throws SecurityException Can be thrown by JDK, but won't since it is our own test class.
	 * @throws NoSuchFieldException Can be thrown by JDK, but won't since it is our own test class.
	 */
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

	/**
	 * Test method for {@link BeanUtils#resolveBeanProperty(Field, EnumSet)}.
	 * 
	 * @throws SecurityException Can be thrown by JDK, but won't since it is our own test class.
	 * @throws NoSuchFieldException Can be thrown by JDK, but won't since it is our own test class.
	 */
	@Test
	public void testResolveBeanPropertyExceptions()
			throws SecurityException, NoSuchFieldException {
		Field field = BeanFields.class.getDeclaredField("withGetter");
		try {
			BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		field = BeanFields.class.getDeclaredField("withGetterAndSetter");
		try {
			BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		field = BeanFields.class.getDeclaredField("withSetter");
		try {
			BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		field = BeanFields.class.getDeclaredField("withNone");
		try {
			BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			BeanUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
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

		public void setWithSetter(final Object withSetter) {
			this.withSetter = withSetter;
		}

		public boolean isPrimitiveBoolean() {
			return primitiveBoolean;
		}

		public void setPrimitiveBoolean(final boolean primitiveBoolean) {
			this.primitiveBoolean = primitiveBoolean;
		}
	}
}