#!/bin/sh

OPTIONS="-cp importexport-cli.jar:../common/lib/client-security-config.jar:../common/lib/common-framework.jar:../common/lib/app-framework.jar:../common/lib/client-pf.jar:../common/lib/common-pf.jar:../common/lib/castor-0.9.5.4.jar:../common/lib/commons-logging.jar:../common/lib/jaxrpc.jar:../common/lib/common-framework-types.jar:../common/lib/axis.jar:../common/lib/policy-services.jar:../common/lib/policy-types.jar:../common/lib/commons-discovery-0.2.jar:../common/lib/saaj.jar:../common/lib/server-shared-types.jar:../common/lib/server-shared-services.jar:../common/lib/wsdl4j-1.5.1.jar:../common/lib/antlr.jar:../common/lib/mail.jar:../common/lib/xercesImpl.jar:../common/lib/common-domain.jar:../common/lib/xml-apis.jar -Djava.util.logging.config.file=logging.properties -XX:-UseSplitVerifier"

../../java/jre/bin/java $OPTIONS com.nextlabs.shared.tools.EntityImportSeedData "$@"
