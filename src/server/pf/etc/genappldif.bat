@echo off
..\java\jre\bin\java.exe -XX:-UseSplitVerifier -Djava.library.path=common/dll -jar genappldif/genappldif.jar %*