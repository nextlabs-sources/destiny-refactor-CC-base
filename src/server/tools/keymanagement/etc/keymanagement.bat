@echo off

set OPTIONS=-Djava.util.logging.config.file=keymanagement.logging.properties -Djava.library.path=. -DKM_TOOL_HOME=.

..\..\java\jre\bin\java.exe %OPTIONS% -jar keymanagement.jar %*

