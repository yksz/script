#!/usr/bin/env python
# -*- coding: utf-8 -*-
# python3
# delete directories

import os
import stat
import shutil


def get_filelist(path):
    filelist = []
    for root, dirs, files in os.walk(path):
        for name in files:
            filelist.append(os.path.join(root, name))
    return filelist


def get_dirlist(path):
    dirlist = []
    for root, dirs, files in os.walk(path):
        for name in dirs:
            dirlist.append(os.path.join(root, name))
    return dirlist


def remove_dirs(path):
    filelist = get_filelist(path)
    for name in filelist:
        os.chmod(name, stat.S_IWRITE)

    dirlist = get_dirlist(path)
    for name in dirlist:
        os.chmod(name, stat.S_IWRITE)

    os.chmod(path, stat.S_IWRITE)
    shutil.rmtree(path)


def find_dirs(path, target):
    dirlist = []
    for root, dirs, files in os.walk(path):
        for name in dirs:
            if name == target:
                dirlist.append(os.path.join(root, name))
    return dirlist


def main():
    msg = "This script deletes specified directories in current directory.\n"
    msg += "Enter directory name: "
    name = input(msg)

    current_dir = os.path.abspath(os.path.dirname(__file__))
    dirs = find_dirs(current_dir, name)
    for path in dirs:
        print("delete: " + path)
        remove_dirs(path)


if __name__ == '__main__':
    main()
