#!/bin/sh
##
## Replace a string in multiple files
##

if [ $# -lt 3 ]; then
    echo 'usage: ./replace.sh <old string> <new string> <file>'
    echo '  e.g. ./replace.sh lang long "*.txt"'
    exit 1
fi

find -name "$3" | xargs sed -i'.bak' "s/$1/$2/g"
