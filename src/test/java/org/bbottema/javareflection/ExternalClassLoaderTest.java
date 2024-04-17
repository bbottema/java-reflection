package org.bbottema.javareflection;

import org.bbottema.javareflection.util.ExternalClassLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalClassLoaderTest {
	
	@Test
	public void testConstructor() {
		new ExternalClassLoader();
		// ok, no exception
	}

	@Test
	public void testSettersGetters() {
		ExternalClassLoader loader = new ExternalClassLoader();
		assertThat(loader.getBasepath()).isNull();
		loader.setBasepath("test");
		assertThat(loader.getBasepath()).isEqualTo("test");
	}
}