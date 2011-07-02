package org.codemonkey.javareflection;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

/**
 * A toolkit that can read and compile .java sourcefiles on the fly in runtime. This <code>class loader</code> caches loaded classes for
 * repeated requests. Also, if a .class file already exists for a specific .java sourcefile, the classloader will compare dates and see if
 * the .java needs recompiling. <br />
 * <br />
 * A basepath can be specified for the classloader to look in for sourcefiles.
 */
public final class ExternalClassLoader extends ClassLoader {
	/**
	 * base path used to locate [packaged] classes in
	 */
	private String basepath;

	/**
	 * List of classes. These are non-instances of a class, of which instances can be spawn.
	 */
	protected final Map<String, Class<?>> classes;

	/**
	 * The exception that was thrown when the class was actually found but some other error occurred.
	 */
	protected CompileException exception;

	/**
	 * Constructor which initializes all properties.
	 */
	public ExternalClassLoader() {
		super(ExternalClassLoader.class.getClassLoader());
		classes = new HashMap<String, Class<?>>();
	}

	/**
	 * Loads a class from the classes cache if available. Delegates to {@link #findClass(String)} otherwise.
	 * 
	 * @see ClassLoader#loadClass(String)
	 */
	@Override
	public final Class<?> loadClass(final String className)
			throws ClassNotFoundException {
		final Class<?> c = classes.get(className);
		return (c != null) ? c : findClass(className);
	}

	/**
	 * Loads a classfile from file in the following order:
	 * <ol>
	 * <li>looks for the java source and if available, checks whether it needs to be compiled</li>
	 * <li>if no .java or .class file found, try to find it in the VM</li>
	 * </ol>
	 * If the class ultimately wasn't found a <code>ClassNotFoundException</code> is being thrown. If class was found, but an error
	 * occurred, store exception for later review and return <code>null</code>. This is done so because the overridden method can't throw an
	 * exception other than <code>ClassNotFoundException</code>.
	 * 
	 * @param className The path and name to the classfile.
	 * @return The requested class reference.
	 * @throws ClassNotFoundException
	 */
	@Override
	public final Class<?> findClass(final String className)
			throws ClassNotFoundException {
		exception = null;
		try {
			// try to load, cache and return the class from compiled .class file
			checkForFile(basepath, className);

			// if a classfile is available then load it
			final Class<?> c = classes.get(className);
			if (c != null) {
				return c;
			} else {
				throw new ClassNotFoundException();
			}
		} catch (final ClassNotFoundException e) {
			// - see if class can be found in the VM
			// - this check must come last, else you won't detect whether a classfile has been recompiled
			final Class<?> c = findSystemClass(className);
			classes.put(className, c);
			return c;
		} catch (final IOException e) {
			exception = new CompileException(e.getMessage(), e);
			return null;
		} catch (final CompileException e) {
			exception = e;
			return null;
		}
	}

	/**
	 * Reads and compiles the sourcefile from filesystem and creates the class inside the VM.
	 * 
	 * @param className The path and name to the .java sourcefile.
	 * @throws CompileException
	 * @throws IOException
	 */
	private void checkForFile(final String classPath, final String className)
			throws CompileException, IOException {
		// figure paths...
		final String resource = className.replace("..", "||").replace('.', File.separatorChar).replace("||", "..");
		final File javaSource = new File(classPath + File.separatorChar + resource + ".java");
		final File javaClass = new File(classPath + File.separatorChar + resource + ".class");

		new File(basepath).getAbsolutePath();
		javaSource.getAbsolutePath();
		final String absoluteClassPath = javaClass.getAbsolutePath();

		// see if there is a javafile and classfile
		if (javaSource.exists()) {
			// if classfile available, delete if outdated
			if (javaClass.exists()) {
				// determine if the java sourcefile has been modified since last compile
				// else check if the .class file has already been loaded
				if (javaSource.lastModified() > javaClass.lastModified()) {
					if (!javaClass.delete()) {
						throw new CompileException("runtime compiler: unable to removed outdated .class file");
					}
					classes.remove(className);
				} else if (classes.get(className) == null) {
					classes.put(className, loadClass(absoluteClassPath, className));
				}
			}

			// if no classfile available or became outdated
			if (!javaClass.exists()) {
				final int status = 0;// Main.compile(args);

				// load compiled classfile and cache it or return error that occured during compiling
				switch (status) {
				case 0:
					classes.put(className, loadClass(absoluteClassPath, className));
					break;
				case 1:
					throw new CompileException("runtime compiler: ERROR");
				case 2:
					throw new CompileException("runtime compiler: CMDERR");
				case 3:
					throw new CompileException("runtime compiler: SYSERR");
				case 4:
					throw new CompileException("runtime compiler: ABNORMAL");
				default:
					throw new CompileException("Compile status: Unknown exit status");
				}
			}
		}
	}

	/**
	 * Reads the classfile from filesystem.
	 * 
	 * @param classPath The path and name to the classfile.
	 * @param className The name of the class to use as key for the Class reference value.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private Class<?> loadClass(final String classPath, final String className)
			throws IOException {
		// get data from file
		final File f = new File(classPath);
		final int size = (int) f.length();
		final byte buff[] = new byte[size];
		final DataInputStream dis = new DataInputStream(new FileInputStream(f));
		dis.readFully(buff);
		dis.close();
		// convert data into class
		return defineClass(className, buff, 0, buff.length, null);
	}

	/**
	 * Parameterized exception used when Java's runtime compiler fails to compile a Java source file.
	 * 
	 * @author Benny Bottema
	 */
	public static class CompileException extends ScriptException {
		private static final long serialVersionUID = -7210219718456902667L;

		/**
		 * @param reason The description of the cause of the exception.
		 */
		public CompileException(final String reason) {
			super(reason);
		}

		/**
		 * Used to create an exception with a copies stacktrace (using {@link #setStackTrace(StackTraceElement[])}).
		 * 
		 * @param reason The description of the cause of the exception.
		 * @param cause A thrown exception that is the cause.
		 */
		public CompileException(final String reason, final Throwable cause) {
			super(reason + "\n	" + cause.toString());
			setStackTrace(cause.getStackTrace());
		}
	}

	/**
	 * Sets the base path this classloader will look for classes in.
	 * 
	 * @param basepath A folder path to look for classes.
	 */
	public void setBasepath(final String basepath) {
		this.basepath = basepath;
	}

	/**
	 * @return {@link #basepath}
	 */
	public String getBasepath() {
		return basepath;
	}
}