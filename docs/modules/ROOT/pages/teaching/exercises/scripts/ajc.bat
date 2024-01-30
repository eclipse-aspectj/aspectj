@echo off
if exist "%JAVA_HOME%\bin\java.exe" goto haveJava
if exist "%JAVA_HOME%\bin\java.bat" goto haveJava
if exist "%JAVA_HOME%\bin\java" goto haveJava
echo java does not exist as %JAVA_HOME%\bin\java
echo please fix the JAVA_HOME environment variable

:haveJava
"%JAVA_HOME%\bin\java" -classpath "%ASPECTJ_HOME%\lib\aspectjtools.jar;%JAVA_HOME%\lib\tools.jar;%CLASSPATH%" -Xmx64M org.aspectj.tools.ajc.Main %1 %2 %3 %4 %5 %6 %7 %8 %9
