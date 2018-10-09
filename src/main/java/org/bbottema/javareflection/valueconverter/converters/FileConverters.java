package org.bbottema.javareflection.valueconverter.converters;

import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.util.Function;
import org.bbottema.javareflection.util.Function.Functions;
import org.bbottema.javareflection.valueconverter.ValueFunction;
import org.bbottema.javareflection.valueconverter.ValueFunction.ValueFunctionImpl;
import org.jetbrains.annotations.Nullable;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

@Nullable
@UtilityClass
public final class FileConverters {
	
	public static final Collection<ValueFunction<File, ?>> FILE_CONVERTERS = produceFileConverters();
	
	private static Collection<ValueFunction<File, ?>> produceFileConverters() {
		ArrayList<ValueFunction<File, ?>> converters = new ArrayList<>();
		converters.add(new ValueFunctionImpl<>(File.class, File.class, Functions.<File>identity()));
		converters.add(new ValueFunctionImpl<>(File.class, DataSource.class, new FileToDataSourceFunction()));
		converters.add(new ValueFunctionImpl<>(File.class, byte[].class, new FileToByteArrayFunction()));
		converters.add(new ValueFunctionImpl<>(File.class, InputStream.class, new FileToInputStreamFunction()));
		return converters;
	}
	
	private static class FileToDataSourceFunction implements Function<File, DataSource> {
		@Override
		public DataSource apply(File file) {
			return new FileDataSource(file);
		}
	}
	
	private static class FileToByteArrayFunction implements Function<File, byte[]> {
		@Override
		public byte[] apply(File file) {
			try {
				return Files.readAllBytes(file.toPath());
			} catch (IOException e) {
				throw new RuntimeException("Was unable to read file content", e);
			}
		}
	}
	
	private static class FileToInputStreamFunction implements Function<File, InputStream> {
		@Override
		public InputStream apply(File file) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new AssertionError("File found, but also not found? Is this the real life...", e);
			}
		}
	}
}