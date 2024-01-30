# this file should be sourced, NOT executed

# Change this to be the full path for this directory
set EXTRACTION=$HOME/aj-@aj.event@

set ASPECTJ_HOME=$EXTRACTION/aspectj
set PATH=$ASPECTJ_HOME/bin:$PATH
set CLASSPATH=.:$ASPECTJ_HOME/lib/aspectjrt.jar:$EXTRACTION/junit.jar:$JAVA_HOME/jre/lib/rt.jar

chmod a+x $ASPECTJ_HOME/bin/ajc
chmod a+x $ASPECTJ_HOME/bin/ajbrowser
