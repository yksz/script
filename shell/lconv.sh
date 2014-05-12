#!/bin/sh
# newline converter

if [ $# -lt 3 ]; then
    echo 'usage: lconv.sh from to file'
    echo '  e.g. lconv.sh CRLF LF "*.txt"'
    exit 1
fi

newline() {
    case $1 in
        CRLF) echo '\r\n' ;;
        CR)   echo '\r' ;;
        LF)   echo '\n' ;;
    esac
}

for input in `find . -name "$3" -print`; do
    echo $input
    output="${input}.tmp"
    awk -F `newline $1` -v ORS=`newline $2` '{ print }' $input > $output
    mv $output $input
done
