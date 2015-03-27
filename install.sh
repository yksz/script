#!/bin/sh

if [ $# -lt 1 ] ; then
    echo 'usage: ./install.sh <install path>'
    exit 1
fi

INSTALL_PATH=$1
BASE_DIR=$(cd $(dirname $0); pwd)

echo install to $INSTALL_PATH
fileNames=(`ls $BASE_DIR`)
for fileName in ${fileNames[@]} ; do
    filePath="$BASE_DIR/$fileName"
    if [ -d $filePath ] ; then
        dirPath=$filePath
        scriptNames=(`ls $dirPath`)
        for scriptName in ${scriptNames[@]} ; do
            target="$dirPath/$scriptName"
            ln -s $target $INSTALL_PATH
        done
    fi
done
