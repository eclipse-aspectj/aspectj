
The AspectJ tree builds using a variant of Ant 1.5.1.

lib/ contains Ant 1.5.1 core and optional taskdef classes,
as well as xalan.jar which is used for the junitreport task.

To run tests with the junit task, junit.jar must be added
to the system classpath with optional.jar; the script in
aspectj/build/bin/ant.[bat|sh] does this.

To run junit tests and junitreport (and other optional tasks) requires 
declaring the taskdef explicitly. See build-modules.xml.  

For ant scripts, use build/bin/ant.[bat|sh], which adds junit.jar.
