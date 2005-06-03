#!/bin/bash
# Regression test when changing the build script.
# Compare new.zip and old.zip, assuming they contain build results
# aj-build/dist/ide/eclipse for two different versions of the build script
# (and comparing only the contents of the .jar files).
# [There must be a better way: diff support that recurses into .jar/.zip files]

[ -n "$DEBUG" ] && set -vx 

jar="${JAVA_HOME}/bin/jar"

errExit() {
  echo "ERROR $1: $2"
  exit "$1"
}

# make this empty to avoid unzipping new.zip and old.zip
dozip="yes"
if [ -z "${dozip}" ] ; then
	cd temp
else
	rm -rf temp
	mkdir temp
	cd temp
	for d in new old; do
		[ -f ../$d.zip ] || errExit 2 "make $d.zip";
		mkdir $d
		cd $d
		$jar -xf ../../$d.zip
		cd eclipse
		ls *.jar | sort > ../../$d-names.txt
		cd ../..
	done
fi

  diff *-names.txt > /dev/null || errExit 3 "expected same products"
  for j in `cat new-names.txt`; do
     mkdir $j-dir;
     cd $j-dir
     for d in new old; do 
	     mkdir $d
	     cd $d
	     $jar xf ../../$d/eclipse/$j
	     dirpath=`pwd | sed 's|/cygdrive/c|c:|'`
	     for z in `find . -type f -name \*.jar -o -name \*.zip`; do
	        mkdir ${z}-dir
	        pushd ${z}-dir
	        $jar -xf $dirpath/${z}
	        popd
	     done
	     cd ..
     done
	 diff -qr new old > diffs.txt
     cd ..
  done
  cat */diffs.txt
  wc -l */diffs.txt




