#/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf

SERVER_NAME=`sed '/dubbo.application.name/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`

LOGS_DIR=""
if [ -n "LOGS_FILE"]; then
    LOGS_DIR=`dirname $LOGS_FILE`