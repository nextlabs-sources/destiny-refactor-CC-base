#!/bin/sh

../java/jre/bin/java -XX:-UseSplitVerifier -Djava.library.path=common/lib -jar genappldif/genappldif.jar "$@"