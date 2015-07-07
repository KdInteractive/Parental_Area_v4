#!/bin/bash

COMMITNUM=`git log --pretty=format:'' | wc -l | tr -d ' '`

if [ -f building ]; then
	brand=$(cat building)
fi
#if [ -z $brand ]; then
#	COMMITNUM=$COMMITNUM
#elif [ $brand != "generic" ]; then
#	COMMITNUM=$((COMMITNUM+1000000))
#fi
logger "Build version: ${COMMITNUM}"

if [ "${BUILD_NUMBER}" == "" ]; then
    echo $((COMMITNUM+1))
else
    echo ${COMMITNUM}
fi