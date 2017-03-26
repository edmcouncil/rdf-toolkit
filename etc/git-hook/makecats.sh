#!/bin/bash
#


gitdir=`git rev-parse --git-dir`
root=`realpath $gitdir/..`


find . -maxdepth 1 -mindepth 1 -type d \(  -path ./etc -prune -o -path ./.git -prune -o   -exec `(dirname $0)`/make1cat.sh  {} \; \)
find . -name 'catalog-v001.xml' -exec git add --update {} \;
