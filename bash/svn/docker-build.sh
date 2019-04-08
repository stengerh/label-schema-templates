#!/usr/bin/env bash

if $(which xmlstarlet >/dev/null 2>&1)
then
  VCS_URL="$(svn info --xml | xmlstarlet --template --value-of 'info/entry/url')"
  VCS_REF="$(svn info --xml | xmlstarlet --template --value-of 'info/entry/commit/@revision')"
else
  VCS_URL="$(LANG=C svn info | grep '^URL:' | cut -d ' ' -f 2)"
  VCS_REF="$(LANG=C svn info | grep '^Revision:' | cut -d ' ' -f 2)"
fi

BUILD_DATE="$(date --utc +%FT%TZ)"

docker build \
  --build-arg VCS_URL="${VCS_URL}" \
  --build-arg VCS_REF="${VCS_REF}" \
  --build-arg BUILD_DATE="${BUILD_DATE}" \
  .
