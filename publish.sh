#!/bin/bash
# A script to publish the plugin
#
# Usage: ./publish.sh $RELEASE_VERSION
#
set -eu

cd $(dirname $0)

# Commit released version in gradle.properties and create a tag
commit_version() {
    new_version=$1

    sed -i "" -e "s/^version=.*$/version=$new_version/" gradle.properties

    git add gradle.properties
    git commit -m "Release $new_version"

    git push origin master

    tag="v$new_version"
    git tag $tag
    git push origin $tag
}

version="$1"
if [ -z "$version" ]; then
    echo "Usage: $0 RELEASE_VERSION" >&2
    exit 1
fi

if [ $(git tag | grep "^v$version\$" | wc -l) -ne 0 ]; then
    echo "$version already released"
    exit 1
fi

echo "Publishing $version"
./gradlew -P snapshot=false -P version="$version" clean publishPlugin
commit_version $version
