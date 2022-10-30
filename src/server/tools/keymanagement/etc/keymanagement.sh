#!/bin/sh

OPTIONS="-Djava.util.logging.config.file=keymanagement.logging.properties -Djava.library.path=. -DKM_TOOL_HOME=."

#JAVA="$JAVA_HOME/bin/java"

../../java/jre/bin/java $OPTIONS -jar keymanagement.jar "$@"

