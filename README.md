[![APACHE v2 License](https://img.shields.io/badge/license-apachev2-blue.svg?style=flat)](LICENSE-2.0.txt) 
[![Latest Release](https://img.shields.io/maven-central/v/com.github.bbottema/java-reflection.svg?style=flat)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.bbottema%22%20AND%20a%3A%22java-reflection%22) 
[![Javadocs](http://www.javadoc.io/badge/com.github.bbottema/java-reflection.svg)](http://www.javadoc.io/doc/com.github.bbottema/java-reflection)
[![Codacy](https://img.shields.io/codacy/grade/d04a57e7f3184b47962e2666419683a1.svg?style=flat)](https://www.codacy.com/app/b-bottema/java-reflection)

# java-reflection
*java-reflection* is an advanced toolbox for finding compatible methods, constructors, annotations and converting values.

It defines utilities on Class level, Method level, Bean level, Package level, Type level and General utilities.

In additions, it provides an advanced conversion framework, that uses Dijkstra's graph to find the most efficient conversion route,
through multiple converters if a direct one is not available. It contains many built in converters and allows for easy extension.

```
<dependency>
  <groupId>com.github.bbottema</groupId>
  <artifactId>java-reflection</artifactId>
  <version>3.12.0</version>
</dependency>
```

v3.12.0 (4-November-2019)

- For simple class lookups, In addition to java.lang, also try the packages java.util and java.math


v3.11.1 - v3.11.3 (29-October-2019 - 1-November-2019)

- 3.11.3: Solved an NullPointerException in a collectMethods method
- 3.11.2: recompiled so the line numbers are correct with the released sources (due to license boilerplate added at the wrong time)
- 3.11.1: ClassUtils.collectMethods now returns methods define on implemented interfaces as well


v3.11.0 (27-October-2019)

- Added support for UUID conversion
- The Jakarta Activation framework is now an explicit dependency


v3.10.1 (21-October-2019)

- Made locateClass a little bit more user friendly by deferring return type T


v3.10.0 (28-September-2019)

- Added methods for finding parameters by annotations type
- Moved to Intellij @Nullability annotations
- Added license boilerplate code in Maven build script
- Solved a bunch of static anlayses warnings
- Improved how compatibility work when passing null-arguments as argument lists for locating constructors/methods


v3.9.5 (3-June-2019)

- Added method to check if a class has a method with a given name


v3.9.4 (27-May-2019)

- Allow null-values when invoking setter


v3.9.3 (10-May-2019)

- Added API for checking method compatibility based on actual arguments rather than by types only
- Added helper method to zip method parameters with their respective actual arguments
- Made the LookupMode arguments of type Set rather than EnumSet, so they can be made unmodifiable
- Added convenience method for returning the verify and return the only method in a findMethods result


v3.9.2 (30-April-2019)

- Added API to easily invoke bean setters / getters or methods


v3.9.1 (27-April-2019)

- Added convencience method to return the first method for a specified name
- Added method that returns an annotation of a specified type from a list
- Added method that returns an annotation of a specified type from an array
- Fixed visibility modifier on a public API


v3.9.0 (18-Januari-2019)

- Added support for bean-like methods defined on interfaces


v3.8.1 - v3.8.5 (13-October-2018 - 8-Januari-2019)

- Added support for same-type converters
- Performance update: implemented cache for generateComptibleSignatureLists
- Fixed Incompatible type exception if a number is outside of Byte or Short range
- Incompatible type exceptions are now gathered in case a conversion ultimately failed


v3.8.0 (9-October-2018)

- Added File based converters to InputStream, DataSource and byte[]
- Fixed problem with conversion candidates failing during actual conversions, so now we try all candidates rather than just the first one
- Fixed nullability analysis issue


v3.7.0 (6-October-2018)

- Added API for find declared Generic types in inheritance chains


v3.6.0 (4-October-2018)

- Added API for resolving field values
- Fixed API for searching fields. Now fields of any visibility can be resolved


v3.5.1 (1-October-2018)

- Added overloaded version of MethodUtils.findMatchingMethods(..) that also supports Collection in addition to varargs...
- Fixed name based type matching to properly work with arrays vs varargs...


v3.5.0 (30-September-2018)

- Made method collection facilities in ClassUtil much more robust by allowing any combination of method modifiers to find methods for rather than just
 a boolean "publicOnly". This includes modifiers other than for visibility as well.


v3.4.0 (26-September-2018)

- Added BeanUtils API to verify if a given Method is a bean setter / getter


v3.3.0 (26-September-2018)

- More robust class location facility
- Support any custom classloader for locating classes


v3.2.1 (24-September-2018)

- Optimized recursive code and implemented some caches


v3.2.0 (21-September-2018)

- Added alternative lookup method for Methods based on type names rather than types
- Streamlines ClassUtils API a bit


v3.1.0 (21-September-2018)

Complete overhaul:
- Conversion now works with graph-based path finding resolution to find all possible conversion paths
- Converters can now be added by third parties
- fixed a bug with the cache not working properly
- restructured classes and packages so it makes a lot more sense


v2.x.x (28-August-2018)

- Converted to Java 7 and added spotbugs
- Resolved a bunch of warnings
- Removed dependencies on external libraries


v1.0 (13-August-2011)

Initial upload