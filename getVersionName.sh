#!/bin/bash
VERSIONNAME="4.0"
COMMITNUM=`git log --pretty=format:'' | wc -l | tr -d ' '`

logger "Build version: $VERSIONNAME.${COMMITNUM}-${BUILD_NUMBER}"
versionCode=`./getVersionCode.sh`
brand=$(./getBuilding.sh)

if [ "${BUILD_NUMBER}" == "" ]; then
    echo $VERSIONNAME.${versionCode}_$brand\#debug
else
    echo $VERSIONNAME.${versionCode}_$brand\#${BUILD_NUMBER}
fi

