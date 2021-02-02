#!/bin/bash
VERSION=$(git symbolic-ref -q --short HEAD || git describe --tags --exact-match 2> /dev/null || git rev-parse --short HEADgit describe --tags --exact-match 2> /dev/null || git symbolic-ref -q --short HEAD || git rev-parse --short HEAD)
if [ "$VERSION" == "main" ]
then
  echo "latest"
elif [[ "$VERSION" != v* ]];
then
  echo 'v'$VERSION
else
  echo $VERSION
fi