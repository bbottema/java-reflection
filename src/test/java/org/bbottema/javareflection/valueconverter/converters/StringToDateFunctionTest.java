package org.bbottema.javareflection.valueconverter.converters;

import org.junit.jupiter.api.Test;

import java.util.GregorianCalendar;

import static java.util.Calendar.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bbottema.javareflection.valueconverter.converters.StringConverters.StringToDateFunction;

public class StringToDateFunctionTest {
	
	@Test
	public void testStringToDateConversion() {
		final StringToDateFunction converter = new StringToDateFunction();
		assertThat(converter.apply("2011-5-4")).isEqualTo(new GregorianCalendar(2011, MAY, 4).getTime());
		assertThat(converter.apply("2011-5-14")).isEqualTo(new GregorianCalendar(2011, MAY, 14).getTime());
		assertThat(converter.apply("2011-05-4")).isEqualTo(new GregorianCalendar(2011, MAY, 4).getTime());
		assertThat(converter.apply("2011-05-14")).isEqualTo(new GregorianCalendar(2011, MAY, 14).getTime());
		assertThat(converter.apply("2011-5-4 05:10")).isEqualTo(new GregorianCalendar(2011, MAY, 4, 5, 10).getTime());
		assertThat(converter.apply("2011-5-14 05:10")).isEqualTo(new GregorianCalendar(2011, MAY, 14, 5, 10).getTime());
		assertThat(converter.apply("2011-05-4 05:10")).isEqualTo(new GregorianCalendar(2011, MAY, 4, 5, 10).getTime());
		assertThat(converter.apply("2011-05-14 05:10")).isEqualTo(new GregorianCalendar(2011, MAY, 14, 5, 10).getTime());
		assertThat(converter.apply("2011-5-4 5:4")).isEqualTo(new GregorianCalendar(2011, MAY, 4, 5, 4).getTime());
		assertThat(converter.apply("2011-5-14 5:4")).isEqualTo(new GregorianCalendar(2011, MAY, 14, 5, 4).getTime());
		assertThat(converter.apply("2011-05-4 5:4")).isEqualTo(new GregorianCalendar(2011, MAY, 4, 5, 4).getTime());
		assertThat(converter.apply("2011-05-14 5:4")).isEqualTo(new GregorianCalendar(2011, MAY, 14, 5, 4).getTime());
	}
}