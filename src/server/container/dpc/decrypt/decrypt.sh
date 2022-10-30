#!/bin/bash

ROOT=$CATALINA_HOME

if [ -z "$ROOT" ]; then
    ROOT=$JBOSS_HOME
fi

java -classpath $ROOT/nextlabs/dpc/decryptj/agent-controlmanager.jar com.bluejungle.destiny.agent.tools.DecryptBundle 
