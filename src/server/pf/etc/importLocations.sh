#!/bin/sh

../java/jre/bin/java -XX:-UseSplitVerifier -jar locationimporter/location-importer.jar "$@"