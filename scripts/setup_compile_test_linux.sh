#!/usr/bin/env bash
set -euo pipefail

# ParkingDetection Linux setup/build/test helper.
# Usage:
#   ./scripts/setup_compile_test_linux.sh [GIT_URL] [WORKDIR]
# Default repository:
#   https://github.com/zkhan011/parkingdetection.git

GIT_URL="${1:-https://github.com/zkhan011/parkingdetection.git}"
WORKDIR="${2:-${PWD}/parkingdetection-linux-build}"
REPO_DIR="${WORKDIR}/parkingdetection"
APP_ID="com.zishan.parkingdetection"
APK_PATH="android/app/build/outputs/apk/debug/app-debug.apk"
ZIP_PATH="artifacts/parking-detection-debug.zip"
REQUIRED_PLATFORM="platforms/android-35/android.jar"
REQUIRED_BUILD_TOOLS="build-tools/35.0.0"

log() { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
warn() { printf '\n\033[1;33mWARNING: %s\033[0m\n' "$*" >&2; }
fail() { printf '\n\033[1;31mERROR: %s\033[0m\n' "$*" >&2; exit 1; }
have() { command -v "$1" >/dev/null 2>&1; }

find_android_sdk() {
  if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}" ]]; then printf '%s' "${ANDROID_HOME}"; return 0; fi
  if [[ -n "${ANDROID_SDK_ROOT:-}" && -d "${ANDROID_SDK_ROOT}" ]]; then printf '%s' "${ANDROID_SDK_ROOT}"; return 0; fi
  if [[ -d "${HOME}/Android/Sdk" ]]; then printf '%s' "${HOME}/Android/Sdk"; return 0; fi
  return 1
}

print_sdk_help() {
  cat >&2 <<'HELP'
Android SDK API 35 is required. Install Android Studio or command-line tools, then run:

  sdkmanager \
    "platform-tools" \
    "platforms;android-35" \
    "build-tools;35.0.0"

  yes | sdkmanager --licenses

Set ANDROID_HOME or ANDROID_SDK_ROOT to the SDK path, for example:

  export ANDROID_HOME="$HOME/Android/Sdk"
  export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
HELP
}

log "Preparing workspace: ${WORKDIR}"
mkdir -p "${WORKDIR}"
if [[ "${GIT_URL}" == "." || -d "${GIT_URL}/.git" ]]; then
  log "Copying local repository from ${GIT_URL}"
  rm -rf "${REPO_DIR}"
  mkdir -p "${REPO_DIR}"
  (cd "${GIT_URL}" && git archive --format=tar HEAD) | (cd "${REPO_DIR}" && tar xf -)
else
  log "Cloning repository over HTTPS: ${GIT_URL}"
  rm -rf "${REPO_DIR}"
  git clone "${GIT_URL}" "${REPO_DIR}"
fi
cd "${REPO_DIR}"

log "Checking required tools"
have git || fail "git is required"
have java || fail "Java 17 is required"
have unzip || fail "unzip is required"
have zip || fail "zip is required"

if [[ -d "${HOME}/.local/share/mise/installs/java/17.0.2" ]]; then
  export JAVA_HOME="${HOME}/.local/share/mise/installs/java/17.0.2"
  export PATH="${JAVA_HOME}/bin:${PATH}"
elif [[ -z "${JAVA_HOME:-}" ]]; then
  warn "JAVA_HOME is not set. Java 17 is required."
fi

log "Checking Android SDK"
SDK_DIR="$(find_android_sdk || true)"
if [[ -z "${SDK_DIR}" ]]; then print_sdk_help; fail "Android SDK not found"; fi
export ANDROID_HOME="${SDK_DIR}"
export ANDROID_SDK_ROOT="${SDK_DIR}"
export PATH="${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/cmdline-tools/latest/bin:${PATH}"
[[ -f "${ANDROID_HOME}/${REQUIRED_PLATFORM}" ]] || { print_sdk_help; fail "Missing ${ANDROID_HOME}/${REQUIRED_PLATFORM}"; }
[[ -d "${ANDROID_HOME}/${REQUIRED_BUILD_TOOLS}" ]] || { print_sdk_help; fail "Missing ${ANDROID_HOME}/${REQUIRED_BUILD_TOOLS}"; }

log "Tool versions"
java -version
./gradlew --version

log "Gradle project structure"
./gradlew projects

log "Running clean and tests"
./gradlew clean
./gradlew test
./gradlew :android:domain:test
./gradlew :android:app:testDebugUnitTest

log "Building debug APK"
./gradlew :android:app:assembleDebug
[[ -s "${APK_PATH}" ]] || fail "APK was not generated at ${APK_PATH}"

log "Verifying APK"
find . -type f -name "*.apk" -size +0 -print
file "${APK_PATH}" || true
sha256sum "${APK_PATH}"
if have aapt; then aapt dump badging "${APK_PATH}" | sed -n '1,40p'; else warn "aapt not found; skipping badging dump"; fi
if have apksigner; then apksigner verify --verbose "${APK_PATH}"; else warn "apksigner not found; skipping APK signature verification"; fi

log "Packaging ZIP artifact"
mkdir -p artifacts
cp "${APK_PATH}" artifacts/parking-detection-debug.apk
(
  cd artifacts
  sha256sum parking-detection-debug.apk > SHA256SUMS.txt
  cat > README.txt <<README
Parking Detection debug APK package

Install:
adb install -r parking-detection-debug.apk
adb shell monkey -p ${APP_ID} 1
README
  zip -9 -FS parking-detection-debug.zip parking-detection-debug.apk README.txt SHA256SUMS.txt
)
[[ -s "${ZIP_PATH}" ]] || fail "ZIP was not generated at ${ZIP_PATH}"
unzip -l "${ZIP_PATH}"

if have adb; then
  adb devices
  if adb get-state >/dev/null 2>&1; then
    adb install -r "${APK_PATH}"
    adb shell monkey -p "${APP_ID}" 1
    adb logcat -d | tail -n 200
  else
    warn "No connected adb device; skipping install/launch"
  fi
else
  warn "adb not found; skipping install/launch"
fi

if have swift && [[ -f ios/ParkingDetection/Package.swift ]]; then
  log "Running optional Swift tests"
  (cd ios/ParkingDetection && swift test)
else
  warn "Swift not available; skipping optional iOS tests"
fi

log "Done"
printf 'APK: %s/%s\n' "${REPO_DIR}" "${APK_PATH}"
printf 'ZIP: %s/%s\n' "${REPO_DIR}" "${ZIP_PATH}"
