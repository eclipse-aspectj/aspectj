#!/bin/sh
# run jacks against javac and ajc (with and without usejavac)
# @process-test ajc runjacks.sh # run jacks tests against javac and ajc (with and without usejavac)

[ -n "$DEBUG" ] && set -vx

errMssg() {
    [ -n "$1" ] && echo "## $0 $1"
    [ -n "$2" ] && exit "$2"
}

toJavaPath() {
    echo "$1" | sed 's|/cygdrive/\([a-zA-Z]\)/|\1:/|'
}

toDosPath() {
    toJavaPath "$1" | sed 's|/|\\|g'
}

emit_ajcbat() {
cat<<EOF
$jhomeDos\bin\java -classpath "$ajlibDos\aspectjtools.jar;$ajlibDos\aspectjrt.jar;$jhomeDos\lib\tools.jar" org.aspectj.tools.ajc.Main %1 %2 %3 %4 %5 %6 %7 %8
EOF
}

emit_ajcsetup() {
cat<<EOF
# Set the paths for your tools in this file
set JAVA_HOME $jhomeJ
set JAVA_CLASSPATH ""
set JAVAC $jacksDirJ/ajc.bat
set JAVAC_FLAGS "$javacFlags"

# To test the assert statement, use this setup
#set JAVAC_FLAGS "-source 1.4"
set JAVA ${JDKDIR}/jre/bin/java.exe
set JAVA_FLAGS ""

# This activates encoding support.
set JAVAC_ENCODING_FLAG "-encoding "
set tcltest::testConstraints(encoding) 1

# Activate javac only test cases
set tcltest::testConstraints(javac) 1

# Uncomment this to test the assert statement
#set tcltest::testConstraints(assert) 1

EOF
}

compareJacksResults() {
  [ -f javac.out ] || exit 2
  sc='/^==== [^ ][^ ]* FAILED/s|^==== \([^ ][^ ]*\) .*|\1 '
  for ajc in ajc usejavac ; do
    [ -f $ajc.out ] || exit 2
    for i in $ajc javac ; do 
        sed -n "$sc$i|p" "$i.out" | sort > "$i.join"
    done 
    join -a 1 -a 2 $ajc.join javac.join \
       | sed "/$ajc javac/d" \
       | sort > compare.javac.$ajc.out
    rm $ajc.join javac.join
    
    ajcCount=`sed -n "/ $ajc/p" compare.javac.$ajc.out | wc -l`
    javacCount=`sed -n "/ javac/p" compare.javac.$ajc.out | wc -l`
    
    echo "## $ajcCount $ajc failures unmatched by javac" 
    echo "## $javacCount javac failures unmatched by $ajc"
  done
  wc -l *.out 
}



#############################################
SKIP_USEJAVAC=skippingUseJavac
#pattern='*abrupt*'  # use this to test script w/o running all
if [ ! "$1" = "compare" ] ; then
tclDir=/cygdrive/c/home/apps/tcl # XXX crosscut only
case `uname` in 
    CYGWIN* ) ;;
    * ) errMssg "only cygwin supported" 4 ;;
esac

[ -z "$JDKDIR" -o -x "$JDKDIR/bin/java" ] \
  || errMssg "requires valid JDKDIR=$JDKDIR" 5

[ -z "$tclDir" -o -x "$tclDir/bin/tclsh" ] \
  || errMssg "requires valid tclDir=$tclDir" 5

cvsroot=":pserver:anoncvs@oss.software.ibm.com:/usr/cvs/jikes"
scriptDir=`dirname "$0"`
scriptDir=`cd "$scriptDir";pwd`
toBaseDir=../../..
jacksDir="${jacksDir:-`cd "$scriptDir/$toBaseDir";pwd`/jacks}"
ajlibDir="${ajlibDir:-`cd "$scriptDir/$toBaseDir";pwd`/aj-build-modules/jars}"
outDir="${outDir:-${1:-.}}"
[ -d "$outDir" ] || exit 3

jacksDirJ=`toJavaPath "$jacksDir"`
jhomeJ=`toJavaPath "$JDKDIR"`
ajlibDos=`toDosPath "$ajlibDir"`
jhomeDos=`toDosPath "$JDKDIR"`

[ -f "$ajlibDir/aspectjtools.jar" ] \
  || errMssg "bad ajlibDir=$ajlibDir" 4

## get jacks if necessary
if [ -d "$jacksDir" ] ; then
  [ -f "$jacksDir/jacks.tcl" ] || errMssg "bad jacksDir" 4
  echo "## using existing jacks dir at $jacksDir"
else
  cd `dirname "$jacksDir"` || errMssg "bad path to $jacksDir" 5
  echo anoncvs | cvs -d "$cvsroot" login 
  cvs -d "$cvsroot" co jacks 
fi

## setup our stuff
cd "$jacksDir" || errMssg "jacks dir failed" 6

# comment unix, uncomment cygwin
sc='/ulimit/s|^|#|'
sc="$sc;"'/^exec tclsh8*/s|^|#|'
sc="$sc;"'/^##* *exec tclsh.*cygpath/s|^##* *e|e|'
sed "$sc" jacks > ff \
  && mv ff jacks || errMssg "unable to setup jacks for cygwin" 2

export PATH="$tclDir/bin:$JDKDIR/bin:$jacksDir:$PATH"

## javac should work in any case
if [ -f javac.out ] ; then
    echo "using existing javac.out"
    ls -ald javac.out
else 
    ## need to setup JAVA_HOME, JAVAC, JAVA
    sc='/^set JAVA_HOME /s|.*|set JAVA_HOME '"$JDKDIR|" 
    sc="$sc;"'/^set JAVAC /s|.*|set JAVAC $JAVA_HOME/bin/javac.exe|' 
    sc="$sc;"'/^set JAVA /s|.*|set JAVA $JAVA_HOME/bin/java.exe|' 
    sed "$sc" javac_setup > ff && mv ff javac_setup
    echo "## starting javac at `date`"
    jacks javac $pattern > "$outDir/"javac.out || errMssg "javac failed " "$?"
    echo "##   ending javac at `date`"
fi

if [ -f "$outDir/"ajc.out ] ; then
    echo "using existing ajc.out"
    ls -ald "$outDir/"ajc.out
else 
    ## setup and run  ajc
    emit_ajcbat > ajc.bat
    emit_ajcsetup > ajc_setup
    echo "## starting ajc at `date`"
    jacks ajc $pattern > "$outDir/"ajc.out || errMssg "ajc failed " "$?"
    echo "##   ending ajc at `date`"
fi

if [ -f "$outDir/"usejavac.out ] ; then
    echo "using existing usejavac.out"
    ls -ald "$outDir/"usejavac.out
elif [ -n "$SKIP_USEJAVAC" ] ; then 
    echo "skipping usejavac"
else 
    ## setup -usejavac
    javacFlags="-usejavac"
    emit_ajcsetup > ajc_setup
    echo "## starting ajc -usejavac at `date`"
    jacks ajc $pattern > "$outDir/"usejavac.out || errMssg "usejavac failed " "$?"
    echo "##   ending ajc -usejavac at `date`"
fi

fi ## not only comparing

compareJacksResults

