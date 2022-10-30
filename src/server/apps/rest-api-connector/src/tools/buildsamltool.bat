@ECHO OFF

SET XLIB_LOCATION=%1

IF NOT DEFINED XLIB_LOCATION GOTO :USAGE

javac -cp "%XLIB_LOCATION%/*" SAMLTool.java

EXIT /B 0

:USAGE
echo "buildsamltool.bat <location of xlib jar files>"
