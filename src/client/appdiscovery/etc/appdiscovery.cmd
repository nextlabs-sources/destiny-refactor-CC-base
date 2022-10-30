@echo off
if exist "jre\bin\java.exe" goto usejre
java -Djava.library.path=. -cp appdiscovery.jar;agent-ipc.jar;common-framework.jar;ddif-tools.jar;runtime.jar;osgi.jar;core.jar;resolver.jar;defaultAdaptor.jar;eclipseAdaptor.jar;console.jar;ui.jar;swt.jar;jface.jar;commons-logging-1.0.4.jar com.bluejungle.destiny.appdiscovery.ApplicationDiscovery
goto end
:usejre
jre\bin\java -Djava.library.path=. -cp appdiscovery.jar;agent-ipc.jar;common-framework.jar;ddif-tools.jar;runtime.jar;osgi.jar;core.jar;resolver.jar;defaultAdaptor.jar;eclipseAdaptor.jar;console.jar;ui.jar;swt.jar;jface.jar;commons-logging-1.0.4.jar com.bluejungle.destiny.appdiscovery.ApplicationDiscovery
:end
