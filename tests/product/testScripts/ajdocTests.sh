#!/bin/sh
# info: test ajc command line
# syntax: $0 (define JDKDIR and AJ_BASE)
# @testcase PR#655 ajc errors when output dir is bad
# @process-test ajdoc ajdocTests.sh # tests for PR628, PR688, PR711

[ -n "$DEBUG" ] && set -vx 

errMssg() {
    [ -n "$1" ] && echo "## $0 $1"
    [ -n "$2" ] && exit "$2"
}

runAjdoc() { 
  
  ccdir="${globalClasses:-classes}"
  [ -d "$ccdir" ] || mkdir "$ccdir" 
  rm -rf "$ccdir"/*
  #export CLASSPATH="$jdktoolsjar"
  #$JDKDIR/bin/java -jar c:/home/wes/aj/aj-build-modules/jars/ajdoc.jar \
  #   -d "$ccdir" -classpath "$rtjar" "${@}"
  $JDKDIR/bin/java -classpath "$jdktoolsjar${PS}$toolsjar" org.aspectj.tools.ajdoc.Main \
     -d "$ccdir" -classpath "$rtjar" "${@}"
}


declareWarningDoc() {
## test of declare warnings - which are not documented but should be?
echo "## warning - simple declare warning currently causes compiler failure"
cat>DeclareWarningDoc.java<<EOF

import java.io.PrintStream;

    /** class javadoc */
public class DeclareWarningDoc {
    /** method javadoc */
    public static void main (String[] args) {
        System.out.println("here"); 
    } 
}

/** javadoc for aspect */
aspect Warnings {
    /** javadoc for declared warning  - call inside method*/
    declare warning : 
        call(void PrintStream.println(String))
        : "use logger";

    /** javadoc for declared warning  - method execution */
    declare warning : 
        execution(static void main(String[]))
        : "using main";
}
EOF

runAjdoc -private DeclareWarningDoc.java

}
pr688() {

## todo test cases for
# PR#628 - docs for method declared on target
# PR#682 - fix ant should fail on compiler error (verified by inspection)

cat>AjdocTest.java<<EOF

/** 
  * @testcase PR#688 bad links cause ajdoc exceptions
  * <ul>
  * <li>link: {@link Aspect#ok()}</li>
  * <li>link: {@link C#ok()}</li>
  * <li>link: {@link C#bad}</li>
  * <li>link: {@link Aspect#bad()} // fails iff ()</li>
  * <li>link: {@link Aspect#bad}</li>
  * </ul>
  * @see Aspect#bad 
  * @see C#bad 
  * @see Aspect#ok() 
  * @see C#ok() 
 */
public interface AjdocTest { }
aspect Aspect { void ok() {} }
class C { void ok() {} }

EOF

    runAjdoc -private AjdocTest.java
}

pr628() { ## PR#628  - also test case for PR#710, PR#711, PR#712
    cat>AjdocTest.java<<EOF

/**  Test class comment */
class Test { /* int i comment */ int i;}
/** aspectj AjdocTest comment */
public aspect AjdocTest { /** AjdocTest.ok() comment */ void Test.ok() {} }
/** aspectj AnotherAspect comment */
aspect AnotherAspect { /** ok() comment */ void ok() {} }
/** class AnotherClass comment */
class AnotherClass { /** aok() comment */ void aok() {} }


EOF

runAjdoc -private AjdocTest.java
}

## todo test cases for
# PR#682 - fix ant should fail on compiler error (verified by inspection)


############################################################## execution
JDKDIR="${JDKDIR:-c:/home/apps/jdk13}"
AJ_BASE="${AJ_BASE:-c:/home/wes/aj/aj-dist/tools}"
[ -d "$JDKDIR" ] || errMssg "require JDKDIR=$JDKDIR" 3
[ -d "$AJ_BASE" ] || errMssg "require AJ_BASE=$AJ_BASE" 3
toolsjar="$AJ_BASE/lib/aspectjtools.jar"
jdktoolsjar="$JDKDIR/lib/tools.jar"
rtjar="$AJ_BASE/lib/aspectjrt.jar"
[ -f "$toolsjar" ] || errMssg "require toolsjar=$toolsjar" 3
[ -f "$rtjar" ] || errMssg "require rtjar=$rtjar" 3

curdir=`pwd`
d=temp`basename "$0" .sh`688
[ -d $d ] && rm -rf $d 
mkdir $d  && cd $d
pr688
cd "$curdir"


d=temp`basename "$0" .sh`628
[ -d $d ] && rm -rf $d 
mkdir $d  && cd $d
pr628
cd "$curdir"

echo "####### Warnings not documented - RFE"
d=temp`basename "$0" .sh`declareWarningDoc
[ -d $d ] && rm -rf $d 
mkdir $d  && cd $d
declareWarningDoc
cd "$curdir"


echo "####### PR#711 Expecting error message, not exception"
runAjdoc -d missingDir -private AjdocTest.java

# todo: clean up/evaluate
