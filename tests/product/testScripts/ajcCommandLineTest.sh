#!/bin/sh
# info: test ajc command line
# syntax: $0 (define JDKDIR and AJ_BASE)
# @testcase PR#655 ajc errors when output dir is bad
# @process-test ajc acjCommandLineTest.sh # test PR#655 ajc errors when output dir is bad

[ -n "$DEBUG" ] && set -vx 

errMssg() {
    [ -n "$1" ] && echo "## $0 $1"
    [ -n "$2" ] && exit "$2"
}

generateMinimalSource() { # todo: share with other tests
    mkdir -p src src/pack
    genSource "" Main "Main pack.Main" > src/Main.java
    genSource pack Main "pack.Main" > src/pack/Main.java

    cat > src/minimal_pack.Main_0.lst <<EOF
pack/Main.java
EOF
    cat > src/minimal_Main_1.lst <<EOF
Main.java
pack/Main.java
EOF
    cat > src/pack/minimal_Main_2.lst <<EOF
../Main.java
Main.java
EOF
    cat > src/pack/minimal_Main_3.lst <<EOF
../*.java
*.java
EOF
    cat > minimal_Main_4.lst <<EOF
@src/minimal_Main_1.lst
EOF
    cat > src/minimal_Main_5.lst <<EOF
@minimal_Main_1.lst
EOF
    cat > src/minimal_Main_6.lst <<EOF
@../src/minimal_Main_1.lst
EOF
    cat > src/minimal_Main_7.lst <<EOF
// starting comment
Main.java        // end of line comment
// middle comment
pack/Main.java
// end comment
EOF
    cat > src/minimal_Main_8.lst <<EOF
*.java
pack/*.java
EOF
    echo "minimal"
}

genSource() { # todo share with other tests
    package="$1"
    class="$2"
    refs="$3"
    if [ -z "$package" ] ; then
      name="$class"
    else
      echo "package $package;"
      name="$package.$class"
    fi
cat<<EOF
public class $class {
    public static final String NAME = "$name";
    public static String[] ARGS = new String[] 
EOF
    sep='{'
    for i in $refs ; do
      echo "    $sep ${i}.NAME"
      sep=','
    done
cat<<EOF
    };
    public static void main (String[] args) {
        util.Util.signal(NAME + ".main",ARGS);
    } 
} // class $class
EOF
}

getMain() {
    [ -n "$1" ] && echo "$1" | sed 's|.*_\([^_][^_]*\)_.*|\1|'
}

compile() { 
  ccdir="${globalClasses:-classes}"
  [ -d "$ccdir" ] || mkdir "$ccdir" 
  rm -rf "$ccdir"/*
  $JDKDIR/bin/java -classpath "$toolsjar" org.aspectj.tools.ajc.Main \
     -d "$ccdir" -classpath "$utiljar${PS}$rtjar" "${@}"
}

run() {
  $JDKDIR/bin/java -classpath "$utiljar${PS}$rtjar${PS}$ccdir" "${@}"
}

testAjc() {
  argfile="$1"
  shift
  [ -n "$VERBOSE" ] && echo "# $1"
  [ -f "$argfile" ] || errMssg "testAjc argfile: $1" 3
  main=`getMain "$argfile"`
  [ -n "$main" ]  || errMssg "testAjc no main for argfile: $argfile" 3
  compile -argfile "$argfile" "${@}" || errMssg "testAjc compile 1 fail: $argfile" 3
  run "$main" || errMssg "testAjc run 1 fail: $argfile" 3
  compile @"$argfile" "${@}" || errMssg "testAjc compile 2 fail: $argfile" 3
  run "$main" || errMssg "testAjc run 2 fail: $argfile" 3
}

createUtilJar() {
  mkdir util
  cat > util/Util.java <<EOF
package util;
public class Util {
    public static void signal(String label, String[] names) {
        boolean printing = $printing;
        if (printing) {
            StringBuffer sb = new StringBuffer();
            sb.append(label);
            sb.append("=[");
            for (int i = 0; i < names.length; i++) {
              if (i > 0) sb.append(", ");
              sb.append(names[i]);
            }
            sb.append("]");
            System.out.println(sb.toString());
        }
    }
}
EOF
compile util/Util.java
"$JDKDIR/bin/jar" cf "$utiljar" -C classes .
rm -rf util
}

getArgFiles() {
  name="$1"
  find . -name "$1"\*.lst | sed 's|./||'
}

echoSetup() {
cat<<EOF
##### setup for $0
  JDKDIR:    $JDKDIR
  AJ_BASE:   $AJ_BASE
  uname -a:  `uname -a`
EOF
}

############################################################## execution
printing=true # must be true or false
JDKDIR="${JDKDIR:-c:/home/apps/jdk13}"
AJ_BASE="${AJ_BASE:-c:/home/wes/aj/aj-dist/tools}"
[ -d "$JDKDIR" ] || errMssg "require JDKDIR=$JDKDIR" 3
[ -d "$AJ_BASE" ] || errMssg "require AJ_BASE=$AJ_BASE" 3
toolsjar="$AJ_BASE/lib/aspectjtools.jar"
rtjar="$AJ_BASE/lib/aspectjrt.jar"
utiljar="$AJ_BASE/lib/argfilesUtil.jar"
[ -f "$toolsjar" ] || errMssg "require toolsjar=$toolsjar" 3
[ -f "$rtjar" ] || errMssg "require rtjar=$rtjar" 3
[ -f "$utiljar" ] || createUtilJar
echoSetup

d=temp`basename $0`
[ -d $d ] && rm -rf $d 
mkdir $d  && cd $d
export globalClasses='bad dir '
name=`generateMinimalSource`
for i in `getArgFiles "$name"` ; do 
    testAjc "$i" # expecting CE, not stack trace
done

echo "tests passed if no compiler failures, only output"

