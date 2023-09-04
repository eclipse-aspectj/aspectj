# AspectJ Java version compatibility

For reasons described e.g. in [this comment](https://github.com/eclipse/org.aspectj/issues/139#issuecomment-1072946123),
for AspectJ users it has become a little challenging to find out which minimum AspectJ version is required in order to
process byte code or compile source code using features of a certain Java language version. Since Java 10, this cannot
be easily concluded from the AspectJ version number anymore, and we are sorry for that. So here is a little overview:

AspectJ version | Java version | Comments
----------------|--------------|--------
1.9.20 - 1.9.20.1 | 20
1.9.19 | 19
1.9.9 - 1.9.9.1 | 18
1.9.8 | 17 | AspectJ compiler requires JDK 11+ during build time. During runtime, AspectJ still only requires Java 8+ for both compile-time and load-time weaving. Pure Java code can be compiled down to as old as 1.3 byte code level.
1.9.7 | 15, 16
1.9.6 | 14
1.9.5 | 13
1.9.3 - 1.9.4 | 12
1.9.2 | 11
1.9.1 | 10
1.9.0 | 9
1.8.0 - 1.8.14 | 8
1.7.0 - 1.7.4 | 7
1.6.0 - 1.6.12 | 6
1.5.0 - 1.5.4 | 5

Older versions omitted.

FYI, here is a brief overview of
[Java language changes since Java 9](https://docs.oracle.com/en/java/javase/18/language/java-language-changes.html).
They basically correspond to the new Java language features supported by AspectJ versions for the respective Java
versions.
