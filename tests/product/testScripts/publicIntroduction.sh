#!/bin/sh
# @testcase public introductions not visible in libraries given to javac or jikes
# @testcase synthetic members should not be visible from libraries given to ajc todo??
# @process-test ajc publicIntroduction.sh # two testcase for public introductions

[ -n "$DEBUG" ] && set -vx

echo "before running: define (install) AJ_BASE and add #!/bin/sh to scripts"
scriptDir=`dirname "$0"`
scriptName=`basename "$0" .sh`
tempDir=temp${scriptName}
JDKDIR="${JDKDIR:-${JAVA_HOME:-`type java | sed -n '/bin\/java/s|/bin/java.*||p'`}}"
AJ_BASE="${AJ_BASE:-`type ajc | sed -n '/bin\/ajc/s|/bin/ajc.*||p'`}"
ajLib="${AJ_BASE}/lib/aspectjrt.jar"
PS="${PS:-;}" # todo

errMssg() {
    [ -n "$1" ] && echo "## FAIL: $1"
    [ -n "$2" ] && exit "$2"
}

makeSources() {
    cat > Javap.java<<EOF
public class Javap {
  interface I {
    void i();
  }
  class C {}
  static aspect A {
    public void I.i() {}
    declare parents: C implements I;
  }
}
EOF

    cat > Target.java <<EOF
public class Target { 
    // regular members
    public long publicLong;
    public long getPublicLong() { return publicLong; }
}

aspect A {
    // introduced members
    public int Target.publicInt;
    public int Target.getPublicInt() {
        return publicInt;
    }
    public static int Target.staticPublicInt;
    public static int Target.getStaticPublicInt() {
        return staticPublicInt;
    }
}

EOF
   mkdir user 
    cat > user/TargetUser.java<<EOF
public class TargetUser {
    public static void main (String[] args) {
        Target me = new Target();
        // regular members
        long l = me.publicLong;
        l = me.getPublicLong();
        // introduced members
        int i = me.publicInt;
        i = me.getPublicInt();
        i = me.getStaticPublicInt();
        i = me.staticPublicInt;
    }
}
EOF
}

#############################################################
curdir=`pwd`
[ ! -d "$tempDir" ] && mkdir -p "$tempDir"
cd "$tempDir" || errMssg "unable to creat temp dir" 3

[ -z "$JDKDIR" ]  \
  && errMssg "expecting \$JDKDIR/bin/java:  $JDKDIR/bin/java" 3

[ -z "$AJ_BASE" ]  \
  && errMssg "expecting \$AJ_BASE/bin/ajc:  $AJ_BASE/bin/ajc" 3

makeSources

mkdir classes
### javap case - PR#649
cmdName=ajc.javap
"$AJ_BASE/bin/ajc" -classpath "$ajLib" \
    -d classes Javap.java \
    > $cmdName.txt  2>&1 \
    || errMssg "$cmdName: `cat $cmdName.txt`" 3

cmdName=javap
"$JDKDIR/bin/javap" -classpath "$ajLib${PS}classes" Javap \
    > $cmdName.txt  2>&1 \
    || errMssg "$cmdName: `cat $cmdName.txt`" 3

# javac case - PR#646
cmdName=ajc
"$AJ_BASE/bin/ajc" -classpath "$ajLib" \
    -d classes Target.java \
    > $cmdName.txt  2>&1 \
    || errMssg "$cmdName: `cat ../$cmdName.txt`" 3

cd user

cmdName=../javac
"$JDKDIR/bin/javac" -classpath ../classes -d ../classes TargetUser.java \
    > $cmdName.txt  2>&1 \
    || errMssg "$cmdName: `cat $cmdName.txt`" 3

cd ..

cmdName=java
"$JDKDIR/bin/java" -classpath "$ajLib${PS}classes" TargetUser \
    > $cmdName.txt  2>&1 \
    || errMssg "$cmdName: `cat $cmdName.txt`" 3

cd "$curdir"
rm -rf "$tempDir"

echo "tests passed if they got to this point..."

    


