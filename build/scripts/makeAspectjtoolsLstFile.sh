#!/bin/bash
# generate .lst file for aspectjtools.jar
# by compile all non-testing src dirs
# todo: copy ajbrowser manifest
[ -n "$DEBUG" ] && set -vx

usage() {
    name=`basename "$0"`
	cat<<EOF
 Usage: $name { build | manifest }
For compiling aspectjtools-aj.jar using ajc, generate build/aspectjtools.lst  
and files it refers to, aspectjtools-sources.lst and aspectj-resources.jar.  

To add to aspectjtools.jar a meta-inf/manifest.mf with a main-class, 
specify a single argument: "manifest"

To create the .lst file, use ajc to build, and add the manifest,
define ASPECTJ_HOME and specify a single argument: "build"

EOF
}

canonicalPath() {
	cygpath -ma "$@"
}

filterOutNonsourceDirs() {
    sed '/\/build\//d;/\/testing/d'
}

listSourceDirs() {
	for i in ../*/testsrc; do 
	     p=`echo $i \
	        | filterOutNonsourceDirs \
	        | sed 's|../\(.*\)/testsrc|\1|'`;
	     p=`canonicalPath "../$p"`
	     [ -d "$p/src" ] && echo "$p/src"
	 done
}

generateSourcesLstFiles() {
	[ -n "$DEBUG" ] && set -vx
	rm -f aspectjtools-sources.lst; 
	for i in `listSourceDirs` ; do
	     find "$i" -type f -name \*.java \
	        | sed 's|/[^/]*.java|/*.java|'  \
	        | sort -u ; 
	done > aspectjtools-sources.lst; 
	# head aspectjtools-sources.lst
}

genResourceJar() { # slow, but hey
	[ -n "$DEBUG" ] && set -vx
	jarFile=`canonicalPath "$1"`
	[ -n "$jarFile" ] || exit 43
	
	"${JAVA_HOME:-c:/home/apps/jdk14}/bin/"jar \
	   cfM "$jarFile" build.xml
	
	curDir=`pwd`;
	for i in `listSourceDirs` ; do
        cd "$i"
		"${JAVA_HOME:-c:/home/apps/jdk14}/bin/"jar \
   		  ufM "$jarFile" `find . -type f | sed '/CVS/d;/\.java/d'`
		cd "$curDir"
	done 
	cd "$curDir"

#	mf=`canonicalPath "grj_manifest.txt"`	
#	sed 's|@build.version.short@|1.1.1|;s|@company.name@|na|' \
#	  ../ajbrowser/ajbrowser.mf.txt > "$mf"
#	"${JAVA_HOME:-c:/home/apps/jdk14}/bin/"jar \
#	   ufm "$jarFile" "$mf" build.xml
#	rm "$mf"
}

addManifest() {
	outjar="$1"
	mf=`canonicalPath "grj_manifest.txt"`	
	sed 's|@build.version.short@|1.1.1|;s|@company.name@|na|' \
	  ../ajbrowser/ajbrowser.mf.txt > "$mf"
	echo "nothing" > am_nothing
	"${JAVA_HOME:-c:/home/apps/jdk14}/bin/"jar \
	   ufm "$outjar" "$mf" am_nothing
	rm "$mf" am_nothing
}

getClasspath() {
	[ -n "$DEBUG" ] && set -vx
	ajrt=`canonicalPath  "${ASPECTJ_HOME:-c:/home/apps/aspectj-1.1.1}/lib/aspectjrt.jar"`
	echo "${ajrt};`cygpath -ma ../lib/ant/lib/ant.jar`"
}

getInjars() {
	[ -n "$DEBUG" ] && set -vx	
	cp=""
	sep=""
	for i in "$1" ../lib/bcel/bcel.jar \
	         ../org.eclipse.jdt.core/jdtcore-for-aspectj.jar \
	         ; do
		cp="${cp}${sep}`canonicalPath "$i"`"
		[ -z "${sep}" ] && sep=";"
	done
	echo "$cp"
}

setupManifest() {
	[ -n "$DEBUG" ] && set -vx
	midir="../ajbrowser/src/META-INF"
	[ -d "$midir" ] || mkdir ../ajbrowser/src/META-INF
	sed 's|@build.version.short@|1.1.1|;s|@company.name@|na|' \
	  ../ajbrowser/ajbrowser.mf.txt > "$1"
}

generateFile() {
	resourceJar="$1"
	outputJar="$2"
	[ -n "$DEBUG" ] && set -vx
	[ -f "$resourceJar" ] || exit 98
	[ -n "$outputJar" ] || exit 98
	rm -f "$outputJar"
	PS=";"
	#sourceroots=`getSourceroots`
	classpath=`getClasspath`
	injars=`getInjars "$resourceJar"`
    cat<<EOF
# generated `date` by $0
# remove this line if modifying this file
-outjar
$outputJar
-classpath
$classpath
-injars
$injars
-argfile
`canonicalPath aspectjtools-sources.lst`
EOF
}

errExit() {
	[ -n "$1" ] && echo "## error: $1"
	[ -n "$2" ] && exit "$2"
}

#########################################################################
#########################################################################
scriptDir=`dirname "$0"`
scriptDir=`cd "$scriptDir"; pwd`
script=`canonicalPath "$0"`
export PS=";" ### XXX platform
ASPECTJ_HOME="${ASPECTJ_HOME:-~/dev/tools/aspectj-1.1.1}"
export ASPECTJ_HOME=`cygpath -m "${ASPECTJ_HOME}"`

cd "$scriptDir/.."
outjar=`canonicalPath aspectjtools-aj.jar`

if [ -f aspectjtools.lst ] ; then
    grep -q "remove this line" aspectjtools.lst \
    && rm -f aspectjtools.lst \
    || exit 23
fi
set -e
if [ "$1" = "manifest" ] ; then
   addManifest "$outjar"
elif [ "build" = "$1" ] ; then
    [ "$script" -ot aspectjtools.lst ] || "$script"
    [ -d "$ASPECTJ_HOME" ] || errExit "define ASPECTJ_HOME" 49
    "$ASPECTJ_HOME"/bin/ajc -argfile aspectjtools.lst
    "$script" manifest    
elif [ -n "$1" ] ; then
    usage
else 
	generateSourcesLstFiles
	resourceJar=`canonicalPath aspectjtools-resources.jar`
	[ -f "$resourceJar" ] || genResourceJar "$resourceJar"
	outjar=`canonicalPath aspectjtools-aj.jar`
	rm -f aspectjtools.lst
	generateFile "$resourceJar" "$outjar" > aspectjtools.lst
	ls -ald "$resourceJar" aspectjtools.lst
fi
