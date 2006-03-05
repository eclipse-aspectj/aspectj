#!/bin/sh
# run sample use cases
[ -z "$DEBUG" ] || set -vx
VERBOSE="${VERBOSE}"

# define and export ajc, JDKDIR
ajc="${ajc:-ajc}"
ajrt="`dirname "${ajc}"`/../lib/aspectjrt.jar"
JAVA_HOME="${JAVA_HOME:-`type java | sed -n '/\/bin\/java/s|/bin/java.*||'`}"
JAVA="$JAVA_HOME/bin/java"
JAVAC="$JAVA_HOME/bin/javac"
JAR="$JAVA_HOME/bin/jar"
PS=";" ### XXX windows

## for direct access to eclipse
useEclipse="${useEclipse}"
if [ ! -f "$ajrt"  -o -n "$useEclipse" ] ; then
    modulesDir=`dirname "$0"`/../../../../modules
    if [ ! -d "$modulesDir/build/src" ] ; then
        modulesDir=`dirname "$0"`/../../../../../modules
    fi
    [ ! -d "$modulesDir/build/src" ] && exit 42
    main=app.Main
    ws="$modulesDir"
    for i in asm bridge bcweaver org.aspectj.ajdt.core org.eclipse.jdt.core util ; do
       ccp="$ccp;$ws/$i/bin"
    done
    ccp="$ccp;$ws/lib/eclipse2.0/jdtDepends.jar;$ws/lib/bcel/bcel.jar"
    ajrt="$ws/runtime/bin"
    [ -d "$ajrt" ] && useEclipse=yesUseEclipse
fi

compile() {
    [ -n "$VERBOSE" ] && echo "compile ${@}"
    if [ -n "$useEclipse" ] ; then
        $JAVA -classpath "$ccp" org.aspectj.tools.ajc.Main "${@}"
    else
        $ajc "${@}"
    fi
}

cleanDirs() {
    [ -d jars ] || mkdir jars
    [ -d classes ] || mkdir classes
    rm -rf classes/*
    for i in profile myprofile app; do mkdir classes/$i ; done
}

makeJar() {
    $JAR cfM jars/$1.jar -C classes .
    cleanDirs
}
runTest() {
  runcp="${1:-$ajrt${PS}classes}"
  name="${2:-default}"
  [ -n "$VERBOSE" ] && echo "run ${@}"
  $JAVA -classpath "$runcp" $main \
     > $name.out.txt 2> $name.err.txt
  if [ ! "$name" = "expected" ] ; then
    diffResult=PASS
    for i in out err; do
        diff expected.$i.txt $name.$i.txt || diffResult=FAIL
        [ -n "$VERBOSE" ] && wc -l expected.$i.txt $name.$i.txt
    done
    echo "${diffResult}: $runcp"
  fi
}

## 0: setup
cd `dirname "$0"`
cleanDirs
find . -name \*.java           | sed 's|^./||' > files.lst
grep -l aspect `cat files.lst` | sed 's|^./||' > aspects.lst
grep -L aspect `cat files.lst` | sed 's|^./||' > java.lst

## 1: compile all from sources
compile -d classes -classpath "$ajrt" -argfile files.lst
makeJar compileAll
runTest "$ajrt${PS}jars/compileAll.jar" expected

## 2: generate and weave user library with aspect sources
 # note: relies on unwoven classes not being flagged for not-weaving?
compile -d classes \
     -classpath "$ajrt" \
     -argfile java.lst
makeJar compileJava
compile -d classes \
     -injars jars/compileJava.jar \
     -classpath "$ajrt" \
     -argfile aspects.lst
makeJar weaveUserLib
runTest "$ajrt${PS}jars/weaveUserLib.jar" weaveUserLib

## 3: generate aspect library
compile -d classes \
     -classpath "$ajrt" \
     -XterminateAfterCompilation \
     -outjar jars/aspectLib.jar \
     -argfile aspects.lst
#makeJar aspectLib

## 3a: weave aspect library with user sources
compile -d classes \
     -aspectpath jars/aspectLib.jar \
     -classpath "$ajrt" \
     -outjar jars/weaveAspectLib.jar \
     -argfile java.lst
#makeJar weaveAspectLib
runTest "$ajrt${PS}jars/aspectLib.jar${PS}jars/weaveAspectLib.jar" weaveAspectLib

## 3b: weave aspect library with user library
compile -d classes \
     -aspectpath jars/aspectLib.jar \
     -injars jars/compileJava.jar \
     -classpath "$ajrt" 
makeJar weaveAllLib
runTest "$ajrt${PS}jars/aspectLib.jar${PS}jars/weaveAllLib.jar" weaveAllLib
