#!/bin/bash

$HADOOP_HOME/bin/hdfs hdfs dfs -mkdir -p /spark-logs

$HADOOP_HOME/bin/yarn --config $HADOOP_CONF_DIR historyserver
