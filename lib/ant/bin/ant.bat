@echo off

REM   Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
REM   reserved.

if exist "%HOME%\antrc_pre.bat" call "%HOME%\antrc_pre.bat"

if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_ANT_HOME=%~dp0..

if "%ANT_HOME%"=="" set ANT_HOME=%DEFAULT_ANT_HOME%
set DEFAULT_ANT_HOME=

REM aspectJ disabled - separates -Dfoo=bar into -Dfoo bar
rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
rem set ANT_CMD_LINE_ARGS=%1
rem if ""%1""=="""" goto doneStart
rem shift
rem :setupArgs
rem if ""%1""=="""" goto doneStart
rem set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% %1
rem shift
rem goto setupArgs
rem This label provides a place for the argument list loop to break out 
rem and for NT handling to skip to.
rem :doneStart

rem find ANT_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if exist "%ANT_HOME%" goto checkJava

rem check for ant in Program Files on system drive
if not exist "%SystemDrive%\Program Files\ant" goto checkSystemDrive
set ANT_HOME=%SystemDrive%\Program Files\ant
goto checkJava

:checkSystemDrive
rem check for ant in root directory of system drive
if not exist %SystemDrive%\ant\nul goto checkCDrive
set ANT_HOME=%SystemDrive%\ant
goto checkJava

:checkCDrive
rem check for ant in C:\ant for Win9X users
if not exist C:\ant\nul goto noAntHome
set ANT_HOME=C:\ant
goto checkJava

:noAntHome
echo ANT_HOME is set incorrectly or ant could not be located. Please set ANT_HOME.
goto end

:checkJava
set LOCALCLASSPATH=%CLASSPATH%
for %%i in ("%ANT_HOME%\lib\*.jar") do call "%ANT_HOME%\bin\lcp.bat" %%i
rem aspectj adding junit to everything...
call "%ANT_HOME%\bin\lcp.bat" %ANT_HOME%\..\junit\junit.jar

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
set _JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_HOME%\lib\tools.jar" call "%ANT_HOME%\bin\lcp.bat" %JAVA_HOME%\lib\tools.jar
if exist "%JAVA_HOME%\lib\classes.zip" call "%ANT_HOME%\bin\lcp.bat" %JAVA_HOME%\lib\classes.zip
goto checkJikes

:noJavaHome
set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If build fails because sun.* classes could not be found
echo   you will need to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.

:checkJikes
if not "%JIKESPATH%"=="" goto runAntWithJikes

:runAnt
"%_JAVACMD%" -classpath "%LOCALCLASSPATH%" "-Dant.home=%ANT_HOME%" %ANT_OPTS% org.apache.tools.ant.Main %*
goto end

:runAntWithJikes
"%_JAVACMD%" -classpath "%LOCALCLASSPATH%" "-Dant.home=%ANT_HOME%" "-Djikes.class.path=%JIKESPATH%" %ANT_OPTS% org.apache.tools.ant.Main  %*
goto end

:end
set LOCALCLASSPATH=
set _JAVACMD=
set ANT_CMD_LINE_ARGS=

if "%OS%"=="Windows_NT" @endlocal

:mainEnd
if exist "%HOME%\antrc_post.bat" call "%HOME%\antrc_post.bat"

