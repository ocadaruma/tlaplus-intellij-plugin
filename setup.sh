#!/bin/bash

set -eu

cd "$(dirname "$0")"
tla2_jar="libs/tla2tools.jar"

if [ ! -e "$tla2_jar" ]; then
  curl -L https://github.com/tlaplus/tlaplus/releases/download/v1.8.0/tla2tools.jar -o "$tla2_jar"
fi
