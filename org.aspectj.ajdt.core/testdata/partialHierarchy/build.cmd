call ajc -outjar fullBase.jar sample\Base.java sample\Derived.java sample\Iface.java
jar xf fullBase.jar
jar cf base.jar sample\Derived.class sample\Iface.class
set XCLASSPATH=%CLASSPATH%
set CLASSPATH=fullBase.jar;%CLASSPATH%
call ajc -injars base.jar -outjar woven.jar sample\Trace.aj
java -classpath woven.jar;%CLASSPATH%  sample.Derived
set CLASSPATH=%XCLASSPATH%
