#!/bin/sh

OPTIONS="-Djava.library.path=. -cp ../common/lib/commons-logging.jar:../common/lib/activation.jar:app-framework.jar:../common/lib/commons-discovery-0.2.jar:enrollment-service.jar:./client-security-config.jar:enrollment.jar:../common/lib/jargs.jar:../common/lib/dom4j-1.5.1.jar:common-framework.jar:../common/lib/server-shared-types.jar:server-shared-services.jar:../common/lib/axis.jar:../common/lib/crypt.jar:../common/lib/jargs.jar:../common/lib/jaxrpc.jar:../common/lib/saaj.jar:server-tools-common.jar:../common/lib/wsdl4j-1.5.1.jar:../common/lib/mail.jar:server-shared-enrollment.jar -DENROLL_TOOL_HOME=."

#JAVA="$JAVA_HOME/bin/java"

../../java/jre/bin/java $OPTIONS com.nextlabs.destiny.tools.enrollment.ClientInfoMgr "$@"