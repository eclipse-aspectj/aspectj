#!/bin/sh
# info: test -argfile and @ variants
# syntax: $0 (define JDKDIR and AJBASE)
# @process-test ajc argfiles.sh # test -argfile and @ variants

[ -n "$DEBUG" ] && set -vx 

errMssg() {
    [ -n "$1" ] && echo "## $0 $1"
    [ -n "$2" ] && exit "$2"
}

## @testcase (support) NPE with -argfile foo.lst with "*.java"
testcaseDesign() {
cat<<EOF
## input variants
command line:  [ [-argfile | @] X <fileRef>{lst} ]...
fileRef:       [[local | relative | absolute] X [DOS|Unix|Mac]]
file line:     [ option | [@<fileRef>{lst} | <fileRef>{java|aj}] ]
option:        ajc options
refLocality:   [cwd | argfile]

## white box
- @, -argfile delegate to same file-creation method
- direct locality matters: null used for for locals and..
- indirect locality matters: referent varies

## design
-- structure
- invocation
  - could set up as unit tests of ConfigParser.java
    - if so, have also to test delivery to ajc? e.g., do package names
      get calculated correctly under different path variants?
      Probably not an issue now, but perhaps later when source-path
      searching is enabled.
  - could invoke ajc.Main programmatically - can ajc be run repeatedly?
    -> might be easier/safer to script, uncover [bash, DOS,...] shell quirks?
-- test case variants
- command-line 
  - test options
    - ajc
    - esp. multi-flag (-source 1.4) - separate lines?
    - VM/Javac?
  - @, -argfile is independent
  - test 0, 1, 3 on same line, interspersed
  - error: @java file, unflagged arg file
- fileRef depends on locality:
  - parm file: CWD, relative-up, relative down-up, absolute, wrong \/:
    - relative and absolute traversing through directories with
      spaces in them (not in package-named directories)
  - file line: same, with * variants 
    - * variants: [*.java | *.aj] [ empty | 1 | n>1 ]
- fileRef also depends on indirection path:
  - add traversal cases
    - 1-2 levels of indirection
- need to think about whether indirection depends on locality reference
  i.e., whether 1-2 levels of indirection need to be through
  all locality types (CWD, relative...)
- basic argfile
  - options
  - files
  - @ref
  - comments
  - empty lines
  - *.java, *.aj references - re

## implementation
- Source tree 
  - minimal, star, maximal: all need each other to compile
  - some in default package, others in named packages
  - some do not share the same source base (or file system?)
  - files:
  Minimal:
  src/
    Main.java
    pack/Main.java
  Star:
  star/
    Main.java
    empty/
    many/One.java
    many/Two.java
    
  Maximal:
  src1/
    Src1DefaultMain.java
    pack1/Src1Pack1Main.java
    pack2/Src1Pack2Main.java
  src2/
    Src2DefaultMain.java
    pack1/Src2Pack1Main.java
    pack2/Src2Pack2Main.java

-

EOF
}

generateCurrentSource() {
    genSource "" Main "Main SomeClass" > Main.java
    genSource "" SomeClass "Main SomeClass" > SomeClass.java

    cat > current_Main_0.lst <<EOF
*.java
EOF
    cat > current_SomeClass_0.lst <<EOF
*.java
EOF
    echo "current"
}

generateMinimalSource() {
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

generateStarSource() {
    echo "todo incomplete"
    mkdir -p star star/many star/empty
    genSource "" Main "Main many.One many.Two" > star/Main.java
    genSource "many" Main "many.One many.Two" > star/many/One.java
    genSource "many" Main "many.One many.Two" > star/many/Two.java
    cat > star/star_Main_1.lst <<EOF
*.java
empty/*.java
many/*.java
EOF
  
    mkdir src1/pack1 src1/pack2 src2/pack1 src2/pack2
    refs="pack1.Src1Pack1Main pack2.Src1Pack2Main pack1.Src2Pack1Main pack2.Src2Pack2Main"
    genSource "" Main "Src1Main $refs" > src1/Src1Main.java
    genSource "" Main "Src2Main $refs" > src2/Src2Main.java
    genSource "pack1" Main "$refs" > src1/pack1/Src1Pack1Main.java
    genSource "pack2" Main "$refs" > src1/pack2/Src1Pack2Main.java
    genSource "pack1" Main "$refs" > src2/pack1/Src2Pack1Main.java
    genSource "pack2" Main "$refs" > src2/pack2/Src2Pack2Main.java
}

genSource() {
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
  [ -d classes ] || mkdir classes
  rm -rf classes/*
  $JDKDIR/bin/java -classpath "$toolsjar" org.aspectj.tools.ajc.Main \
     -d classes -classpath "$utiljar${PS}$rtjar" "${@}"
}

run() {
  $JDKDIR/bin/java -classpath "$utiljar${PS}$rtjar${PS}classes" "${@}"
}

testAjc() {
  argfile="$1"
  [ -n "$VERBOSE" ] && echo "# $1"
  [ -f "$argfile" ] || errMssg "testAjc argfile: $1" 3
  main=`getMain "$argfile"`
  [ -n "$main" ]  || errMssg "testAjc no main for argfile: $1" 3
  compile -argfile "$argfile" || errMssg "testAjc compile 1 fail: $1" 3
  run "$main" || errMssg "testAjc run 1 fail: $1" 3
  compile @"$argfile" || errMssg "testAjc compile 2 fail: $1" 3
  run "$main" || errMssg "testAjc run 2 fail: $1" 3
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

d=tempArgfiles
[ -d $d ] && rm -rf $d 
mkdir $d  && cd $d

name=`generateCurrentSource`
for i in `getArgFiles "$name"` ; do testAjc "$i"; done

name=`generateMinimalSource`
for i in `getArgFiles "$name"` ; do testAjc "$i"; done

# re-run without src prefix..
cd src
for i in `getArgFiles "$name"` ; do testAjc "$i"; done
echo "tests passed if no compiler failures, only output"

