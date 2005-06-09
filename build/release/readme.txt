
This directory has scripts to build and test an AspectJ release.
At the bottom of this readme is a bash script to run it.

NOTE: currently the set of tests done during the release build is
a subset of available product tests...

---- Evaluating the results of a build cycle

There is a ../release-checklist.txt which has steps for a release, some of 
which are automated in this script.  Try the others ;).

Known issues with these tests

- ajc compile of tjp example as part of nonGui target
  runs out of memory.  Reproducible on the command-line: 
      [ajc] Running  in-process using -verbose -d R:\home\wes\ec\aj-
build\install\doc\examples\classes -classpath R:\home\wes\ec\aj-buil
d\install\lib\aspectjrt.jar -argfile R:\home\wes\ec\aj-build\install
\doc\examples\tjp\files.lst
Exception reading component R:\home\wes\ec\aj-build\install\lib\aspe
ctjtools.jar
java.lang.OutOfMemoryError
        <<no stack trace available>>
Exception in thread "main" java.lang.OutOfMemoryError
        <<no stack trace available>>
  
  This target is run without failonerror="true" to complete
  the testing.          
  
Known imperfect results in the AspectJ 1.1 release:

- ajdeCompiler tests will have five known fails, listed
  in releases/aspectj1.1/ajcTests-ajdeCompiler-FAIL.txt
  
- under 1.4, the junit module test output for the weaver
  module is corrupted - the last two characters are 
  duplicated, which cause the junit reporting task to
  not pick up the weaver tests.  Verify that the 
  weaver XML file lists no JUnit failures or errors.


------ untested bash/sh script
#!/bin/bash
[ -n "$DEBUG" ] && set -vx
scriptDir=`dirname "${0}"`
scriptDir=`cd "$scriptDir"; pwd | sed 's|/cygdrive/c/|c:/|'`

#alias myant="$scriptDir/../../lib/ant/bin/ant"
myant="$scriptDir/../../lib/ant/bin/ant"

# clean, create distribution jar
cd `dirname "$0"`
cd ..
"$myant" clean
"$myant" 

# install distribution:
cd release
"$myant" install 
  
# test installation and sources in local tree:
[ -z "$skipTest" ] && "$myant" test ${sp} -Dskip.build=true 

# check 1.1 runtime - compile will fail,
# but should only be in known 1.2-dependent classes
#export JAVA_HOME=c:/home/apps/jdk13
#"$myant" compile-runtime-11 
