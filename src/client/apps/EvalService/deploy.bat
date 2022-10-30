echo on
cmd /C C:\apache-tomcat-6.0.37\bin\shutdown.bat
cmd /C ant -f C:\work\EvalService\scripts\build.xml
rem copy /Y C:\work\EvalService\build\EvalService.jar C:\apache-tomcat-6.0.37\webapps\EvalService\WEB-INF\lib\EvalService.jar
rmdir /s /q C:\apache-tomcat-6.0.37\webapps\EvalService
del /F /Q C:\apache-tomcat-6.0.37\webapps\EvalService.war
copy /Y C:\work\EvalService\build\EvalService.war C:\apache-tomcat-6.0.37\webapps\EvalService.war
cmd /c C:\apache-tomcat-6.0.37\bin\catalina.bat jpda start