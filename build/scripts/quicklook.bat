rem be verbose, no @echo off
rem requires existing ajhome install for ant scripts, libs
rem beware - withou DOS linefeeds, DOS won't interpret...

rem ------ read variables from local setup
set scriptPath=%~dp0
set scriptDir=%scriptPath:\quicklook.bat=%
if not exist %scriptDir%\localSetup.bat goto ERR_SETUP

call %scriptDir%\localSetup.bat 

if "x" == "x%userEmail%" goto ERR_VARIABLE
if "x" == "x%mailHost%"  goto ERR_VARIABLE
if "x" == "x%HOME%"      goto ERR_VARIABLE
if "x" == "x%CVS_RSH%"   goto ERR_VARIABLE
if "x" == "x%CVSROOT%"   goto ERR_VARIABLE

set buildDir=%scriptDir:\scripts=%
set antScript=%buildDir:\build=%\lib\ant\bin\ant.bat

if not exist %antScript% goto ERR_ANT
if not exist %buildDir%  goto ERR_BUILDDIR
if not exist %JDKDIR%    goto ERR_JDKDIR

rem XXX redo these when web building 
rem set SQEDIRPATH=%SQEDRIVE%%SQEDIR%
rem if exist %SQEDIRPATH%    goto RUN
rem net use %SQEDRIVE% \\samba\aop /persistent:no
rem if not exist %SQEDIRPATH% goto ERR_MOUNT
rem set mountedDrive=yes
goto RUN

rem build update.tree, quicklook
:RUN
set JAVA_HOME=%JDKDIR%
if not "x" == "x%DEBUG%" set verbose=-verbose
chdir %buildDir% || goto ERR_CD
rem fyi, normal ant.bat script loses any internal "=", so we rely on a patched script
set MAIL_OPTIONS=-logger org.apache.tools.ant.listener.MailLogger -DMailLogger.mailhost=%mailHost% -DMailLogger.success.to=%userEmail% -DMailLogger.failure.to=%userEmail% -DMailLogger.from=%userEmail% 
set options=%MAIL_OPTIONS% -DCVSROOT=%CVSROOT%
rem cannot use "|| goto ERR_ANT_UPDATE" b/c ant script returns non-zero for valid builds
call %antScript% -f build.xml update.tree %verbose% %options% 

set options=%MAIL_OPTIONS% -Duser.email=%userEmail% -Daop.dir=%SQEDRIVE% 
set options=%options%  -Drun.ajcTests=runAjcTests -Drun.junit.tests=runJUnitTests
call %antScript% -f build.xml quicklook %verbose% %options% 

if "ok%mountedDrive%" == "okyes" net use %SQEDRIVE% /delete
goto QUIT

rem ---------- errors
:ERR_CD
echo "unable to cd to build directory: %buildDir%"
goto QUIT

:ERR_SETUP
echo "expected local setup in %scriptDir%\localSetup.bat"
goto QUIT

:ERR_BUILDDIR
echo "expected build dir: %buildDir%"
goto QUIT

:ERR_VARIABLE
echo "local setup is incorrect - missing variables"
goto QUIT

:ERR_ANT
echo "expected ant script: %antScript%"
goto QUIT

:ERR_JDKDIR
echo "no JDKDIR=%JDKDIR%"
goto QUIT

:ERR_MOUNT
echo "unable to mount or use SQEDIRPATH=%SQEDIRPATH%"
goto QUIT

:ERR_CREATE_FAILED
echo "unable to find quicklook source after running setup build"
goto QUIT

:ERR_ANT_CREATE
echo "FAIL ant create.source failed"
goto QUIT

:ERR_ANT_UPDATE
echo "FAIL ant update.source failed"
goto QUIT

:ERR_ANT_QUICKLOOK
echo "FAIL ant quicklook failed"
goto QUIT

:ERR_ANT_DESTORY
echo "FAIL ant destroy.source failed"
goto QUIT

:QUIT
