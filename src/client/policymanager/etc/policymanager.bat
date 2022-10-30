@echo off
if exist "jre\bin\java.exe" goto usejre
java -Djava.library.path=plugins\com.bluejungle.destiny.policymanager_1.0.0\bin -cp startup.jar org.eclipse.core.launcher.Main -application com.bluejungle.destiny.policymanager.PolicyManagerApplication -clean
goto end
:usejre
jre\bin\java -Djava.library.path=plugins\com.bluejungle.destiny.policymanager_1.0.0\bin -cp startup.jar org.eclipse.core.launcher.Main -application com.bluejungle.destiny.policymanager.PolicyManagerApplication -clean
:end