#!/usr/bin/env bash
CLUSTER_NAME=$1
CASSANDRA_VERSION=$2
CASSANDRA_NODES=$3

export PATH=$PATH:"/home/wespe/.sdkman/candidates/java/8.0.212-amzn/bin"
ccm status 2> /dev/null | grep "$CLUSTER_NAME"
if [[ $? -ne 0 ]]
then
    echo "Starting Cluster ${CLUSTER_NAME}"
    ccm create ${CLUSTER_NAME} -v ${CASSANDRA_VERSION} -n ${CASSANDRA_NODES} -s 2> /dev/null
fi