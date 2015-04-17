***Java Reflection***

Java Reflection provides a small package with nifty Java reflection methods.

Aside from a variety of utility functions, this library mainly aims to ease method/constructor lookups, using value conversions, such as autoboxing, auto casting, but also commons value conversions (such as Boolean to String and many more).

Normal JDK method matching through reflection is very strict on the exact argument class types, but with this Java Reflection Library, this is much easier and more robust (and performs better).

The main reason this project is available as open source is because it is a dependency of the Swift Socket Server project (https://github.com/bbottema/swift-socket-server), yet merited a project of its own.

**Documentation**

Because of the size and nature of this little project, about four utility classes with only `static` methods, I will refer to the JavaDoc for all documentation.

* *JReflect*
* *ValueConverter*
* *FieldUtils*
* *ExternalClassLoader*

***What's inside***

**Main features**

  * an advanced utility class that finds and invokes methods/constructors based on a given list of values (optionally converting the values to the right type)
  * a conversion class that is able to convert common value type to a different type (`boolean` to `Character` for example)
  * an easy way to find an object's fields, optionally including setters/getters, restricted by a number of criteria (such as: should have a getter method, should be a `public` field etc.)
  * an (experimental, but tested) ClassLoader that is able to resort to manually compile .class files and load them on the fly

**Extra features**

  * a method that returns the autoboxed version of a value
  * a convenience method that assign a value to an object's field directly
  * a method that determines what the smallest `Number` type is that can hold a list of given number values of various types without losing precision
  * various utility methods

---

Here are some basic instructions to build and compile or run junit tests.

With Ant (default target is 'test'):

ant clean:
- deletes the entire target folder

ant compile:
- compiles all classes to the target folder

ant jar:
- creates a library jar of the framework
