@echo off
rem *******************************************************************
rem Copyright (c) 2004 IBM Corporation
rem All rights reserved. 
rem This program and the accompanying materials are made available 
rem under the terms of the Common Public License v1.0 
rem which accompanies this distribution and is available at 
rem http://www.eclipse.org/legal/cpl-v10.html 
rem  
rem Contributors: 
rem     Matthew Webster     initial implementation 
rem ******************************************************************/

if "%ASPECTJ_HOME%" == "" set ASPECTJ_HOME=..\..\..\

if exist "%JAVA_HOME%\bin\java.exe" goto haveJava
if exist "%JAVA_HOME%\bin\java.bat" goto haveJava
if exist "%JAVA_HOME%\bin\java" goto haveJava
echo java does not exist as %JAVA_HOME%\bin\java
echo please fix the JAVA_HOME environment variable
:haveJava
"%JAVA_HOME%\bin\java" -classpath "%ASPECTJ_HOME%\lib\aspectjtools.jar" "-Djava.system.class.loader=org.aspectj.weaver.WeavingURLClassLoader" "-Daj.class.path=%ASPECTPATH%;%CLASSPATH%" "-Daj.aspect.path=%ASPECTPATH%" %1 %2 %3 %4 %5 %6 %7 %8 %9 
