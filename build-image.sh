#!/bin/bash
set -e -u -o pipefail

if [ $# -lt 1 ]; then
  echo 'version is required'
  exit 1
fi

VERSION=$1

docker build . -t midjourney-proxy:${VERSION}

docker tag midjourney-proxy:${VERSION} iddddg/midjourney-proxy:${VERSION}
docker push iddddg/midjourney-proxy:${VERSION}
