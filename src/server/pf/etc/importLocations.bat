@echo off
rem $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/etc/importLocations.bat#1 $

..\java\jre\bin\java.exe -XX:-UseSplitVerifier -jar locationimporter/location-importer.jar %*
