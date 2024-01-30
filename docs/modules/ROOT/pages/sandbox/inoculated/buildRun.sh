#!/bin/sh

JDKDIR="${JDKDIR:-${JAVA_HOME:-`setjdk.sh`}}"
AJ_HOME="${AJ_HOME:-`setajhome.sh`}"
PS="${PS:-;}"
ajrt=`pathtojava.sh "$AJ_HOME/lib/aspectjrt.jar"`
mkdir -p ../classes

for i in *.java; do
    pack=`sed -n '/package/s|.*package  *\([^ ][^ ]*\)[ ;].*|\1|p' "$i"`
    [ -n "$pack" ] && pack="${pack}."
    rm -rf classes/*
    cname=$pack`basename $i .java`
    echo ""
    echo "########## $cname"
    $AJ_HOME/bin/ajc -d ../classes -classpath "$ajrt" "$i"
    && $JDKDIR/bin/java -classpath "../classes${PS}$ajrt" $cname
done

rm -rf ../classes
