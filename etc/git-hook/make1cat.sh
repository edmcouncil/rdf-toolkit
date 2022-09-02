#!/bin/bash
#

cd "$1" || exit 1
../etc/infra/catalog/makecat.sh
