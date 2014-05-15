#!/bin/sh
# newline converter

if [ $# -lt 3 ]; then
    echo 'usage: nconv.sh from to file'
    echo '  e.g. nconv.sh CRLF LF "*.txt"'
    exit 1
fi

case $1 in
    CRLF) f='\r\n' ;;
    CR)   f='\r' ;;
    LF)   f='\n' ;;
    *)    echo "unknown newline: $1"
          exit 1 ;;
esac
case $2 in
    CRLF) t='\r\n' ;;
    CR)   t='\r' ;;
    LF)   t='\n' ;;
    *)    echo "unknown newline: $2"
          exit 1 ;;
esac
for input in `find . -name "$3" -print`; do
    echo $input
    output="${input}.tmp"
    awk -F $f -v ORS=$t '{ print }' $input > $output
    mv $output $input
done
