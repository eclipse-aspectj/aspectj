This example shows how to call an AOP Alliance aspect (interceptor) from
within AspectJ. 

The contents of the src directory comprise some library classes plus an
abstract aspect that can be extended to create AOP Alliance invoking concrete
aspects (provide an implementation of getMethodInterceptor() and the 
targetJP() pointcut in your concrete subaspect). 

The contents of the testsrc directory supply test cases, and in so doing a
sample of using the AOPAllianceAdapter aspect (HelloAOPAllianceAdapter aspect).

With the docs module checked out of the AspectJ CVS tree, I build and test 
these AOPAlliance samples as follows:

Create an AJDT project, "AOPAlliance" (you can call it whatever you like).
Add the file lib/aopalliance.jar to the build path. Remove the default source
directory that will have been set up for the project, and add a "src" directory.
When doing this click "advanced" and set it up as a link to the src folder in
this directory. Do the same for "testsrc". Now you have an AJDT project that
should build and take its source from this directory. To run the tests, select
the "AllTests.java" file in the AJDT project and choose "run as junit."
