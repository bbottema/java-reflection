package org.bbottema.javareflection;

import org.bbottema.javareflection.util.ExternalClassLoader;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Junit test for {@link ExternalClassLoader}.
 */
public class ExternalClassLoaderTest {
	/**
	 * Test for {@link ExternalClassLoader#ExternalClassLoader()}.
	 */
	@Test
	public void testConstructor() {
		new ExternalClassLoader();
		// ok, no exception
	}

	/**
	 * Test for ExternalClassLoader setters/getters.
	 */
	@Test
	public void testSettersGetters() {
		ExternalClassLoader loader = new ExternalClassLoader();
		assertThat(loader.getBasepath()).isNull();
		loader.setBasepath("test");
		assertThat(loader.getBasepath()).isEqualTo("test");
	}
}