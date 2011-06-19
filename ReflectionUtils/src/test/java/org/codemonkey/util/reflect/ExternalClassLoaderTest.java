package org.codemonkey.util.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ExternalClassLoaderTest {
	@Test
	public void testConstructor() {
		new ExternalClassLoader();
		// ok, no exception
	}

	@Test
	public void testSettersGetters() {
		ExternalClassLoader loader = new ExternalClassLoader();
		assertNull(loader.getBasepath());
		loader.setBasepath("test");
		assertEquals("test", loader.getBasepath());
	}
}