

NEXTLABS CONTROL CENTER SERVER
---------------------------------------------

CONFIGURATION
=============
You will only have to update configuration.xml and server.xml. Refer to the configuration-template.xml and server-template.xml to know what all fields need to be updated. Use Policy_Server/tools/crypt/mkpassword.sh to encrypt the password (provide only encrypted passwords in both the xml files)


Make sure all script files (.sh) and java are executable. You can run the following commands to set the executable flag.

find . -name "*.sh" | xargs chmod +x

find . -name java | xargs chmod +x


START & STOP
===========
use start-policy-server to start the Control Center Server

use stop-policy-server to stop the Control Center Server

NOTE
====
Everytime you move this folder, install_home property in configuration/server.xml needs to be updated
