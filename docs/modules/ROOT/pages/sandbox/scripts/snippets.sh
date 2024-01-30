#!/bin/sh
# shell script snippets for AspectJ


# @author Wes Isberg
# START-SAMPLE scripts-weaveLibraries Shell script to use ajc to weave jars and then run
ASPECTJ_HOME="${ASPECTJ_HOME:-c:/aspectj-1.1.0}"
ajc="$ASPECTJ_HOME/bin/ajc"

# make system.jar by weaving aspects.jar into lib.jar and app.jar
$ajc -classpath "$ASPECTJ_HOME/lib/aspectjrt.jar" \
     -aspectpath aspects.jar" \
     -injars "app.jar;lib.jar" \
     -outjar system.jar

# XXX copy any required resources from META-INF directories

# run it
java -classpath "aspects.jar;system.jar" com.company.app.Main

# END-SAMPLE scripts-weaveLibraries
