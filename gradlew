#!/usr/bin/env sh
set -eu

# Text-only Gradle bootstrapper. The repository host rejects binary files, so
# gradle-wrapper.jar is not committed. This script still avoids requiring a
# machine-level Gradle install by downloading Gradle 8.10.2 into .gradle/.

GRADLE_VERSION="8.10.2"
DIST_NAME="gradle-${GRADLE_VERSION}-bin"
DIST_URL="https://services.gradle.org/distributions/${DIST_NAME}.zip"
BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
BOOTSTRAP_DIR="${BASE_DIR}/.gradle/bootstrap"
GRADLE_HOME_LOCAL="${BOOTSTRAP_DIR}/${DIST_NAME}"
GRADLE_BIN="${GRADLE_HOME_LOCAL}/bin/gradle"
ZIP_PATH="${BOOTSTRAP_DIR}/${DIST_NAME}.zip"

if [ -n "${GRADLE_HOME:-}" ] && [ -x "${GRADLE_HOME}/bin/gradle" ]; then
  exec "${GRADLE_HOME}/bin/gradle" "$@"
fi

if [ -x "${GRADLE_BIN}" ]; then
  exec "${GRADLE_BIN}" "$@"
fi

mkdir -p "${BOOTSTRAP_DIR}"

echo "Gradle ${GRADLE_VERSION} not found locally; downloading ${DIST_URL}" >&2
DOWNLOAD_OK=0
if command -v curl >/dev/null 2>&1; then
  if curl -fL --retry 3 --connect-timeout 20 -o "${ZIP_PATH}" "${DIST_URL}"; then DOWNLOAD_OK=1; fi
elif command -v wget >/dev/null 2>&1; then
  if wget -O "${ZIP_PATH}" "${DIST_URL}"; then DOWNLOAD_OK=1; fi
else
  cat >&2 <<MSG
Neither curl nor wget is available to download Gradle ${GRADLE_VERSION}.
Install curl/wget, set GRADLE_HOME, or preinstall Gradle ${GRADLE_VERSION} at:
  ${GRADLE_HOME_LOCAL}
MSG
  exit 127
fi

if [ "${DOWNLOAD_OK}" -ne 1 ]; then
  if command -v gradle >/dev/null 2>&1; then
    echo "Gradle download failed; falling back to gradle on PATH." >&2
    exec gradle "$@"
  fi
  echo "Gradle download failed and no gradle executable exists on PATH." >&2
  exit 1
fi

if ! command -v unzip >/dev/null 2>&1; then
  echo "unzip is required to extract ${ZIP_PATH}" >&2
  exit 127
fi

rm -rf "${GRADLE_HOME_LOCAL}"
unzip -q "${ZIP_PATH}" -d "${BOOTSTRAP_DIR}"
rm -f "${ZIP_PATH}"

if [ ! -x "${GRADLE_BIN}" ]; then
  echo "Downloaded Gradle but executable was not found at ${GRADLE_BIN}" >&2
  exit 1
fi

exec "${GRADLE_BIN}" "$@"
