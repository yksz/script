#!/bin/sh
# remove .svn directories

find . -name .svn -print -exec rm -rf {} \; 2>/dev/null
