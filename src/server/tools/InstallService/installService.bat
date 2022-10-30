REM [SC] QueryServiceConfig SUCCESS
REM 
REM SERVICE_NAME: enterpriseDLPServer
REM         TYPE               : 10  WIN32_OWN_PROCESS
REM         START_TYPE         : 2   AUTO_START
REM         ERROR_CONTROL      : 0   IGNORE
REM         BINARY_PATH_NAME   : "C:\Program Files\NextLabs\Policy Server\server\tomcat\bin\PolicyServer.exe" //RS//CompliantEnterpriseServer
REM         LOAD_ORDER_GROUP   :
REM         TAG                : 0
REM         DISPLAY_NAME       : Control Center Policy Server
REM         DEPENDENCIES       :
REM         SERVICE_START_NAME : LocalSystem

sc create EnterpriseDLPServer binpath= "\"INSTALLDIR_TOKENserver\tomcat\bin\PolicyServer.exe\" //RS//CompliantEnterpriseServer" start= auto error= ignore  DisplayName= "Control Center Policy Server"


