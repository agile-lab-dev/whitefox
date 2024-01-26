#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
pushd $SCRIPT_DIR/..
nix-shell --run './gradlew server:app:imageBuild -Dquarkus.package.type=native -Dquarkus.native.container-build=true -Dquarkus.container-image.name=server-native'
popd
