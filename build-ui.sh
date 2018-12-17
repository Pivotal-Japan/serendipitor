#!/bin/bash

STATIC_DIR=../src/main/resources/public/

pushd serendipitor-ui
elm-app build
rm -rf ${STATIC_DIR}
mkdir ${STATIC_DIR}
cp -r build/* ${STATIC_DIR}
popd