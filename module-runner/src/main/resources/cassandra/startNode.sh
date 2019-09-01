#!/usr/bin/env bash
NODE=$1

export PATH=$PATH:"/home/wespe/.sdkman/candidates/java/8.0.222-amzn/bin"
ccm ${NODE} start