#!/bin/sh
# kludge to generate a bat script using ajc.bat, ajdoc.bat, and ajdb.bat
# set the Java command for the target platform,
# and run the resulting script in the aspectj directory
# @process-test tools makeTestBat.sh # generate .bat script to test examples

JAVA='C:\home\apps\jdk13\bin\java'
errMssg() {
  [ -n "$1" ] && echo "## $0 - $1"
  [ -n "$2" ] && exit "$2"
}

javaCommand() {
  # grep 'main(' */*.java | sed 's|\.java.*||;s|\/|.|;s|\(.*\)| \1 ) m=\1;;| ' | sort -u
  m=""
  case "$1" in 
    # todo: need to distinguish multiple entrypoints per compile?
    bean\\files ) m=bean.DeclareError;;
    bean\\files ) m=bean.Demo;;
    introduction\\files ) m=introduction.CloneablePoint;;
    introduction\\files ) m=introduction.ComparablePoint;;
    introduction\\files ) m=introduction.HashablePoint;;
    introduction\\files ) m=introduction.Point;;
    observer\\files ) m=observer.Demo;;
    spacewar\\debug ) m=spacewar.Game;;
    spacewar\\demo ) m=spacewar.Game;;
    telecom\\basic ) m=telecom.BasicSimulation;;
    telecom\\billing ) m=telecom.BillingSimulation;;
    telecom\\timing ) m=telecom.TimingSimulation;;
    tjp\\files ) m=tjp.Demo;;
    tracing\\notrace ) m=tracing.ExampleMain;;
    tracing\\tracelib ) m=tracing.ExampleMain;;
    tracing\\tracev1 ) m=tracing.ExampleMain;;
    tracing\\tracev2 ) m=tracing.ExampleMain;;
    tracing\\tracev3 ) m=tracing.ExampleMain;;
     * ) echo "rem no java command yet for $1" ;;
  esac
  if [ -n "$m" ] ; then
    case "$2" in 
      ajdb ) echo "call ..\\bin\\ajdb.bat -classpath \"$1;..\\lib\\aspectjrt.jar\" $m < ajdbInput" ;;
      java ) echo "$JAVA -classpath \"$1;..\\lib\\aspectjrt.jar\" $m ";;
      * )    echo "rem unrecognized request $2 for $1"
    esac
  fi
}
getAjdocPackages() {
  m=""
  case "$1" in 
    bean\\files ) m="bean ";;
    introduction\\files ) m="introduction ";;
    coordination\\lib ) m="coordination ";;
    observer\\files ) m="observer ";;
    spacewar\\debug ) m="spacewar coordination";;
    spacewar\\demo ) m="spacewar ";;
    telecom\\basic ) m="telecom ";;
    telecom\\billing ) m="telecom ";;
    telecom\\timing ) m="telecom ";;
    tjp\\files ) m="tjp ";;
    tracing\\notrace ) m="tracing ";;
    tracing\\tracelib ) m="tracing tracing.lib";;
    tracing\\tracev1 ) m="tracing tracing.version1";;
    tracing\\tracev2 ) m="tracing tracing.version2";;
    tracing\\tracev3 ) m="tracing tracing.version3";;
     * ) echo "rem no ajdoc command yet for $1" ;;
  esac
  echo "$m"
}

example() {
  docPacks=$(getAjdocPackages "$1")
  cat<<EOF
echo "###################### running $1"
if exist $1 rmdir /S /Q $1
if not exist $1 mkdir $1
call ..\\bin\\ajc.bat   -d $1 -classpath ..\\lib\\aspectjrt.jar -argfile $1.lst > $1\\ajc.txt 2>&1
@echo on
$(javaCommand $1 java) > $1\\java.txt 2>&1
@echo on
$(javaCommand $1 ajdb) > $1\\ajdb.txt 2>&1
@echo on
call ..\\bin\\ajdoc.bat -d $1 -classpath ..\\lib\\aspectjrt.jar -sourcepath . $docPacks > $1\\ajdoc.txt 2>&1
EOF
}

unixToDOS() {
   sed 's|||' # add ^M here (C-v C-m)
}

allListFiles() {
cat<<EOF
@bean\files.lst
@spacewar\debug.lst
@coordination\lib.lst
@introduction\files.lst
@observer\files.lst
@telecom\timing.lst
@spacewar\debug.lst
@tjp\files.lst
@tracing\tracev3.lst
EOF
}
[ -f "lib/aspectjrt.jar" ] || errMssg "rt" 3
[ -f "examples/bean/files.lst" ] || errMssg "eg" 3

cd examples
allListFiles | unixToDOS > examples.lst 
echo "exit" > ajdbInput
echo "cd examples"
echo "if not exist ajworkingdir mkdir ajworkingdir"
for i in $(ls */*.lst | sed 's|\.lst||;s|\/|\\|') ; do
   example $i | sed 's|$||' # add ^M here (C-v C-m)
done

## now do all together

dir=all
packages="bean coordination introduction spacewar telecom telecom.timing tracing tracing.lib tracing.version3"
  cat<<EOF
echo "###################### compile all "
if exist $dir rmdir /S /Q $dir
if not exist $dir mkdir $dir
call ..\\bin\\ajc.bat   -d $dir -classpath ..\\lib\\aspectjrt.jar @examples.lst > $dir\\ajc.txt 2>&1
call ..\\bin\\ajdoc.bat -d $dir -classpath ..\\lib\\aspectjrt.jar -sourcepath . $packages > $dir\\ajdoc.txt 2>&1
EOF
