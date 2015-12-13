#!/bin/sh

if [ $# -lt 1 ] ; then
    echo "usage: $0 <install path>"
    exit 1
fi

TARGET_DIR=('shell')
INSTALL_PATH=$1
echo install to $INSTALL_PATH

baseDir=$(cd $(dirname $0); pwd)
for dir in ${TARGET_DIR[@]} ; do
    dirPath="$baseDir/$dir"
    scriptNames=(`ls $dirPath`)
    for scriptName in ${scriptNames[@]} ; do
        target="$dirPath/$scriptName"
        ln -s $target $INSTALL_PATH
    done
done
