#!/bin/sh
# time compile of sources to aspectjtools and aspectjrt
# using many compilers

[ -n "$DEBUG" ] && set -vx


## redirect to stdout to avoid timing errors from console

### set for your system
jdk14="${jdk14:-d:/jdk14}"
jdk13="${jdk13:-j:/home/apps/jdk13}"
jikes="${jikes:-j:/home/wes/dev/bin/win/jikes.exe}"
aj110="${aj110:-j:/home/wes/dev/tools/aspectj-1.1.0}"
aj111="${aj111:-j:/home/wes/dev/tools/aspectj-1.1.1rc1}"
PS="${PS:-;}"

#################
ajdir=`dirname "$0"`/../..
ajdir=`cd "$ajdir"; pwd`
ajdir=`echo "$ajdir" | sed 's|/cygdrive/\([a-zA-Z]\)/|\1:/|'`
allfiles="$ajdir/allfiles.lst"
onefile="$ajdir/onefile.lst"
srcdirs="ajbrowser ajde asm bridge org.aspectj.ajdt.core runtime taskdefs util weaver"
libs="lib/ant/lib/ant.jar lib/bcel/bcel.jar org.eclipse.jdt.core/jdtcore-for-aspectj.jar"

classesDir="$ajdir/classes"
ajc110=$aj110/bin/ajc
ajc111=$aj111/bin/ajc

classpath=""
for i in $libs; do
  classpath="$classpath${PS}$ajdir/$i"
done
sourcepath=""
sep=""
for i in $srcdirs; do
  sourcepath="$sourcepath${sep}$ajdir/$i/src"
  [ -n "$sep" ] || sep="${PS}"
done


if [ ! -f "$allfiles" ] ; then
  for i in $srcdirs; do
    find "$ajdir/$i/src" -type f -name \*.java
  done > "$allfiles"
fi

if [ ! -f "$onefile" ] ; then
  echo "$ajdir/util/src/org/aspectj/util/PartialOrder.java" > "$onefile"
fi

[ -d "$classesDir" ] || mkdir "$classesDir"

for argfile in "$onefile" "$allfiles" ; do
for compiler in "$jdk14"/bin/javac "$jdk13"/bin/javac "$ajc110" "$ajc111" "$jikes" ; do
  rm -rf "$classesDir"/*
  if [ "$ajc111" = "$compiler" ] ; then
    cp="$aj111/lib/aspectjrt.jar$classpath"
  elif [ "$jikes" = "$compiler" ] ; then
    cp="$aj110/lib/aspectjrt.jar$classpath${PS}$jdk14/jre/lib/rt.jar${PS}$sourcepath"
  else
    cp="$aj110/lib/aspectjrt.jar$classpath"
  fi
  echo "##################################### $compiler $cp"
  start=`date +%s`  
  $compiler \
    -d "$classesDir" -classpath "$cp" @"$argfile" 
  end=`date +%s`
  duration=`expr $end - $start`
  echo "$duration ($start - $end) # $compiler"
done
done

rm -f "$allfiles" "$onefile"
exit


