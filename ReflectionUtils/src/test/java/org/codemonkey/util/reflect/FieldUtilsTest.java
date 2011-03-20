package org.codemonkey.util.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.codemonkey.util.reflect.FieldUtils.BeanRestriction;
import org.codemonkey.util.reflect.FieldUtils.Visibility;
import org.junit.Test;

/**
 * @author Benny Bottema
 */
public class FieldUtilsTest {

	/**
	 * Test method for {@link org.codemonkey.util.reflect.JReflect#meetsVisibilityRequirements(java.lang.reflect.Field, java.util.EnumSet)}.
	 */
	@Test
	public void testCollectFieldsInheritanceAndOnlyGetters() {
		final Map<Class<?>, List<FieldWrapper>> fields = FieldUtils.collectFields(FieldsTestClass.class, FieldsTestClassGrandparent.class,
				EnumSet.of(Visibility.PROTECTED), EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER));
		assertEquals(3, fields.size());
		assertTrue(fields.keySet().contains(FieldsTestClass.class));
		assertTrue(fields.keySet().contains(FieldsTestClassParent.class));
		assertTrue(fields.keySet().contains(FieldsTestClassGrandparent.class));

		List<FieldWrapper> fieldWrappers = fields.get(FieldsTestClass.class);
		assertEquals(1, fieldWrappers.size());
		assertEquals("field5", fieldWrappers.get(0).getField().getName());
		assertNull(fieldWrappers.get(0).getSetter());
		assertNotNull(fieldWrappers.get(0).getGetter());
		fieldWrappers = fields.get(FieldsTestClassParent.class);
		assertEquals(2, fieldWrappers.size());
		assertEquals("field4", fieldWrappers.get(0).getField().getName());
		assertNull(fieldWrappers.get(0).getSetter());
		assertNotNull(fieldWrappers.get(0).getGetter());
		assertEquals("fieldA", fieldWrappers.get(1).getField().getName());
		assertNull(fieldWrappers.get(1).getSetter());
		assertNotNull(fieldWrappers.get(1).getGetter());
		fieldWrappers = fields.get(FieldsTestClassGrandparent.class);
		assertEquals(2, fieldWrappers.size());
		assertEquals("field1", fieldWrappers.get(0).getField().getName());
		assertNull(fieldWrappers.get(0).getSetter());
		assertNotNull(fieldWrappers.get(0).getGetter());
		assertEquals("fieldA", fieldWrappers.get(1).getField().getName());
		assertNull(fieldWrappers.get(1).getSetter());
		assertNotNull(fieldWrappers.get(1).getGetter());
	}

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

	public class FieldsTestClass extends FieldsTestClassParent {
		protected Object field5; // yes
		public Object field6; // no, not protected

		public Object getField5() {
			return field5;
		}
	}

	/**
	 * Test method for {@link org.codemonkey.util.reflect.JReflect#meetsVisibilityRequirements(java.lang.reflect.Field, java.util.EnumSet)}.
	 */
	@Test
	public void testCollectFieldsSimplButOnlySetter() {
		final Map<Class<?>, List<FieldWrapper>> fields = FieldUtils.collectFields(FieldsTestClassOnlySetter.class,
				FieldsTestClassOnlySetter.class, EnumSet.allOf(Visibility.class), EnumSet.of(BeanRestriction.YES_SETTER));
		assertEquals(1, fields.size());
		assertTrue(fields.keySet().contains(FieldsTestClassOnlySetter.class));
		final List<FieldWrapper> fieldWrappers = fields.get(FieldsTestClassOnlySetter.class);
		assertEquals(1, fieldWrappers.size());
		assertEquals("field1", fieldWrappers.get(0).getField().getName());
		assertNull("field1", fieldWrappers.get(0).getGetter());
		assertNotNull("field1", fieldWrappers.get(0).getSetter());
	}

	public class FieldsTestClassOnlySetter {
		protected Object field1; // yes

		public void setField1(final Object field1) {
			this.field1 = field1;
		}
	}

	/**
	 * Test method for {@link org.codemonkey.util.reflect.JReflect#meetsVisibilityRequirements(java.lang.reflect.Field, java.util.EnumSet)}.
	 */
	@Test
	public void testMeetVisibilityRequirements()
			throws SecurityException, NoSuchFieldException {
		Field field = FieldModifiers.class.getDeclaredField("_private");
		assertTrue(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PUBLIC)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.DEFAULT)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED)));
		assertTrue(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE, Visibility.PUBLIC)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED, Visibility.DEFAULT)));

		field = FieldModifiers.class.getDeclaredField("_protected");
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PUBLIC)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.DEFAULT)));
		assertTrue(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE, Visibility.PUBLIC)));
		assertTrue(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED, Visibility.DEFAULT)));

		field = FieldModifiers.class.getDeclaredField("_public");
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE)));
		assertTrue(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PUBLIC)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.DEFAULT)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED)));
		assertTrue(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE, Visibility.PUBLIC)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED, Visibility.DEFAULT)));

		field = FieldModifiers.class.getDeclaredField("_default");
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PUBLIC)));
		assertTrue(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.DEFAULT)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED)));
		assertFalse(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PRIVATE, Visibility.PUBLIC)));
		assertTrue(FieldUtils.meetsVisibilityRequirements(field, EnumSet.of(Visibility.PROTECTED, Visibility.DEFAULT)));
	}

	@SuppressWarnings("unused")
	private static class FieldModifiers {
		Object _default;
		private Object _private;
		protected Object _protected;
		public Object _public;
	}

	/**
	 * Test method for {@link org.codemonkey.util.reflect.FieldUtils#resolveBeanProperty(Field, EnumSet)}.
	 */
	@Test
	public void testResolveBeanProperty()
			throws SecurityException, NoSuchFieldException {
		Field field = BeanFields.class.getDeclaredField("withGetter");
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER)));
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER)), true, false);
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER)), true, false);
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_SETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER)));
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER)),
				true, false);
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER)));

		field = BeanFields.class.getDeclaredField("withGetterAndSetter");
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER)));
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER)), true, true);
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_SETTER)), true, true);
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER)));
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER)),
				true, true);

		field = BeanFields.class.getDeclaredField("withSetter");
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER)), false, true);
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER)));
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_SETTER)), false, true);
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER)));
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER)),
				false, true);
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER)));

		field = BeanFields.class.getDeclaredField("withNone");
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER)), false, false);
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER)), false, false);
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_SETTER)));
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.NO_SETTER)),
				false, false);
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.NO_SETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_SETTER)));
		assertNull(FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.YES_GETTER, BeanRestriction.YES_SETTER)));
	}

	/**
	 * Test method for {@link org.codemonkey.util.reflect.FieldUtils#resolveBeanProperty(Field, EnumSet)}.
	 */
	@Test
	public void testResolvePrimitiveBooleanProperty()
			throws SecurityException, NoSuchFieldException {
		final Field field = BeanFields.class.getDeclaredField("primitiveBoolean");
		assertNotNullProperty(FieldUtils.resolveBeanProperty(field, EnumSet.noneOf(BeanRestriction.class)), true, true);
	}

	private void assertNotNullProperty(final FieldWrapper resolvedBeanProperty, final boolean hasGetter, final boolean hasSetter) {
		assertNotNull(resolvedBeanProperty);
		assertEquals(hasGetter, resolvedBeanProperty.getGetter() != null);
		assertEquals(hasSetter, resolvedBeanProperty.getSetter() != null);
	}

	/**
	 * Test method for
	 * {@link org.codemonkey.util.reflect.FieldUtils#meetsBeanRestrictions(Field, org.codemonkey.util.reflect.FieldUtils.BeanRestriction)}.
	 */
	@Test
	public void testResolveBeanPropertyExceptions()
			throws SecurityException, NoSuchFieldException {
		Field field = BeanFields.class.getDeclaredField("withGetter");
		try {
			FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		field = BeanFields.class.getDeclaredField("withGetterAndSetter");
		try {
			FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		field = BeanFields.class.getDeclaredField("withSetter");
		try {
			FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		field = BeanFields.class.getDeclaredField("withNone");
		try {
			FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_GETTER, BeanRestriction.YES_GETTER));
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			// ok
		}
		try {
			FieldUtils.resolveBeanProperty(field, EnumSet.of(BeanRestriction.NO_SETTER, BeanRestriction.YES_SETTER));
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
	};
}