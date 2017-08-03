#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-consumer.sh
## Description: to start consumer
## Version:     1.0
## Author:      lidiliang
## Created:     2017-08-03
################################################################################
set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
BIN_DIR=$(cd $(dirname $0); pwd)    ## bin所在目录
DEPLOY_DIR=$(cd ..;pwd)           ## 项目根目录
CONF_DIR=${DEPLOY_DIR}/conf      ## 配置文件目录
LIB_DIR=${DEPLOY_DIR}/lib        ## Jar 包目录
LIB_JARS=`ls $LIB_DIR | grep .jar | awk '{print "'$LIB_DIR'/"$0}' | tr "\n" ":"`;  ## jar 包位置以及第三方依赖jar包，绝对路径
LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
LOG_FILE=${LOG_DIR}/start-consumer.log        ##  log 日记文件

#####################################################################
# 函数名: start_consumer
# 描述: 把consumer 消费组启动起来
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function start_consumer()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    nohup java -classpath $CONF_DIR:$LIB_JARS com.hzgc.ftpserver.kafka.consumer.ConsumerGroupsMain >> ${LOG_FILE} 2>&1 &
}

#####################################################################
# 函数名: main
# 描述: 脚本主要业务入口
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function main()
{
    start_consumer
}

## 脚本主要业务入口
main
