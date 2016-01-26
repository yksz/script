#!/bin/sh

if [ $# -lt 1 ] ; then
    echo "usage: $0 <install path>"
    exit 1
fi

TARGET_DIR=('shell')

install_path=$1
echo install to $install_path

script_dir=$(cd $(dirname $0); pwd)
for dir in ${TARGET_DIR[@]} ; do
    dir_path="$script_dir/$dir"
    script_names=(`ls $dir_path`)
    for script_name in ${script_names[@]} ; do
        target="$dir_path/$script_name"
        ln -sf $target $install_path
    done
done
