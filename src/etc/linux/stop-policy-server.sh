#!/bin/bash
# The program should be executed by root
[ "$(whoami)" != "root" ] && exec sudo -- "$0"
service CompliantEnterpriseServer stop