@ECHO OFF

SET XLIB_LOCATION=%1

REM discard first parameter
SHIFT
SET REMAINDER=%1
:loop
shift
if [%1]==[] goto afterloop
set REMAINDER=%REMAINDER% %1
goto loop
:afterloop

IF NOT DEFINED XLIB_LOCATION GOTO :USAGE

java -classpath "%XLIB_LOCATION%/*;." SAMLTool %REMAINDER%

EXIT /B 0

:USAGE
echo "runsamltool.bat <location of xlib jar files>" (VALIDATE filename.txt|GENERATE)
