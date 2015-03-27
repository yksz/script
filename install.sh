#!/bin/sh

if [ $# -lt 1 ] ; then
    echo 'usage: ./install.sh <install path>'
    exit 1
fi

path=$1
root=(`ls`)

echo install to $path
for file in ${root[@]} ; do
    if [ -d $file ] ; then
        scripts=(`ls $file`)
        for script in ${scripts[@]} ; do
            ln -s $script $path
        done
    fi
done
