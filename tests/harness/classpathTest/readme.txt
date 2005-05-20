

This contains artifacts for two harness test cases, 
for classpath (jar and dir) and aspectpath.  

Binaries are in jars/*, and classesDir/*, and 
binary sources and a build script are in classpath-src.
  
See specifications are in tests/ajcHarnessTests.xml, e.g.,

    <ajc-test dir="harness/classpathTest"
      title="specify jars and directories on classpath"
      keywords="purejava">
        <compile classpath="classesDir,jars/required.jar" 
                 files="Main.java"/>
        <run class="Main"/>
    </ajc-test>

    <ajc-test dir="harness/classpathTest"
      title="specify aspectpath and classpath jars and directories">
        <compile classpath="classesDir,jars/required.jar"
                 aspectpath="jars/requiredAspects.jar"
                 files="AspectMain.java"/>
        <run class="AspectMain"/>
    </ajc-test>
