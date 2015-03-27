#!/bin/sh
#
# Remove multiple directories recursively in current directory
#

if [ $# -lt 1 ]; then
    echo 'usage: ./rd.sh <directory name>'
    echo '  e.g. ./rd.sh ".svn"'
    exit 1
fi

find . -name "$1" -print -exec rm -rf {} \; 2>/dev/null
