#!/bin/sh
# syntax: <mainFile> {file}...
# info: compile files and run mainFile class (with Tester classes)
# requires JDKDIR and build output be available

[ -n "$DEBUG" ] && set -vx
pathtojava() {
    echo "$1" | sed 's|/cygdrive/\(c\)/|\1:/|' # todo
}
errMssg() {
    echo "## $0 Error: $1"
    if [ -n "$2" ] ; then exit "$2" ; fi
}

sourcesRunningInNew() {
    cat<<EOF
InnerInterfaceNames.java
InnerInterfaceAccess.java
PR691.java
AroundDoubleAssignment.java
AroundDoubleAssignmentC.java
AroundChangeThis.java
PointcutQualification.java
PointcutQualification2.java
AbstractPointcutAccess.java
SourceLocationCall.java
CallNotTarget.java
AroundCall.java
ArgsInCflow2.java
FactorialCflow.java
ArrayInc.java
TargetObjectReplacement.java
EOF
}

runAjc() {
    "$JDKDIR/bin/java" -classpath "$compilerJar" org.aspectj.tools.ajc.Main \
        -d "$classesDir" -classpath "$rtJar${PS}$clientJar" "${@}"
}
runJava() {
    "$JDKDIR/bin/java" -classpath "$rtJar${PS}$clientJar${PS}$classesDir" "${@}"
}

if [ -n "$PS" ] ; then
  case `uname` in
    CYGWIN* ) PS=";" ;;
    * ) PS=":" ;;
  esac
fi

scriptDir=`dirname "$0"`
scriptDir=`cd "$scriptDir"; pwd`
java="$JDKDIR/bin/java"
[ -x "$java" ] || errMssg "no java=$java"
ajJarsDir=`cd "$scriptDir/../../../aj-build-modules/jars"; pwd`
ajJarsDirJ=`pathtojava "$ajJarsDir"`

clientJar="$ajJarsDirJ/testing-client.jar"
compilerJar="${compilerJar:-ajJarsDirJ/alltesting.jar}"
rtJar="$ajJarsDirJ/runtime.jar"
classesDir=cl
[ -f "$clientJar" ] || errMssg "no clientJar=$clientJar" 4
[ -f "$rtJar" ] || errMssg "no rtJar=$rtJar" 4

cd "$scriptDir/../new" || errMssg "no new directory" 4
mkdir "$classesDir"
sources="${@:-`sourcesRunningInNew`}"
for srcFile in $sources ; do
    [ -f "$srcFile" ] || errMssg "no srcFile=$srcFile" 4
    className=`echo "$srcFile" | sed 's|\/|.|g;s|\.java||'`
    [ -n "$className" ] || errMssg "no className" 4
    rm -rf "$classesDir"/*
    runAjc "${srcFile}"
    runJava $className || echo '<<<< FAIL '"$srcFile"
done
rm -rf "$classesDir"

