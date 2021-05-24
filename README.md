# AspectJ

AspectJ is:
- a seamless aspect-oriented extension to the Java programming language
- Java platform compatible
- easy to learn and use

AspectJ enables:
- clean modularization of crosscutting concerns, such as error checking and handling, synchronization, context-sensitive behavior, performance optimizations, monitoring and logging, debugging support, and multi-object protocols

## Building

AspectJ has a multi module maven build. Although various modules produce intermediate results, the key artifacts at the end of the build are:

* `aspectjrt` - the AspectJ runtime
* `aspectjweaver` - the AspectJ weaver
* `aspectjtools` - the AspectJ compiler

These are the artifacts published to maven central for each release. In addition there is an installer that can be run with `java -jar` to install AspectJ onto Windows/Mac/Linux. This installer is produced by the installer sub module.

In the root of a cloned AspectJ simply run:

`./mvnw clean install`

This will build all the modules, run all the tests and install the key artifacts in your local repository.
Once built access the maven dependencies from your local repository or run the installer to install AspectJ locally:
```
java -jar installer/target/aspectj-<VERSION>.jar
```

## Running the tests in eclipse

Once you have imported all the projects using `m2e`, there is a special module called `run-all-junit-tests` and within that a `RunTheseBeforeYouCommitTests` class that can be run with the JUnit launcher to execute thousands of tests.
Ensure you run this with a JDK - the more recent the better since some tests will not execute on older JDKs - tests that verify language features that only exist in the more up to date JDK version.

## Documentation

[Complete documentation quicklinks](https://www.eclipse.org/aspectj/docs.php)

[Getting started with AspectJ](https://www.eclipse.org/aspectj/doc/released/progguide/starting.html)

[Programming Guide](https://www.eclipse.org/aspectj/doc/released/progguide/index.html)

[READMEs for each version of AspectJ](https://github.com/eclipse/org.aspectj/tree/master/docs/dist/doc)

## Maven releases

AspectJ is published to maven central under the `org.aspectj` groupID: https://search.maven.org/search?q=g:org.aspectj
