#!/bin/sh

find . -name .git -print -exec rm -rf {} \; 2>/dev/null
