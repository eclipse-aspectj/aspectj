#!/bin/sh

[ -n "$DEBUG" ] && set -vx
genAntScript=${genAntScript} # anything to create Ant script instead
eclipseDir=`cd $HOME/../apps/eclipse && pwd`
[ -d "$eclipseDir" ] || exit 3
scriptDir=`dirname "$0`
scriptDir=`cd "$scriptDir"; pwd`
cd "$scriptDir/../.." # in lib/build

listAllPlugins() {
    cd "$eclipseDir/plugins"
    ls -d *
}

# try first without ant
jdtPlugins() {
   cat<<EOF
org.apache.ant_1.4.1
org.apache.xerces_4.0.3
org.eclipse.core.boot_2.0.0
org.eclipse.core.resources_2.0.0
org.eclipse.core.runtime_2.0.0
EOF
}

#return paths from eclipse plugin dir to jdt jars
jdtPluginJars() {
  curDir=`pwd`
  for i in `jdtPlugins` ; do
    cd "$eclipseDir/plugins/$i"
    for j in *.jar; do 
       echo "$i/$j"
    done
    cd "$curDir"
  done
}

verifyingCreatedAndActual() {
cat<<EOF
 rm made
 \$JDKDIR/bin/jar tf jdtDepends.jar \
   | sed '/META-INF/d' \
   | sort > made
 rm have
 \$JDKDIR/bin/jar tf \
    c:/home/wes/aj/aspectj/modules/lib/eclipse2.0/jdtDepends.jar \
    | sed '/META-INF/d' \
    | sort > have
 diff made have
1720d1719
< org/w3c/dom/html/HTMLDOMImplementation.class
EOF
}

cd "$eclipseDir/plugins"
if [ -n "$genAntScript" ] ; then
    cat<<EOF
       <!--
       <property name="eclipseDir"
            location="\${eclipseDir}"/>
       -->
    <target name="jdtDepends.jar" if="\${eclipseDir}" >
       <jar destfile="jdtDepends.jar" >
         <zipgroupfileset dir="\${eclipseDir}/plugins">
EOF
    for k in `jdtPluginJars` ; do
    cat <<EOF
           <include name="$k" />
EOF
    done
    cat <<EOF
         </zipgroupfileset>
       </jar>
    </target>
EOF
else
    mkdir tempzzz; cd tempzzz ; rm -rf *;
    for k in `jdtPluginJars` ; do
        "$JDKDIR"/bin/jar xf ../$k
    done
    "$JDKDIR"/bin/jar cfM ../jdtDepends.jar .
    cd ..
    mv jdtDepends.jar "$scriptDir"

    ls -ald "$scriptDir/jdtDepends.jar"
fi
