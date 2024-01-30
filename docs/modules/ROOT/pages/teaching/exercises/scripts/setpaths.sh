# this file should be sourced, NOT executed

# Change this to be the full path for this directory
EXTRACTION=$HOME/aj-@aj.event@

if [ "$JAVA_HOME" = "" ] 
then
    echo Please remember to manually set $JAVA_HOME to 
    echo the location of your java installation
fi

export ASPECTJ_HOME=$EXTRACTION/aspectj
export PATH=$ASPECTJ_HOME/bin:$PATH
export CLASSPATH=.:$ASPECTJ_HOME/lib/aspectjrt.jar:$EXTRACTION/junit.jar:$JAVA_HOME/jre/lib/rt.jar

chmod a+x $ASPECTJ_HOME/bin/ajc
chmod a+x $ASPECTJ_HOME/bin/ajbrowser

