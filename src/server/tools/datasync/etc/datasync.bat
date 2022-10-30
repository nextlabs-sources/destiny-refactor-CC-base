@echo off

..\..\java\jre\bin\java.exe -Xms256m -Xmx512m -Djava.util.logging.config.file=logging.properties -jar datasync.jar %*

