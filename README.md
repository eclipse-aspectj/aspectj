# AspectJ


## Building

AspectJ has a multi module maven build. Although various modules produce intermediate results, the key artifacts at the end of the build are:

* `aspectjrt` - the AspectJ runtime
* `aspectjweaver` - the AspectJ weaver
* `aspectjtools` - the AspectJ compiler

These are the artifacts published to maven central for each release. In addition there is an installer that can be run with `java -jar` to install AspectJ onto Windows/Mac/Linux. This installer is produced by the installer sub module.

In the root of a cloned AspectJ simply run:

`./mvnw clean install`

This will build all the modules, run all the tests and install the key artifacts in your local repository.

## Running the tests in eclipse

Once you have imported all the projects using `m2e`, there is a special module called `run-all-junit-tests` and within that a `RunTheseBeforeYouCommitTests` class that can be run with the JUnit launcher. This will execute all the tests in each module plus the compiler tests in the `tests` module.

 