#!/bin/bash
# jr4 tf jdtcore-for-aspectj.jar images/ant_logo_large.gif readme.txt

errExit() {
	[ -n "$1" ] && echo "ERROR: $1"
	[ -n "$2" ] && exit "$2"
}
[ -f build.xml.mine ] || errExit "requires build.xml.mine" 23
[ -f build.xml.head ] || errExit "requires build.xml.head" 23

cp build.xml build.xml.orig

scriptDir=`dirname "${0}"`
cd "$scriptDir/.."

ant=../lib/ant/bin/ant
[ -n "$DEBUG"] || ant="../lib/ant/bin/ant -verbose"
jdk="${JAVA_HOME:-${JDKDIR:-c:/home/apps/jdk14}}"
jr4="$jdk/bin/jar"

rm -rf ../aj-build/*
[ -d ../aj-build/jars ] && exit 23
$ant build
mv ../aj-build/jars/build.jar ../lib/build/build.jar || exit 54

rm -rf build-test
[ -d build-test ] && errExit "rm build-test" 34
mkdir build-test

cp build.xml.head build.xml
rm -rf ../aj-build/*
[ -d ../aj-build/jars ] && errExit "rm aj-build" 23
$ant aspectjtools-dist
mv ../aj-build/dist build-test/dist-head
mv build.xml.orig build.xml

cp build.xml.mine build.xml
rm -rf ../aj-build/*
[ -d ../aj-build/jars ] && errExit "rm aj-build" 23
$ant aspectjtools-dist
mv ../aj-build/dist build-test/dist-mine
mv build.xml.orig build.xml

cd build-test
for i in dist-mine dist-head; do 
  $jr4 tf $i/tools/lib/aspectjtools.jar \
    | sort \
    > $i.txt; 
done; 

# skip
diff dist*.txt
wc -l dist*.txt
grep ant_logo_large dist*.txt
echo "rm -rf build-test"

