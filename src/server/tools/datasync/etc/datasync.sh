#!/bin/sh

../../java/jre/bin/java -Xms256m -Xmx512m -Djava.util.logging.config.file=logging.properties -jar datasync.jar "$@"