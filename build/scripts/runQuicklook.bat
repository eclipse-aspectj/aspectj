rem @echo off

rem %~dp0 is name of current script
set scriptPath=%~dp0
set scriptDir=%scriptPath:\runQuicklook.bat=%

call %scriptDir%quicklook.bat > %scriptDir%quicklook.out 2>&1 
