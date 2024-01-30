@echo off

rem Change this to be the full path for this directory
set EXTRACTION=c:\aj-oopsla

if exist "%JAVA_HOME%\bin\java.exe" goto haveJava
if exist "%JAVA_HOME%\bin\java.bat" goto haveJava
if exist "%JAVA_HOME%\bin\java" goto haveJava
echo java does not exist as %JAVA_HOME%\bin\java
echo please fix the JAVA_HOME environment variable

:haveJava
set ASPECTJ_HOME=%EXTRACTION%\aspectj
set PATH=%ASPECTJ_HOME%\bin;%PATH%
set CLASSPATH=.;%ASPECTJ_HOME%\lib\aspectjrt.jar;%EXTRACTION%\junit.jar
