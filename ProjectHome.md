# Java Reflection #

**Java Reflection provides a small package with nifty reflection features that will help with finding constructors, methods and value conversions**.

The main reason this project is available as open source is because it is a dependency of the [Swift Socket Server](https://code.google.com/p/swift-socket-server/) project, yet merited a project of its own.

Main features:

  * an advanced utility class that finds and invokes methods/constructors based on a given list of values (optionally converting the values to the right type)
  * a conversion class that is able to convert common value type to a different type (`boolean` to `Character` for example)
  * an easy way to find an object's fields, optionally including setters/getters, restricted by a number of criteria (such as: should have a getter method, should be a `public` field etc.)
  * an (experimental, but tested) ClassLoader that is able to resort to manually compile .class files and load them on the fly

Extra features:

  * a method that returns the autoboxed version of a value
  * a convenience method that assign a value to an object's field directly
  * a method that determines what the smallest `Number` type is that can hold a list of given number values of various types without losing precision
  * various utility methods

