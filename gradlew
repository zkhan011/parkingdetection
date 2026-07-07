#!/usr/bin/env sh
# Text-only Gradle launcher for repositories that cannot store binary wrapper jars.
# It uses an installed Gradle executable, while the required version is documented
# in gradle/wrapper/gradle-wrapper.properties and provisioned in CI.
set -eu

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

cat >&2 <<'MSG'
Gradle is required but was not found on PATH.
Install Gradle 8.10.2, or use CI's gradle/actions/setup-gradle step, then rerun this command.
MSG
exit 127
