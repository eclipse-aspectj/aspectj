#!/bin/sh
# incrementally-compile AspectJ sources using Jikes

[ -n "$DEBUG" ] && set -vx

### set for your system
jikes="${jikes:-j:/home/wes/dev/bin/win/jikes.exe}"
jdk14="${jdk14:-d:/jdk14}"
PS="${PS:-;}"

#################
ajdir=`dirname "$0"`/../..
ajdir=`cd "$ajdir"; pwd`
ajdir=`echo "$ajdir" | sed 's|/cygdrive/\([a-zA-Z]\)/|\1:/|'`
allfiles="$ajdir/allfiles.lst"
srcdirs="ajbrowser ajde asm bridge org.aspectj.ajdt.core runtime taskdefs util weaver"
libs="lib/ant/lib/ant.jar lib/bcel/bcel.jar org.eclipse.jdt.core/jdtcore-for-aspectj.jar"

classesDir="$ajdir/classes"


[ -f "$allfiles" ] && rm "$allfiles"
sourcepath=""
for i in $srcdirs; do
  sourcepath="$sourcepath${PS}$ajdir/$i/src"
  find "$ajdir/$i/src" -type f -name \*.java
done > "$allfiles"

classpath=""
for i in $libs; do
  classpath="$classpath${PS}$ajdir/$i"
done
classpath="$jdk14/jre/lib/rt.jar$sourcepath$classpath"

[ -d "$classesDir" ] || mkdir "$classesDir"
rm -rf "$classesDir"/*
set -vx
exec "$jikes" -d "$classesDir" -classpath "$classpath" @"$allfiles" ++



