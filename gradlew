#!/usr/bin/env sh
set -eu

# Text-only Gradle launcher used because this repository host rejects binary files
# such as gradle-wrapper.jar. CI provisions Gradle 8.10.2 via
# gradle/actions/setup-gradle; local developers should install Gradle 8.10.2.

if [ -n "${GRADLE_HOME:-}" ] && [ -x "$GRADLE_HOME/bin/gradle" ]; then
  exec "$GRADLE_HOME/bin/gradle" "$@"
fi

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

cat >&2 <<'MSG'
Gradle 8.10.2 is required, but no Gradle executable was found.
Install Gradle 8.10.2 or run in CI with gradle/actions/setup-gradle.
MSG
exit 127
